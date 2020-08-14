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
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.github.castorm.kafka.connect.http.record.StringKvSourceRecordMapperTest.Fixture.now;
import static com.github.castorm.kafka.connect.http.record.StringKvSourceRecordMapperTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.record.StringKvSourceRecordMapperTest.Fixture.partition;
import static com.github.castorm.kafka.connect.http.record.StringKvSourceRecordMapperTest.Fixture.partitionMap;
import static com.github.castorm.kafka.connect.http.record.StringKvSourceRecordMapperTest.Fixture.record;
import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StringKvSourceRecordMapperTest {

    StringKvSourceRecordMapper mapper;

    @Mock
    SourceRecordMapperConfig config;

    @BeforeEach
    void setUp() {
        given(config.getTopic()).willReturn("topic");
        mapper = new StringKvSourceRecordMapper(__ -> config);
        mapper.configure(emptyMap());
    }

    @Test
    void givenTopic_whenMap_thenTopicMapped() {
        assertThat(mapper.map(record, partition).topic()).isEqualTo("topic");
    }

    @Test
    void givenPartition_whenMap_thenPartitionMapped() {
        assertThat(mapper.map(record, partition).sourcePartition()).isEqualTo(partitionMap);
    }

    @Test
    void givenKey_whenMap_thenIdMapped() {
        assertThat(mapper.map(record.withKey("value"), partition).key()).isEqualTo("value");
    }

    @Test
    void givenValue_whenMap_thenBodyMapped() {
        assertThat(mapper.map(record.withValue("value"), partition).value()).isEqualTo("value");
    }

    @Test
    void givenOffset_whenMap_thenOffsetMapped() {
        assertThat(mapper.map(record.withOffset(offset), partition).sourceOffset()).isEqualTo(offset.toMap());
    }

    @Test
    void givenTimestamp_whenMap_thenTimestampMapped() {
        assertThat(mapper.map(record.withOffset(offset), partition).timestamp()).isEqualTo(now.toEpochMilli());
    }

    @Test
    void whenMap_thenNoPartitionMapped() {
        assertThat(mapper.map(record, partition).kafkaPartition()).isNull();
    }

    @Test
    void whenMap_thenKeySchemaMapped() {
        assertThat(mapper.map(record, partition).keySchema()).isNotNull();
    }

    @Test
    void whenMap_thenValueSchemaMapped() {
        assertThat(mapper.map(record, partition).valueSchema()).isNotNull();
    }

    interface Fixture {
        Instant now = now();
        ImmutableMap<String, String> partitionMap = ImmutableMap.of("p", "v");
        Partition partition = Partition.of(partitionMap);
        Offset offset = Offset.of(ImmutableMap.of("k", "v"), "key", now);
        KvRecord record = KvRecord.builder().value("not-null").offset(offset).build();
    }
}
