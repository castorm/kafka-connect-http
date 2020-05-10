package com.github.castorm.kafka.connect.http.throttle;

/*-
 * #%L
 * kafka-connect-http-plugin
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
import com.github.castorm.kafka.connect.http.throttle.FixedIntervalThrottler.Sleeper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.castorm.kafka.connect.http.throttle.FixedIntervalThrottlerTest.Fixture.intervalMillis;
import static com.github.castorm.kafka.connect.http.throttle.FixedIntervalThrottlerTest.Fixture.lastPollMillis;
import static com.github.castorm.kafka.connect.http.throttle.FixedIntervalThrottlerTest.Fixture.offset;
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
        Offset offset = Offset.of(emptyMap(), now());
        long intervalMillis = 60000L;
        long lastPollMillis = System.currentTimeMillis();
    }
}
