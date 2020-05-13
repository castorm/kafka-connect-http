package com.github.castorm.kafka.connect.throttle;

/*-
 * #%L
 * kafka-connect-http-plugin
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
