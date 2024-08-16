package com.github.castorm.kafka.connect.http.record;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.model.KvRecord;
import com.github.castorm.kafka.connect.http.record.spi.KvSourceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.source.SourceRecord;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class ObjectMapKvSourceRecordMapper implements KvSourceRecordMapper {

    private static final String KEY_FIELD_NAME = "key";
    private static final String TIMESTAMP_FIELD_NAME = "timestamp";

    private static Map<String, ?> sourcePartition = emptyMap();

    private final Function<Map<String, ?>, SourceRecordMapperConfig> configFactory;

    private SourceRecordMapperConfig config;

    // Jackson ObjectMapper for deserialization
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ObjectMapKvSourceRecordMapper() {
        this(SourceRecordMapperConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        config = configFactory.apply(settings);
    }

    @Override
    public SourceRecord map(KvRecord record) {

        Offset offset = record.getOffset();
        Long timestamp = offset.getTimestamp().map(Instant::toEpochMilli).orElseGet(System::currentTimeMillis);

        String key = record.getKey();

        Map<String, Object> deserializedValue;
        try {
            deserializedValue = objectMapper.readValue(record.getValue().toString(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize record value", e);
        }

        deserializedValue.put(KEY_FIELD_NAME, key);
        deserializedValue.put(TIMESTAMP_FIELD_NAME, timestamp);

        return new SourceRecord(
                sourcePartition,
                offset.toMap(),
                config.getTopic(),
                null,
                null,
                key,
                null,
                deserializedValue,
                timestamp);
    }
}
