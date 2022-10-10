package com.github.castorm.kafka.connect.timer;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.github.castorm.kafka.connect.timer.AdaptableIntervalTimerTest.Fixture.intervalMillis;
import static com.github.castorm.kafka.connect.timer.AdaptableIntervalTimerTest.Fixture.now;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AdaptableIntervalTimerTest {

    AdaptableIntervalTimer timer;

    @Mock
    AdaptableIntervalTimerConfig config;

    @Mock
    FixedIntervalTimer tailTimer;

    @Mock
    FixedIntervalTimer catchupTimer;

    @BeforeEach
    void setUp() {
        given(tailTimer.getIntervalMillis()).willReturn(intervalMillis);
        given(config.getTailTimer()).willReturn(tailTimer);
        given(config.getCatchupTimer()).willReturn(catchupTimer);
        timer = new AdaptableIntervalTimer(__ -> config);
        timer.configure(emptyMap());
    }

    @Test
    void givenFirst_whenGetRemainingMillis_thenCatchupTimer() {

        timer.getRemainingMillis();

        then(tailTimer).should(never()).getRemainingMillis();
        then(catchupTimer).should().getRemainingMillis();
    }

    @Test
    void givenNewRecordsLastNotLongAgo_whenGetRemainingMillis_thenTailTimer() {

        timer.reset(now.minus(intervalMillis - 1, MILLIS));
        timer.reset(now);

        timer.getRemainingMillis();

        then(tailTimer).should().getRemainingMillis();
        then(catchupTimer).should(never()).getRemainingMillis();
    }

    @Test
    void givenNewRecordsLastLongAgo_whenGetRemainingMillis_thenCatchupTimer() {

        timer.reset(now.minus(intervalMillis + 2, MILLIS));
        timer.reset(now.minus(intervalMillis + 1, MILLIS));

        timer.getRemainingMillis();

        then(catchupTimer).should().getRemainingMillis();
        then(tailTimer).should(never()).getRemainingMillis();
    }

    @Test
    void givenNoNewRecordsLastNotLongAgo_whenGetRemainingMillis_thenTailTimer() {

        timer.reset(now.minus(intervalMillis - 1, MILLIS));
        timer.reset(now.minus(intervalMillis - 1, MILLIS));

        timer.getRemainingMillis();

        then(catchupTimer).should(never()).getRemainingMillis();
        then(tailTimer).should().getRemainingMillis();
    }

    @Test
    void givenNoNewRecordsLastLongAgo_whenGetRemainingMillis_thenTailTimer() {

        timer.reset(now.minus(intervalMillis + 1, MILLIS));
        timer.reset(now.minus(intervalMillis + 1, MILLIS));

        timer.getRemainingMillis();

        then(catchupTimer).should(never()).getRemainingMillis();
        then(tailTimer).should().getRemainingMillis();
    }

    @Test
    void whenReset_thenTimersReset() {

        timer.reset(now);

        then(catchupTimer).should().reset(now);
        then(tailTimer).should().reset(now);
    }

    interface Fixture {
        Instant now = now();
        long intervalMillis = 60000L;
    }
}
