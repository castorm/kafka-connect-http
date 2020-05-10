package com.github.castorm.kafka.connect.throttle;

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
