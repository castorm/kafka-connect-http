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
        assertThat(((Struct) mapper.map(item.withKey(value)).key()).get("key")).isEqualTo(value);
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
