package com.github.castorm.kafka.connect.throttle;

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
import com.github.castorm.kafka.connect.throttle.FixedIntervalThrottler.Sleeper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.castorm.kafka.connect.throttle.FixedIntervalThrottlerTest.Fixture.intervalMillis;
import static com.github.castorm.kafka.connect.throttle.FixedIntervalThrottlerTest.Fixture.lastPollMillis;
import static com.github.castorm.kafka.connect.throttle.FixedIntervalThrottlerTest.Fixture.offset;
import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FixedIntervalThrottlerTest {

    FixedIntervalThrottler throttler;

    @Mock
    Function<Map<String, ?>, FixedIntervalThrottlerConfig> configFactory;

    @Mock
    FixedIntervalThrottlerConfig config;

    @Mock
    Sleeper sleeper;

    @Mock
    Supplier<Long> lastPollMillisInitializer;

    @Captor
    ArgumentCaptor<Long> sleepMillis;

    @Test
    void givenConfiguredThrottler_whenThrottle_thenSleepForInterval() throws InterruptedException {

        givenConfiguredThrottler(intervalMillis, lastPollMillis);

        throttler.throttle(offset);

        then(sleeper).should().sleep(sleepMillis.capture());

        assertThat(sleepMillis.getValue()).isCloseTo(intervalMillis, offset(intervalMillis / 10));
    }

    @Test
    void givenConfiguredThrottler_whenThrottleAfterInterval_thenDoesntSleep() throws InterruptedException {

        givenConfiguredThrottler(intervalMillis, lastPollMillis - intervalMillis);

        throttler.throttle(offset);

        then(sleeper).should(never()).sleep(any());
    }

    private void givenConfiguredThrottler(long intervalMillis, long lastPollMillis) {
        given(configFactory.apply(any())).willReturn(config);
        given(config.getPollIntervalMillis()).willReturn(intervalMillis);
        given(lastPollMillisInitializer.get()).willReturn(lastPollMillis);
        throttler = new FixedIntervalThrottler(configFactory, sleeper, lastPollMillisInitializer);
        throttler.configure(emptyMap());
    }

    interface Fixture {
        Offset offset = Offset.of(emptyMap(), "key", now());
        long intervalMillis = 60000L;
        long lastPollMillis = System.currentTimeMillis();
    }
}
