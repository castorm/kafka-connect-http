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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class FixedIntervalThrottlerConfigTest {

    @Test
    void whenPollIntervalMillis_thenDefault() {
        assertThat(new FixedIntervalThrottlerConfig(emptyMap()).getPollIntervalMillis()).isEqualTo(10000L);
    }

    @Test
    void whenPollIntervalMillis_thenInitialized() {
        assertThat(new FixedIntervalThrottlerConfig(ImmutableMap.of("http.throttler.interval.millis", "42")).getPollIntervalMillis()).isEqualTo(42L);
    }
}
