package com.github.castorm.kafka.connect.timer;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.castorm.kafka.connect.timer.FixedIntervalTimerTest.Fixture.intervalMillis;
import static com.github.castorm.kafka.connect.timer.FixedIntervalTimerTest.Fixture.lastPollMillis;
import static com.github.castorm.kafka.connect.timer.FixedIntervalTimerTest.Fixture.maxExecutionTimeMillis;
import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FixedIntervalTimerTest {

    FixedIntervalTimer timer;

    @Mock
    Function<Map<String, ?>, FixedIntervalTimerConfig> configFactory;

    @Mock
    FixedIntervalTimerConfig config;

    @Mock
    Supplier<Long> lastPollMillisInitializer;

    @Test
    void givenConfiguredTimer_whenGetRemainingMillis_thenInterval() {

        givenConfiguredTimer(intervalMillis, lastPollMillis);

        assertThat(timer.getRemainingMillis()).isCloseTo(intervalMillis, offset(maxExecutionTimeMillis));
    }

    @Test
    void givenConfiguredTimer_whenGetRemainingMillisAfterInterval_thenZero() {

        givenConfiguredTimer(intervalMillis, lastPollMillis - intervalMillis);

        assertThat(timer.getRemainingMillis()).isEqualTo(0);
    }

    private void givenConfiguredTimer(long intervalMillis, long lastPollMillis) {
        given(configFactory.apply(any())).willReturn(config);
        given(config.getPollIntervalMillis()).willReturn(intervalMillis);
        given(lastPollMillisInitializer.get()).willReturn(lastPollMillis);
        timer = new FixedIntervalTimer(configFactory, lastPollMillisInitializer);
        timer.configure(emptyMap());
    }

    interface Fixture {
        Offset offset = Offset.of(emptyMap(), "key", now());
        long intervalMillis = 300000L;
        long lastPollMillis = System.currentTimeMillis();
        long maxExecutionTimeMillis = 500L;
    }
}
