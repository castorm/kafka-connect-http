package com.github.castorm.kafka.connect.http.response;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import com.github.castorm.kafka.connect.http.model.HttpRecord;
import com.github.castorm.kafka.connect.http.model.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.github.castorm.kafka.connect.http.response.OffsetTimestampFilterFactoryTest.Fixture.record;
import static com.github.castorm.kafka.connect.http.response.OffsetTimestampFilterFactoryTest.Fixture.now;
import static edu.emory.mathcs.backport.java.util.Collections.emptyMap;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OffsetTimestampFilterFactoryTest {

    OffsetTimestampFilterFactory factory = new OffsetTimestampFilterFactory();

    @Test
    void givenOffset_whenTestEarlier_thenFalse() {
        assertThat(factory.create(Offset.of(emptyMap(), now)).test(record(now.minus(1, MINUTES)))).isFalse();
    }

    @Test
    void givenOffset_whenTestLater_thenTrue() {
        assertThat(factory.create(Offset.of(emptyMap(), now)).test(record(now.plus(1, MINUTES)))).isTrue();
    }

    interface Fixture {
        Instant now = now();
        static HttpRecord record(Instant timestamp) {
            return HttpRecord.builder().offset(Offset.of(emptyMap(), timestamp)).build();
        }
    }
}
