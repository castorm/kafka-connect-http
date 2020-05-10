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

import java.util.Map;
import java.util.function.Function;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyMap;

public class AdaptableIntervalThrottler implements Throttler {

    private final Function<Map<String, ?>, AdaptableIntervalThrottlerConfig> configFactory;

    private Throttler tailThrottler;

    private Throttler catchupThrottler;

    private Long intervalMillis;

    private Offset lastOffset = Offset.of(emptyMap());

    public AdaptableIntervalThrottler() {
        this(AdaptableIntervalThrottlerConfig::new);
    }

    public AdaptableIntervalThrottler(Function<Map<String, ?>, AdaptableIntervalThrottlerConfig> configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public void configure(Map<String, ?> settings) {
        AdaptableIntervalThrottlerConfig config = configFactory.apply(settings);
        tailThrottler = config.getTailThrottler();
        catchupThrottler = config.getCatchupThrottler();
        intervalMillis = config.getTailThrottler().getIntervalMillis();
    }

    @Override
    public void throttle(Offset offset) throws InterruptedException {
        resolveThrottler(offset).throttle(offset);
        lastOffset = offset;
    }

    private Throttler resolveThrottler(Offset offset) {
        if (isCatchingUp(offset)) {
            return catchupThrottler;
        } else {
            return tailThrottler;
        }
    }

    private boolean isCatchingUp(Offset offset) {
        boolean thereWereNewItems = !lastOffset.getTimestamp().equals(offset.getTimestamp());
        boolean longAgoSinceLastItem = offset.getTimestamp().isBefore(now().minus(intervalMillis, MILLIS));
        return thereWereNewItems && longAgoSinceLastItem;
    }
}
