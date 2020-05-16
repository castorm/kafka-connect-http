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
import com.github.castorm.kafka.connect.throttle.spi.Throttler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;

@Slf4j
public class FixedIntervalThrottler implements Throttler {

    private final Function<Map<String, ?>, FixedIntervalThrottlerConfig> configFactory;

    private final Sleeper sleeper;

    @Getter
    private Long intervalMillis;

    private Long lastPollMillis;

    public FixedIntervalThrottler() {
        this(FixedIntervalThrottlerConfig::new);
    }

    FixedIntervalThrottler(Function<Map<String, ?>, FixedIntervalThrottlerConfig> configFactory) {
        this(configFactory, Thread::sleep, System::currentTimeMillis);
    }

    FixedIntervalThrottler(Function<Map<String, ?>, FixedIntervalThrottlerConfig> configFactory, Sleeper sleeper, Supplier<Long> lastPollMillisInitializer) {
        this.configFactory = configFactory;
        this.sleeper = sleeper;
        this.lastPollMillis = lastPollMillisInitializer.get();
    }

    @Override
    public void configure(Map<String, ?> settings) {
        FixedIntervalThrottlerConfig config = configFactory.apply(settings);
        intervalMillis = config.getPollIntervalMillis();
    }

    @Override
    public void throttle(Offset offset) throws InterruptedException {
        long now = currentTimeMillis();
        long sinceLastPollMillis = now - lastPollMillis;
        long sleepMillis = intervalMillis - sinceLastPollMillis;
        if (sleepMillis > 0) {
            sleeper.sleep(sleepMillis);
        }
        lastPollMillis = currentTimeMillis();
    }

    @FunctionalInterface
    public interface Sleeper {

        void sleep(Long milliseconds) throws InterruptedException;
    }
}
