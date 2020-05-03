package com.github.castorm.kafka.connect.http.record;

import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapperTest.Fixture.item;
import static com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapperTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapperTest.Fixture.value;
import static org.assertj.core.api.Assertions.assertThat;

class SchemedSourceRecordMapperTest {

    SchemedSourceRecordMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SchemedSourceRecordMapper();
    }

    @Test
    void givenTopic_whenMap_thenTopicMapped() {

        mapper.configure(ImmutableMap.of("kafka.topic", "topic"));

        assertThat(mapper.map(item).topic()).isEqualTo("topic");
    }

    @Test
    void givenKey_whenMap_thenIdMapped() {
        assertThat(((Struct) mapper.map(item.withKey(value)).key()).get("id")).isEqualTo(value);
    }

    @Test
    void givenValue_whenMap_thenBodyMapped() {
        assertThat(((Struct) mapper.map(item.withValue(value)).value()).get("body")).isEqualTo(value);
    }

    @Test
    void givenOffset_whenMap_thenOffsetMapped() {
        assertThat(mapper.map(item.withOffset(offset)).sourceOffset()).isEqualTo(offset);
    }

    @Test
    void givenTimestamp_whenMap_thenTimestampMapped() {
        assertThat(mapper.map(item.withTimestamp(42L)).timestamp()).isEqualTo(42L);
    }

    @Test
    void whenMap_thenNoPartitionMapped() {
        assertThat(mapper.map(item).kafkaPartition()).isNull();
    }

    @Test
    void whenMap_thenKeySchemaMapped() {
        assertThat(mapper.map(item).keySchema()).isNotNull();
    }

    @Test
    void whenMap_thenValueSchemaMapped() {
        assertThat(mapper.map(item).valueSchema()).isNotNull();
    }

    interface Fixture {
        String value = "value";
        HttpResponseItem item = HttpResponseItem.builder().value(value).build();
        ImmutableMap<String, Object> offset = ImmutableMap.of("k", "v");
    }
}
