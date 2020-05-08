package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * kafka-connect-http-plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
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
    public SourceRecord map(HttpResponseItem item) {

        Struct key = keyStruct(item.getKey());
        Struct value = valueStruct(item.getValue());

        return new SourceRecord(
                sourcePartition,
                item.getOffset(),
                topic,
                null,
                key.schema(),
                key,
                value.schema(),
                value,
                item.getTimestamp().toEpochMilli());
    }

    private static Struct keyStruct(String key) {
        return new Struct(SCHEMA_KEY).put(SCHEMA_KEY_KEY, key);
    }

    private static Struct valueStruct(String body) {
        return new Struct(SCHEMA_VALUE).put(SCHEMA_VALUE_BODY, body);
    }
}
