package com.github.castorm.kafka.connect.http;

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

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@UtilityClass
public class ConnectorUtils {

    public static Map<String, String> processValues(HashMap<Object, Object> config, Function<String, String> valueProcessor) {
        return config.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>((String) entry.getKey(), valueProcessor.apply((String) entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static HashMap<Object, Object> loadConnectorConfig(String connector) throws IOException {
        InputStream inputStream = ConnectorUtils.class.getClassLoader().getResourceAsStream("connectors/" + connector);
        Properties properties = new Properties();
        properties.load(inputStream);
        return new HashMap<>(properties);
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
            records.forEach(task::commitRecord);
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
