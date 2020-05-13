package com.github.castorm.kafka.connect.throttle;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class AdaptableIntervalThrottlerConfigTest {

    @Test
    void whenTailIntervalMillis_thenDefault() {
        assertThat(config(emptyMap()).getTailThrottler().getIntervalMillis()).isEqualTo(10000L);
    }

    @Test
    void whenTailIntervalMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.throttler.interval.millis", "42")).getTailThrottler().getIntervalMillis()).isEqualTo(42L);
    }

    @Test
    void whenCatchupIntervalMillis_thenDefault() {
        assertThat(config(emptyMap()).getCatchupThrottler().getIntervalMillis()).isEqualTo(1000L);
    }

    @Test
    void whenCatchupIntervalMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.throttle.catchup.interval.millis", "73")).getCatchupThrottler().getIntervalMillis()).isEqualTo(73L);
    }

    private static AdaptableIntervalThrottlerConfig config(Map<String, Object> settings) {
        return new AdaptableIntervalThrottlerConfig(settings);
    }
}
