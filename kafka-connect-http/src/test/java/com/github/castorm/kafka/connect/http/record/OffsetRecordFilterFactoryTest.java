package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * Kafka Connect HTTP
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
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;

import static com.github.castorm.kafka.connect.http.record.OffsetRecordFilterFactoryTest.Fixture.record;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OffsetRecordFilterFactoryTest {

    OffsetRecordFilterFactory factory;

    @Mock
    SourceRecordFilterFactory delegate;

    @BeforeEach
    void setUp() {
        given(delegate.create(any())).willReturn(__ -> false);
        factory = new OffsetRecordFilterFactory(delegate);
    }

    @Test
    void givenNotSeen_whenCreateAndTest_thenFalse() {
        assertThat(factory.create(Offset.of(ImmutableMap.of("i", 5), "dummy-endpoint")).test(record(3))).isFalse();
    }

    @Test
    void givenSeen_whenCreateAndTest_thenTrue() {

        Predicate<SourceRecord> predicate = factory.create(Offset.of(ImmutableMap.of("i", 5)));

        predicate.test(record(5));

        assertThat(predicate.test(record(6))).isTrue();
    }

    interface Fixture {
        static SourceRecord record(int endpoint) {
            return new SourceRecord(null, ImmutableMap.of("i", endpoint), null, null, null, null, null, null, 0L);
        }
    }
}
