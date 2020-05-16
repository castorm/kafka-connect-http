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

import com.github.castorm.kafka.connect.http.model.HttpRecord;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordMapper;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.apache.kafka.connect.data.SchemaBuilder.string;

public class SchemedSourceRecordMapper implements SourceRecordMapper {

    private static final String SCHEMA_KEY_KEY = "key";
    private static final Schema SCHEMA_KEY = SchemaBuilder.struct()
            .name("com.github.castorm.kafka.connect.http.Key")
            .doc("Message Key")
            .field(SCHEMA_KEY_KEY, string().optional().doc("HTTP Item identifier").build())
            .build();

    private static final String SCHEMA_VALUE_BODY = "body";
    private static final Schema SCHEMA_VALUE = SchemaBuilder.struct()
            .name("com.github.castorm.kafka.connect.http.Value")
            .doc("Message Value")
            .field(SCHEMA_VALUE_BODY, string().doc("HTTP Item response body").build())
            .build();

    private Map<String, ?> sourcePartition = emptyMap();

    private String topic;

    @Override
    public void configure(Map<String, ?> configs) {
        SchemedSourceRecordMapperConfig config = new SchemedSourceRecordMapperConfig(configs);
        topic = config.getTopic();
    }

    @Override
    public SourceRecord map(HttpRecord record) {

        Struct key = keyStruct(record.getKey());
        Struct value = valueStruct(record.getValue());
        Offset offset = record.getOffset();

        return new SourceRecord(
                sourcePartition,
                offset.toMap(),
                topic,
                null,
                key.schema(),
                key,
                value.schema(),
                value,
                offset.getTimestamp().toEpochMilli());
    }

    private static Struct keyStruct(String key) {
        return new Struct(SCHEMA_KEY).put(SCHEMA_KEY_KEY, key);
    }

    private static Struct valueStruct(String body) {
        return new Struct(SCHEMA_VALUE).put(SCHEMA_VALUE_BODY, body);
    }
}
