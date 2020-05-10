package com.github.castorm.kafka.connect.throttle;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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
