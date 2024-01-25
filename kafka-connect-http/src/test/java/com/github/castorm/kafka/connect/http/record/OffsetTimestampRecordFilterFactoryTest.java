package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.github.castorm.kafka.connect.http.record.OffsetTimestampRecordFilterFactoryTest.Fixture.key;
import static com.github.castorm.kafka.connect.http.record.OffsetTimestampRecordFilterFactoryTest.Fixture.now;
import static com.github.castorm.kafka.connect.http.record.OffsetTimestampRecordFilterFactoryTest.Fixture.endpoint;
import static com.github.castorm.kafka.connect.http.record.OffsetTimestampRecordFilterFactoryTest.Fixture.record;
import static edu.emory.mathcs.backport.java.util.Collections.emptyMap;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OffsetTimestampRecordFilterFactoryTest {

    OffsetTimestampRecordFilterFactory factory = new OffsetTimestampRecordFilterFactory();

    @Test
    void givenOffset_whenTestEarlier_thenFalse() {
        assertThat(factory.create(Offset.of(emptyMap(), key, now)).test(record(now.minus(1, MINUTES)))).isFalse();
    }

    @Test
    void givenOffset_whenTestLater_thenTrue() {
        assertThat(factory.create(Offset.of(emptyMap(), key, now)).test(record(now.plus(1, MINUTES)))).isTrue();
    }

    interface Fixture {
        String key = "key";
        Instant now = now();

        String endpoint = "endpoint";

        static SourceRecord record(Instant timestamp) {
            return new SourceRecord(null, null, null, null, null, null, null, null, timestamp.toEpochMilli());
        }
    }
}
