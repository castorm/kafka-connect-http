package com.github.castorm.kafka.connect.http;

import static com.github.castorm.kafka.connect.common.VersionUtils.getVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpSourceTask extends SourceTask {
    private final Function<Map<String, String>, HttpSourceConnectorConfig> configFactory;

    private Map<String, HttpSourceTaskSingleIndex> tasks;

    HttpSourceTask(Function<Map<String, String>, HttpSourceConnectorConfig> configFactory) {
        this.configFactory = configFactory;
    }

    public HttpSourceTask() {
        this(HttpSourceConnectorConfig::new);
    }

    @Override
    public void start(Map<String, String> settings) {
        String indexIncludeList = settings.get(HttpSourceConnectorConfig.INDEX_INCLUDE_LIST);
        if (null == indexIncludeList) {
            throw new ConfigException(HttpSourceConnectorConfig.INDEX_INCLUDE_LIST + " is required");
        }

        String originalUrl = settings.get(TemplateHttpRequestFactoryConfig.URL);

        List<String> indexes = List.of(indexIncludeList.split(","));
        int idx = 0;
        for (String index : indexes) {
            log.info("Initializing task {} for index {}", idx++, index);
            Map<String, String> taskSettings = new HashMap<>();
            taskSettings.putAll(settings);
            taskSettings.put(TemplateHttpRequestFactoryConfig.URL, originalUrl.replace("<INDEX>", index));
            HttpSourceTaskSingleIndex task = new HttpSourceTaskSingleIndex(this.configFactory);
            task.start(taskSettings);
            tasks.put(index, task);
        }
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        List<SourceRecord> records = new ArrayList<>();
        for (HttpSourceTaskSingleIndex task : tasks.values()) {
            records.addAll(task.poll());
        }
        return records;
    }

    private HttpSourceTaskSingleIndex getTaskForRecord(SourceRecord record) {
        String index = record.topic();
        HttpSourceTaskSingleIndex task = tasks.get(index);
        if (task == null) {
            throw new ConnectException("No HttpSourceTaskSingleIndex found for topic " + index);
        }
        return task;
    }

    @Override
    public void commitRecord(SourceRecord record, RecordMetadata metadata) {
        getTaskForRecord(record).commitRecord(record, metadata);
    }

    @Override
    public void commit() {
        for (HttpSourceTaskSingleIndex task : tasks.values()) {
            task.commit();
        }
    }

    @Override
    public void stop() {
        for (HttpSourceTaskSingleIndex task : tasks.values()) {
            task.stop();
        }
    }

    @Override
    public String version() {
        return getVersion();
    }
}
