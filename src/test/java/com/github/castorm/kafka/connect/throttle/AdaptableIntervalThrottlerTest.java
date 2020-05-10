package com.github.castorm.kafka.connect.throttle;

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

import com.github.castorm.kafka.connect.http.model.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.github.castorm.kafka.connect.throttle.AdaptableIntervalThrottlerTest.Fixture.intervalMillis;
import static com.github.castorm.kafka.connect.throttle.AdaptableIntervalThrottlerTest.Fixture.now;
import static com.github.castorm.kafka.connect.throttle.AdaptableIntervalThrottlerTest.Fixture.offset;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AdaptableIntervalThrottlerTest {

    AdaptableIntervalThrottler throttler;

    @Mock
    AdaptableIntervalThrottlerConfig config;

    @Mock
    FixedIntervalThrottler tailThrottler;

    @Mock
    FixedIntervalThrottler catchupThrottler;

    @BeforeEach
    void setUp() {
        throttler = new AdaptableIntervalThrottler(__ -> config);
        given(tailThrottler.getIntervalMillis()).willReturn(intervalMillis);
        given(config.getTailThrottler()).willReturn(tailThrottler);
        given(config.getCatchupThrottler()).willReturn(catchupThrottler);
        throttler.configure(emptyMap());
    }

    @Test
    void givenNewRecordsLastNotLongAgo_whenThrottle_thenTailThrottler() throws InterruptedException {

        throttler.throttle(offset(now));

        then(tailThrottler).should().throttle(offset(now));
        then(catchupThrottler).should(never()).throttle(any());
    }

    @Test
    void givenNewRecordsLastLongAgo_whenThrottle_thenCatchupThrottler() throws InterruptedException {

        throttler.throttle(offset(now.minus(intervalMillis, MILLIS)));

        then(catchupThrottler).should().throttle(offset(now.minus(intervalMillis, MILLIS)));
        then(tailThrottler).should(never()).throttle(any());
    }

    @Test
    void givenNoNewRecordsLastNotLongAgo_whenThrottle_thenBothTailThrottler() throws InterruptedException {

        throttler.throttle(offset(now));
        throttler.throttle(offset(now));

        then(tailThrottler).should(times(2)).throttle(offset(now));
    }

    @Test
    void givenNoNewRecordsLastLongAgo_whenThrottle_thenOneCatchupOneTailThrottler() throws InterruptedException {

        throttler.throttle(offset(now.minus(intervalMillis, MILLIS)));
        throttler.throttle(offset(now.minus(intervalMillis, MILLIS)));

        then(catchupThrottler).should().throttle(offset(now.minus(intervalMillis, MILLIS)));
        then(tailThrottler).should().throttle(offset(now.minus(intervalMillis, MILLIS)));
    }

    interface Fixture {
        Instant now = now();
        long intervalMillis = 60000L;

        static Offset offset(Instant now) {
            return Offset.of(emptyMap(), now);
        }
    }
}
