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
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.github.castorm.kafka.connect.http.record.SimpleKvSourceRecordMapperTest.Fixture.now;
import static com.github.castorm.kafka.connect.http.record.SimpleKvSourceRecordMapperTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.record.SimpleKvSourceRecordMapperTest.Fixture.record;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleKvSourceRecordMapperTest {

    SimpleKvSourceRecordMapper factory;

    @BeforeEach
    void setUp() {
        factory = new SimpleKvSourceRecordMapper();
    }

    @Test
    void givenTopic_whenMap_thenTopicMapped() {

        factory.configure(ImmutableMap.of("kafka.topic", "topic"));

        assertThat(factory.map(record).topic()).isEqualTo("topic");
    }

    @Test
    void givenKey_whenMap_thenIdMapped() {
        assertThat(((Struct) factory.map(record.withKey("value")).key()).get("key")).isEqualTo("value");
    }

    @Test
    void givenValue_whenMap_thenBodyMapped() {
        assertThat(((Struct) factory.map(record.withValue("value")).value()).get("body")).isEqualTo("value");
    }

    @Test
    void givenOffset_whenMap_thenOffsetMapped() {
        assertThat(factory.map(record.withOffset(offset)).sourceOffset()).isEqualTo(offset.toMap());
    }

    @Test
    void givenTimestamp_whenMap_thenTimestampMapped() {
        assertThat(factory.map(record.withOffset(offset)).timestamp()).isEqualTo(now.toEpochMilli());
    }

    @Test
    void whenMap_thenNoPartitionMapped() {
        assertThat(factory.map(record).kafkaPartition()).isNull();
    }

    @Test
    void whenMap_thenKeySchemaMapped() {
        assertThat(factory.map(record).keySchema()).isNotNull();
    }

    @Test
    void whenMap_thenValueSchemaMapped() {
        assertThat(factory.map(record).valueSchema()).isNotNull();
    }

    interface Fixture {
        Instant now = now();
        Offset offset = Offset.of(ImmutableMap.of("k", "v"), now);
        KvRecord record = KvRecord.builder().value("not-null").offset(offset).build();
    }
}
