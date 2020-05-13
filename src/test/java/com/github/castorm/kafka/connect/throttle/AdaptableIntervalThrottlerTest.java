package com.github.castorm.kafka.connect.throttle;

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
