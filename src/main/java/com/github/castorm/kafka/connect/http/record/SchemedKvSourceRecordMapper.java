package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * kafka-connect-http
 * %%
 * Copyright (C) 2020 CastorM
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

import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.model.KvRecord;
import com.github.castorm.kafka.connect.http.record.spi.KvSourceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static org.apache.kafka.connect.data.SchemaBuilder.string;

@RequiredArgsConstructor
public class SchemedKvSourceRecordMapper implements KvSourceRecordMapper {

    private static Map<String, ?> sourcePartition = emptyMap();

    private final Function<Map<String, ?>, SchemedKvSourceRecordMapperConfig> configFactory;

    private SchemedKvSourceRecordMapperConfig config;

    private Schema keySchema;

    private Schema valueSchema;

    public SchemedKvSourceRecordMapper() {
        this(SchemedKvSourceRecordMapperConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        config = configFactory.apply(settings);
        keySchema = SchemaBuilder.struct()
                .name("com.github.castorm.kafka.connect.http.Key").doc("Message Key")
                .field(config.getKeyPropertyName(), string().optional().doc("HTTP Item key").build())
                .build();
        valueSchema = SchemaBuilder.struct()
                .name("com.github.castorm.kafka.connect.http.Value").doc("Message Value")
                .field(config.getValuePropertyName(), string().doc("HTTP Item value").build())
                .build();
    }

    @Override
    public SourceRecord map(KvRecord record) {

        Struct key = keyStruct(record.getKey());
        Struct value = valueStruct(record.getValue());
        Offset offset = record.getOffset();

        return new SourceRecord(
                sourcePartition,
                offset.toMap(),
                config.getTopic(),
                null,
                key.schema(),
                key,
                value.schema(),
                value,
                offset.getTimestamp().map(Instant::toEpochMilli).orElseGet(System::currentTimeMillis));
    }

    private Struct keyStruct(String key) {
        return new Struct(keySchema).put(config.getKeyPropertyName(), key);
    }

    private Struct valueStruct(String body) {
        return new Struct(valueSchema).put(config.getValuePropertyName(), body);
    }
}
