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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.github.castorm.kafka.connect.http.record.SchemedKvSourceRecordMapperTest.Fixture.now;
import static com.github.castorm.kafka.connect.http.record.SchemedKvSourceRecordMapperTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.record.SchemedKvSourceRecordMapperTest.Fixture.record;
import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SchemedKvSourceRecordMapperTest {

    SchemedKvSourceRecordMapper mapper;

    @Mock
    SourceRecordMapperConfig config;

    @BeforeEach
    void setUp() {
        given(config.getTopicName("dummy-endpoint")).willReturn("topic");
        mapper = new SchemedKvSourceRecordMapper(__ -> config);
        mapper.configure(emptyMap());
    }

    @Test
    void givenTopic_whenMap_thenTopicMapped() {
        assertThat(mapper.map("dummy-endpoint", record).topic()).isEqualTo("topic");
    }

    @Test
    void givenKey_whenMap_thenKeyMapped() {
        assertThat(((Struct) mapper.map("dummy-endpoint", record.withKey("key")).key()).get("_streamkap_key")).isEqualTo("key");
    }

    @Test
    void givenValue_whenMap_thenValueMapped() {
        assertThat(((Struct) mapper.map("dummy-endpoint", record.withValue("value")).value()).get("_streamkap_value")).isEqualTo("value");
    }

    @Test
    void givenKey_whenMap_thenValueKeyMapped() {
        assertThat(((Struct) mapper.map("dummy-endpoint", record.withKey("key")).value()).get("_streamkap_key")).isEqualTo("key");
    }

    @Test
    void givenOffsetTimestamp_whenMap_thenValueTimestampMapped() {
        assertThat(((Struct) mapper.map("dummy-endpoint", record.withOffset(offset)).value()).get("_streamkap_timestamp")).isEqualTo(now.toEpochMilli());
    }

    @Test
    void givenOffset_whenMap_thenOffsetMapped() {
        assertThat(mapper.map("dummy-endpoint", record.withOffset(offset)).sourceOffset()).isEqualTo(offset.toMap());
    }

    @Test
    void givenTimestamp_whenMap_thenTimestampMapped() {
        assertThat(mapper.map("dummy-endpoint", record.withOffset(offset)).timestamp()).isEqualTo(now.toEpochMilli());
    }

    @Test
    void whenMap_thenNoPartitionMapped() {
        assertThat(mapper.map("dummy-endpoint", record).kafkaPartition()).isNull();
    }

    @Test
    void whenMap_thenKeySchemaMapped() {
        assertThat(mapper.map("dummy-endpoint", record).keySchema()).isNotNull();
    }

    @Test
    void whenMap_thenValueSchemaMapped() {
        assertThat(mapper.map("dummy-endpoint", record).valueSchema()).isNotNull();
    }

    interface Fixture {
        Instant now = now();
        Offset offset = Offset.of(ImmutableMap.of("k", "v"), "key", now);
        KvRecord record = KvRecord.builder().value("not-null").offset(offset).build();
    }
}
