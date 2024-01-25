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
import com.github.castorm.kafka.connect.http.model.Partition;
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
import static org.apache.kafka.connect.data.SchemaBuilder.int64;
import static org.apache.kafka.connect.data.SchemaBuilder.string;

@RequiredArgsConstructor
public class SchemedKvSourceRecordMapper implements KvSourceRecordMapper {

    private static final String KEY_FIELD_NAME = "_streamkap_key";
    private static final String VALUE_FIELD_NAME = "_streamkap_value";
    private static final String TIMESTAMP_FIELD_NAME = "_streamkap_timestamp";
    // TODO change ednpoint field name (maybe create new Mapper or extend current class with a specific implementation)
    private static final String ENDPOINT_FIELD_NAME = "_streamkap_index";

    private final Function<Map<String, ?>, SourceRecordMapperConfig> configFactory;

    private SourceRecordMapperConfig config;

    private Schema keySchema;

    private Schema valueSchema;

    public SchemedKvSourceRecordMapper() {
        this(SourceRecordMapperConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        config = configFactory.apply(settings);
        keySchema = SchemaBuilder.struct()
                .name("com.github.castorm.kafka.connect.http.Key").doc("Message Key")
                .field(KEY_FIELD_NAME, string().optional().doc("HTTP Record Key").build())
                .build();
        valueSchema = SchemaBuilder.struct()
                .name("com.github.castorm.kafka.connect.http.Value").doc("Message Value")
                .field(VALUE_FIELD_NAME, string().doc("HTTP Record Value").build())
                .field(KEY_FIELD_NAME, string().optional().doc("HTTP Record Key").build())
                .field(ENDPOINT_FIELD_NAME, string().optional().doc("HTTP Record Value Endpoint").build())
                .field(TIMESTAMP_FIELD_NAME, int64().optional().doc("HTTP Record Timestamp").build())
                .build();
    }

    @Override
    public SourceRecord map(String endpoint, KvRecord record) {

        Offset offset = record.getOffset();
        Long timestamp = offset.getTimestamp().map(Instant::toEpochMilli).orElseGet(System::currentTimeMillis);

        Struct key = keyStruct(record.getKey());
        Struct value = valueStruct(record.getKey(), record.getValue(), timestamp, endpoint);
        Map<String, ?> sourcePartition = Partition.getPartition(endpoint);

        return new SourceRecord(
                sourcePartition,
                offset.toMap(),
                config.getTopicName(endpoint),
                null,
                key.schema(),
                key,
                value.schema(),
                value,
                timestamp);
    }

    private Struct keyStruct(String key) {
        return new Struct(keySchema).put(KEY_FIELD_NAME, key);
    }

    private Struct valueStruct(String key, String value, Long timestamp, String endpoint) {
        return new Struct(valueSchema)
                .put(KEY_FIELD_NAME, key)
                .put(VALUE_FIELD_NAME, value)
                .put(TIMESTAMP_FIELD_NAME, timestamp)
                .put(ENDPOINT_FIELD_NAME, endpoint);
    }
}
