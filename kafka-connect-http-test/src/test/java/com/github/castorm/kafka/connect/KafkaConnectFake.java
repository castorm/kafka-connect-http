package com.github.castorm.kafka.connect;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 Cástor Rodríguez
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

import com.github.castorm.kafka.connect.http.HttpSourceConnector;
import com.github.castorm.kafka.connect.http.HttpSourceTaskSingleEndpoint;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;

@UtilityClass
public class KafkaConnectFake {

    public static List<SourceRecord> readAllRecords(Map<String, String> config) {
        HttpSourceConnector connector = new HttpSourceConnector();
        connector.start(config);
        List<SourceRecord> records = connector.taskConfigs(parseInt(config.getOrDefault("max.tasks", "1")))
                .parallelStream()
                .flatMap(conf -> runTaskUntilExhaust(conf).stream())
                .collect(Collectors.toList());
        connector.stop();
        return records;
    }

    @SneakyThrows
    private static List<SourceRecord> runTaskUntilExhaust(Map<String, String> config) {
        HttpSourceTaskSingleEndpoint task = new HttpSourceTaskSingleEndpoint("dummy");
        task.initialize(emptyContext());
        task.start(config);
        List<SourceRecord> allRecords = new ArrayList<>();
        List<SourceRecord> records;
        do {
            records = task.poll();
            records.forEach(record -> task.commitRecord(record, null));
            task.commit();
            allRecords.addAll(records);
        } while (!records.isEmpty());
        task.stop();
        return allRecords;
    }

    private static SourceTaskContext emptyContext() {
        return new SourceTaskContext() {

            @Override
            public Map<String, String> configs() {
                return emptyMap();
            }

            @Override
            public OffsetStorageReader offsetStorageReader() {
                return new OffsetStorageReader() {

                    @Override
                    public <T> Map<String, Object> offset(Map<String, T> map) {
                        return emptyMap();
                    }

                    @Override
                    public <T> Map<Map<String, T>, Map<String, Object>> offsets(Collection<Map<String, T>> collection) {
                        return emptyMap();
                    }
                };
            }
        };
    }
}
