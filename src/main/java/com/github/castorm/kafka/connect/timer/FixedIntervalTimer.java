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

import com.github.castorm.kafka.connect.timer.spi.Timer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;

@Slf4j
public class FixedIntervalTimer implements Timer {

    private final Function<Map<String, ?>, FixedIntervalTimerConfig> configFactory;

    @Getter
    private Long intervalMillis;

    private Long lastPollMillis;

    public FixedIntervalTimer() {
        this(FixedIntervalTimerConfig::new);
    }

    FixedIntervalTimer(Function<Map<String, ?>, FixedIntervalTimerConfig> configFactory) {
        this(configFactory, System::currentTimeMillis);
    }

    FixedIntervalTimer(Function<Map<String, ?>, FixedIntervalTimerConfig> configFactory, Supplier<Long> lastPollMillisInitializer) {
        this.configFactory = configFactory;
        this.lastPollMillis = lastPollMillisInitializer.get();
    }

    @Override
    public void configure(Map<String, ?> settings) {
        FixedIntervalTimerConfig config = configFactory.apply(settings);
        intervalMillis = config.getPollIntervalMillis();
    }

    @Override
    public Long getRemainingMillis() {
        long now = currentTimeMillis();
        long sinceLastMillis = now - lastPollMillis;
        return max(intervalMillis - sinceLastMillis, 0);
    }

    @Override
    public void reset(Instant lastZero) {
        lastPollMillis = currentTimeMillis();
    }
}
