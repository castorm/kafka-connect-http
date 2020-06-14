package com.github.castorm.kafka.connect.timer;

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

class AdaptableIntervalTimerConfigTest {

    @Test
    void whenTailIntervalMillis_thenDefault() {
        assertThat(config(emptyMap()).getTailTimer().getIntervalMillis()).isEqualTo(60000L);
    }

    @Test
    void whenTailIntervalMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.timer.interval.millis", "42")).getTailTimer().getIntervalMillis()).isEqualTo(42L);
    }

    @Test
    void whenDeprecatedTailIntervalMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.throttler.interval.millis", "42")).getTailTimer().getIntervalMillis()).isEqualTo(42L);
    }

    @Test
    void whenCatchupIntervalMillis_thenDefault() {
        assertThat(config(emptyMap()).getCatchupTimer().getIntervalMillis()).isEqualTo(30000L);
    }

    @Test
    void whenCatchupIntervalMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.timer.catchup.interval.millis", "73")).getCatchupTimer().getIntervalMillis()).isEqualTo(73L);
    }

    @Test
    void whenDeprecatedCatchupIntervalMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.throttler.catchup.interval.millis", "73")).getCatchupTimer().getIntervalMillis()).isEqualTo(73L);
    }

    private static AdaptableIntervalTimerConfig config(Map<String, Object> settings) {
        return new AdaptableIntervalTimerConfig(settings);
    }
}
