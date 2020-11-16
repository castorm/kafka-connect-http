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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.castorm.kafka.connect.http.HttpSourceConnector;
import com.github.castorm.kafka.connect.http.HttpSourceTask;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@UtilityClass
public class ConnectorUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ObjectReader MAP_READER = OBJECT_MAPPER.readerFor(new TypeReference<Map<String, String>>() {
    });

    @SneakyThrows({IOException.class, URISyntaxException.class})
    public static String readConnectorConfig(String connectorPathJson) {
        return new String(Files.readAllBytes(Paths.get(ConnectorUtils.class.getClassLoader().getResource(connectorPathJson).toURI())));
    }

    public static String replaceVariables(String content, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
        }
        return content;
    }

    @SneakyThrows(IOException.class)
    public static Map<String, String> getConfigMap(String connectorConfigJson) {
        return MAP_READER.readValue(OBJECT_MAPPER.readTree(connectorConfigJson).get("config"));
    }

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
        HttpSourceTask task = new HttpSourceTask();
        task.initialize(givenContextWithEmptyOffset());
        task.start(config);
        List<SourceRecord> allRecords = new ArrayList<>();
        List<SourceRecord> records;
        do {
            records = task.poll();
            for (SourceRecord record : records) {
                task.commitRecord(record, null);
            }
            task.commit();
            allRecords.addAll(records);
        } while (!records.isEmpty());
        task.stop();
        return allRecords;
    }

    private static SourceTaskContext givenContextWithEmptyOffset() {
        SourceTaskContext context = Mockito.mock(SourceTaskContext.class);
        OffsetStorageReader offsetReader = Mockito.mock(OffsetStorageReader.class);
        given(context.offsetStorageReader()).willReturn(offsetReader);
        given(offsetReader.offset(any())).willReturn(emptyMap());
        return context;
    }
}
