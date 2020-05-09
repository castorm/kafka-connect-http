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

import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.model.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.github.castorm.kafka.connect.http.response.OffsetFilterFactoryTest.Fixture.item;
import static com.github.castorm.kafka.connect.http.response.OffsetFilterFactoryTest.Fixture.now;
import static edu.emory.mathcs.backport.java.util.Collections.emptyMap;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OffsetFilterFactoryTest {

    OffsetFilterFactory factory = new OffsetFilterFactory();

    @Test
    void givenOffset_whenTestEarlier_thenFalse() {
        assertThat(factory.create(Offset.of(emptyMap(), now)).test(item(now.minus(1, MINUTES)))).isFalse();
    }

    @Test
    void givenOffset_whenTestLater_thenTrue() {
        assertThat(factory.create(Offset.of(emptyMap(), now)).test(item(now.plus(1, MINUTES)))).isTrue();
    }

    interface Fixture {
        Instant now = now();
        static HttpResponseItem item(Instant timestamp) {
            return HttpResponseItem.builder().offset(Offset.of(emptyMap(), timestamp)).build();
        }
    }
}
