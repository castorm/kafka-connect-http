package com.github.castorm.kafka.connect.http;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class StreamkapElasticConnectorTest {

    private static OpensearchContainer<?> opensearch = new OpensearchContainer<>(
        DockerImageName.parse("opensearchproject/opensearch:2.11.0"));

    @BeforeAll
    static void before() {
        opensearch.start();
    }

    @AfterAll
    static void after() {
        opensearch.stop();
    }

    private static SourceTaskContext getContext(Map<String, Object> offset) {
        SourceTaskContext context = mock(SourceTaskContext.class);
        OffsetStorageReader offsetStorageReader = mock(OffsetStorageReader.class);
        given(context.offsetStorageReader()).willReturn(offsetStorageReader);
        given(offsetStorageReader.offset(any())).willReturn(offset);
        return context;
    }

    private Map<String, String> getConf() {
        Map<String, String> props = new HashMap<>();
        props.put("http.response.record.offset.pointer", "key=/_id, timestamp=/sort/0, endpoint=/_index");
        props.put("http.request.body", "{\"size\": 1, \"sort\": [{\"my_timestamp\": \"asc\"}], \"search_after\": [${offset.timestamp?datetime.iso?long}]}");
        props.put("http.request.url", opensearch.getHttpHostAddress() + "/" + HttpSourceConnectorConfig.URL_ENDPOINT_PLACEHOLDER + "/_search");
        props.put("http.response.record.pointer", "/_source");
        props.put("http.request.method", "POST");
        props.put("http.response.list.pointer", "/hits/hits");
        props.put("http.request.headers", "Content-Type: application/json");
        props.put("http.offset.initial", "timestamp=2024-01-10T14:24:03Z");
        props.put("kafka.topic", "my_prefix");
        props.put("kafka.topic.template", "true");
        props.put("http.timer.interval.millis", "0");
        props.put("http.timer.catchup.interval.millis", "0");
        props.put("http.client.read.timeout.millis", "0");
        return props;
    }

    private String loadTestData(int nbIndexes) throws Exception {
        return loadTestData(nbIndexes, 1);
    }
    private String loadTestData(int nbIndexes, int nbRecordsPerIndex) throws Exception {
        List<String> indexes = new ArrayList<>();
        for (int i = 0; i < nbIndexes; i++) {
            long now = new Date().getTime();
            for (int j = 0; j < nbRecordsPerIndex; j++) {
                String id = "" + i + "-" + j;
                sendRequest("/index" + i + "/_doc/" + id, "PUT", 
                    "{ \"my_timestamp\": \"" + Instant.ofEpochMilli(now + j).toString() + "\", \"message\": \"Hello OpenSearch " + id + "\" }");
            }
            indexes.add("index" + i);
        }
        Thread.sleep(2000);//wait for ES to index the data and make it available for search
        return StringUtils.join(indexes, ",");
    }

    private List<SourceRecord> runTasks(Map<String, String> config, int nbTasks) throws InterruptedException {
        return runTasks(config, nbTasks, 1);
    }
    private List<SourceRecord> runTasks(Map<String, String> config, int nbTasks, int nbPolls) throws InterruptedException {
        HttpSourceConnector connector = new HttpSourceConnector();
        connector.start(config);
        List<Map<String, String>> taskConfigs = connector.taskConfigs(2);
        ForkJoinPool pool = new ForkJoinPool(2);
        List<SourceRecord> records = Collections.synchronizedList(new ArrayList<>());
        for (Map<String, String> taskConfig : taskConfigs) {
            HttpSourceTask task = new HttpSourceTask();
            task.initialize(getContext(emptyMap()));
            task.start(taskConfig);
            log.info("Starting task: {}", taskConfig.get(HttpSourceConnectorConfig.ENDPOINT_INCLUDE_LIST));
            pool.submit(() -> {
                for (int i = 0; i < nbPolls; i++) {
                    try {
                        List<SourceRecord> polledRecords = task.poll();
                        records.addAll(polledRecords);
                        for (SourceRecord record : polledRecords) {
                            task.commitRecord(record, null);
                        }
                        task.commit();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(1000, TimeUnit.SECONDS);
        log.info("Tasks done, got {} records", records.size());

        return records;
    }

    @Test
    void testNominal() throws Exception {
        String endpointIncludeList = loadTestData(4);
        Map<String, String> config = getConf();
        config.put(HttpSourceConnectorConfig.ENDPOINT_INCLUDE_LIST, endpointIncludeList);
        List<SourceRecord> records = runTasks(config, 4);
        assertThat(records).hasSize(4);
        assertThat(records.get(0).value()).isInstanceOf(Struct.class);
        assertThat(((Struct)records.get(0).value()).get("_streamkap_value").toString()).contains("my_timestamp");
    }

    @Test
    void testTimestamps() throws Exception {
        String endpointIncludeList = loadTestData(4, 4);
        Map<String, String> config = getConf();
        config.put(HttpSourceConnectorConfig.ENDPOINT_INCLUDE_LIST, endpointIncludeList);
        List<SourceRecord> records = runTasks(config, 4, 4);
        assertThat(records).hasSize(16);
        assertThat(records.get(0).value()).isInstanceOf(Struct.class);
        assertThat(((Struct) records.get(0).value()).get("_streamkap_value").toString()).contains("my_timestamp");
        assertThat(records.stream()
                .sorted((r1, r2) -> ((Struct) r1.value()).getString("_streamkap_key")
                        .compareTo(((Struct) r2.value()).getString("_streamkap_key")))
                .map(r -> ((Struct) r.value()).getString("_streamkap_key")).toArray())
                .containsExactly(
                        "0-0", "0-1", "0-2", "0-3",
                        "1-0", "1-1", "1-2", "1-3",
                        "2-0", "2-1", "2-2", "2-3",
                        "3-0", "3-1", "3-2", "3-3");
    }

    @Test
    void testPerf() throws Exception {
        String endpointIncludeList = loadTestData(400);
        Thread.sleep(10000);
        Map<String, String> config = getConf();
        config.put(HttpSourceConnectorConfig.ENDPOINT_INCLUDE_LIST, endpointIncludeList);
        List<SourceRecord> records = runTasks(config, 10);
        assertThat(records).hasSize(400);
    }

    private void sendRequest(String urlPath, String method, String jsonInputString) throws Exception {
        URL url = new URL(opensearch.getHttpHostAddress() + urlPath);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);			
        }

        int responseCode = con.getResponseCode();
        System.out.println(responseCode); // Handle response code appropriately
    }
}
