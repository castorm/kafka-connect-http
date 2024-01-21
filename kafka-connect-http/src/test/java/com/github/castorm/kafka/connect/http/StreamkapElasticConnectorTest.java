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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.utility.DockerImageName;

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

    @Test
    void testNominal() throws Exception {
        sendRequest("/index1/_doc", "POST", "{ \"my_timestamp\": \"" + Instant.now().toString() + "\", \"message\": \"Hello OpenSearch 1\" }");
        sendRequest("/index2/_doc", "POST", "{ \"my_timestamp\": \"" + Instant.now().toString() + "\", \"message\": \"Hello OpenSearch 2\" }");
        sendRequest("/index1_1/_doc", "POST", "{ \"my_timestamp\": \"" + Instant.now().toString() + "\", \"message\": \"Hello OpenSearch 1_1\" }");
        sendRequest("/index2_2/_doc", "POST", "{ \"my_timestamp\": \"" + Instant.now().toString() + "\", \"message\": \"Hello OpenSearch 2_1\" }");

        HttpSourceConnector connector = new HttpSourceConnector();
        Map<String, String> props = new HashMap<>();
        props.put("connector.class", "com.github.castorm.kafka.connect.http.HttpSourceConnector");
        props.put("http.request.url", "http://${server.url}/rest/api");
        props.put("http.request.params", "updated=${offset.timestamp}");
        props.put("http.request.headers", "Accept: application/json");
        props.put("http.auth.type", "Basic");
        props.put("http.auth.user", "user");
        props.put("http.auth.password", "password");
        props.put("http.offset.initial", "key=TICKT-0001, timestamp=2020-01-01T00:00:01Z, endpoint=topic-name1");
        props.put("http.response.list.pointer", "/issues");
        props.put("http.response.record.offset.pointer", "key=/key, timestamp=/fields/updated, endpoint=/fields/_index");
        props.put("kafka.topic", "topic-name");
        props.put("kafka.topic.template", "true");
        props.put("http.timer.interval.millis", "0");
        props.put("http.timer.catchup.interval.millis", "0");
        props.put("http.client.read.timeout.millis", "0");
  
        connector.start(props);
        List<Map<String, String>> taskConfigs = connector.taskConfigs(2);
        ForkJoinPool pool = new ForkJoinPool(2);
        List<SourceRecord> records = Collections.synchronizedList(new ArrayList<>());
        for (Map<String, String> taskConfig : taskConfigs) {
            HttpSourceTask task = new HttpSourceTask();
            task.initialize(getContext(emptyMap()));
            task.start(taskConfig);
            pool.submit(() -> records.addAll(task.poll()));
        }
        pool.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(records).hasSize(4);
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
