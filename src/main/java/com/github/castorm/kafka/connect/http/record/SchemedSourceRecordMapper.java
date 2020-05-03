package com.github.castorm.kafka.connect.http.record;

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

    private static final String KEY_ID = "id";
    private static final Schema SCHEMA_KEY = SchemaBuilder.struct()
            .name("com.theworkshop.kafka.connect.http.basic.Key")
            .doc("Message Key used for Kafka partitioning")
            .field(KEY_ID, string().optional().doc("id").build())
            .build();

    private static final String VALUE_BODY = "body";
    private static final Schema SCHEMA_VALUE = SchemaBuilder.struct()
            .name("com.theworkshop.kafka.connect.http.basic.Value")
            .doc("Message Value")
            .field(VALUE_BODY, string().doc("Response body").build())
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
                item.getTimestamp());
    }

    private static Struct keyStruct(String key) {
        return new Struct(SCHEMA_KEY).put(KEY_ID, key);
    }

    private static Struct valueStruct(String body) {
        return new Struct(SCHEMA_VALUE).put(VALUE_BODY, body);
    }
}
