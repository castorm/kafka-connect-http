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

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import static java.time.Instant.EPOCH;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;

public class AdaptableIntervalTimer implements Timer {

    private final Function<Map<String, ?>, AdaptableIntervalTimerConfig> configFactory;

    private FixedIntervalTimer tailTimer;

    private FixedIntervalTimer catchupTimer;

    private Long intervalMillis;

    private Instant previousLatest = EPOCH;

    private Instant currentLatest = EPOCH.plus(1, MILLIS);

    public AdaptableIntervalTimer() {
        this(AdaptableIntervalTimerConfig::new);
    }

    public AdaptableIntervalTimer(Function<Map<String, ?>, AdaptableIntervalTimerConfig> configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public void configure(Map<String, ?> settings) {
        AdaptableIntervalTimerConfig config = configFactory.apply(settings);
        tailTimer = config.getTailTimer();
        catchupTimer = config.getCatchupTimer();
        intervalMillis = config.getTailTimer().getIntervalMillis();
    }

    @Override
    public void reset(Instant lastZero) {
        tailTimer.reset(lastZero);
        catchupTimer.reset(lastZero);
        previousLatest = currentLatest;
        currentLatest = lastZero;
    }

    @Override
    public Long getRemainingMillis() {
        return resolveTimer().getRemainingMillis();
    }

    private Timer resolveTimer() {
        return isCatchingUp() ? catchupTimer : tailTimer;
    }

    private boolean isCatchingUp() {
        boolean thereWereNewItems = !previousLatest.equals(currentLatest);
        boolean longAgoSinceLastItem = currentLatest.isBefore(now().minus(intervalMillis, MILLIS));
        return thereWereNewItems && longAgoSinceLastItem;
    }
}
