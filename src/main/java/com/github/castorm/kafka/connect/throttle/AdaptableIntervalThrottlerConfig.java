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

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.HashMap;
import java.util.Map;

import static com.github.castorm.kafka.connect.throttle.FixedIntervalThrottlerConfig.THROTTLE_INTERVAL_MILLIS;
import static java.util.Collections.emptyMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.LONG;

@Getter
public class AdaptableIntervalThrottlerConfig extends AbstractConfig {

    private static final String TAIL_INTERVAL_MILLIS = "http.throttler.interval.millis";
    private static final String CATCHUP_INTERVAL_MILLIS = "http.throttle.catchup.interval.millis";

    private final FixedIntervalThrottler tailThrottler;
    private final FixedIntervalThrottler catchupThrottler;

    public AdaptableIntervalThrottlerConfig(Map<String, ?> originals) {
        super(config(), originals);
        tailThrottler = new FixedIntervalThrottler(__ -> new FixedIntervalThrottlerConfig(mapOf(THROTTLE_INTERVAL_MILLIS, getLong(TAIL_INTERVAL_MILLIS))));
        tailThrottler.configure(emptyMap());
        catchupThrottler = new FixedIntervalThrottler(__ -> new FixedIntervalThrottlerConfig(mapOf(THROTTLE_INTERVAL_MILLIS, getLong(CATCHUP_INTERVAL_MILLIS))));
        catchupThrottler.configure(emptyMap());
    }

    private static Map<String, ?> mapOf(String key, Long value) {
        return new HashMap<String, String>() {{
            put(key, String.valueOf(value));
        }};
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(TAIL_INTERVAL_MILLIS, LONG, 10000L, HIGH, "Throttle Tail Interval Millis")
                .define(CATCHUP_INTERVAL_MILLIS, LONG, 1000L, HIGH, "Throttle Catchup Interval Millis");
    }
}
