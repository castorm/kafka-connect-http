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

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.HashMap;
import java.util.Map;

import static com.github.castorm.kafka.connect.timer.FixedIntervalTimerConfig.TIMER_INTERVAL_MILLIS;
import static java.util.Collections.emptyMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.LONG;

@Getter
public class AdaptableIntervalTimerConfig extends AbstractConfig {

    @Deprecated
    private static final String DEPRECATED_TAIL_INTERVAL_MILLIS = "http.throttler.interval.millis";
    @Deprecated
    private static final String DEPRECATED_CATCHUP_INTERVAL_MILLIS = "http.throttler.catchup.interval.millis";

    private static final String TAIL_INTERVAL_MILLIS = "http.timer.interval.millis";
    private static final String CATCHUP_INTERVAL_MILLIS = "http.timer.catchup.interval.millis";
    private static final long DEFAULT_TAIL_INTERVAL_MILLIS = 60000L;
    private static final long DEFAULT_CATCHUP_INTERVAL_MILLIS = 30000L;

    private final FixedIntervalTimer tailTimer;
    private final FixedIntervalTimer catchupTimer;

    public AdaptableIntervalTimerConfig(Map<String, ?> originals) {
        super(config(), originals);
        Long tailIntervalMillis = getFromPropertyOrDeprecatedProperty(TAIL_INTERVAL_MILLIS, DEPRECATED_TAIL_INTERVAL_MILLIS, DEFAULT_TAIL_INTERVAL_MILLIS);
        Long catchupIntervalMillis = getFromPropertyOrDeprecatedProperty(CATCHUP_INTERVAL_MILLIS, DEPRECATED_CATCHUP_INTERVAL_MILLIS, DEFAULT_CATCHUP_INTERVAL_MILLIS);
        tailTimer = new FixedIntervalTimer(__ -> new FixedIntervalTimerConfig(mapOf(TIMER_INTERVAL_MILLIS, tailIntervalMillis)));
        tailTimer.configure(emptyMap());
        catchupTimer = new FixedIntervalTimer(__ -> new FixedIntervalTimerConfig(mapOf(TIMER_INTERVAL_MILLIS, catchupIntervalMillis)));
        catchupTimer.configure(emptyMap());
    }

    private Long getFromPropertyOrDeprecatedProperty(String property, String deprecatedProperty, Long defaultValue) {
        Long value = getLong(property);
        if (!value.equals(defaultValue)) {
            return value;
        } else {
            return getLong(deprecatedProperty);
        }
    }

    private static Map<String, ?> mapOf(String key, Long value) {
        return new HashMap<String, String>() {{
            put(key, String.valueOf(value));
        }};
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(DEPRECATED_TAIL_INTERVAL_MILLIS, LONG, DEFAULT_TAIL_INTERVAL_MILLIS, HIGH, "(Deprecated) Throttle Tail Interval Millis")
                .define(TAIL_INTERVAL_MILLIS, LONG, DEFAULT_TAIL_INTERVAL_MILLIS, HIGH, "Timer Tail Interval Millis")
                .define(DEPRECATED_CATCHUP_INTERVAL_MILLIS, LONG, DEFAULT_CATCHUP_INTERVAL_MILLIS, HIGH, "(Deprecated) Throttle Catchup Interval Millis")
                .define(CATCHUP_INTERVAL_MILLIS, LONG, DEFAULT_CATCHUP_INTERVAL_MILLIS, HIGH, "Timer Catchup Interval Millis");
    }
}
