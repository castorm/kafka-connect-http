package com.github.castorm.kafka.connect.http;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 - 2024 Cástor Rodríguez
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import org.apache.kafka.connect.source.SourceTaskContext;

import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.model.Partition;
import com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpSourceTask extends SourceTask {
    private final Function<Map<String, String>, HttpSourceConnectorConfig> configFactory;

    private Map<String, HttpSourceTaskSingleEndpoint> tasks = new HashMap<>();

    HttpSourceTask(Function<Map<String, String>, HttpSourceConnectorConfig> configFactory) {
        this.configFactory = configFactory;
    }

    public HttpSourceTask() {
        this(HttpSourceConnectorConfig::new);
    }

    @Override
    public void start(Map<String, String> settings) {
        String endpointIncludeList = settings.get(HttpSourceConnectorConfig.ENDPOINT_INCLUDE_LIST);
        if (null == endpointIncludeList) {
            throw new ConfigException(HttpSourceConnectorConfig.ENDPOINT_INCLUDE_LIST + " is required");
        }

        String originalUrl = settings.get(TemplateHttpRequestFactoryConfig.URL);

        List<String> endpoints = List.of(endpointIncludeList.split(","));
        int idx = 0;
        for (String endpoint : endpoints) {
            log.info("Initializing task {} for endpoint {}", idx++, endpoint);
            Map<String, String> taskSettings = new HashMap<>();
            taskSettings.putAll(settings);
            taskSettings.put(TemplateHttpRequestFactoryConfig.URL, 
                originalUrl.replace(HttpSourceConnectorConfig.URL_ENDPOINT_PLACEHOLDER, endpoint));
            HttpSourceTaskSingleEndpoint task = new HttpSourceTaskSingleEndpoint(endpoint, this.configFactory);
            task.initialize(this.context);
            task.start(taskSettings);
            tasks.put(endpoint, task);
        }
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        List<SourceRecord> records = new ArrayList<>();
        for (HttpSourceTaskSingleEndpoint task : tasks.values()) {
            records.addAll(task.poll());
        }
        return records;
    }

    private HttpSourceTaskSingleEndpoint getTaskForRecord(SourceRecord record) {
        String endpoint = Partition.getEndpointFromPartition(record.sourcePartition());
        HttpSourceTaskSingleEndpoint task = tasks.get(endpoint);
        if (task == null) {
            throw new ConnectException("No HttpSourceTaskSingleEndpoint found for endpoint " + endpoint);
        }
        return task;
    }

    @Override
    public void commitRecord(SourceRecord record, RecordMetadata metadata) {
        getTaskForRecord(record).commitRecord(record, metadata);
    }

    @Override
    public void commit() {
        for (HttpSourceTaskSingleEndpoint task : tasks.values()) {
            task.commit();
        }
    }

    @Override
    public void stop() {
        for (HttpSourceTaskSingleEndpoint task : tasks.values()) {
            task.stop();
        }
    }

    @Override
    public String version() {
        return getVersion();
    }
}
