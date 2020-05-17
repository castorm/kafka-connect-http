package com.github.castorm.kafka.connect.http.response;

/*-
 * #%L
 * Kafka Connect HTTP
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
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeHttpResponsePolicyConfigTest {

    @Test
    void whenNoDelegate_thenDefault() {
        assertThat(config(emptyMap()).getProcessCodes()).containsExactlyInAnyOrder(rangeClosed(200, 299).boxed().distinct().toArray(Integer[]::new));
    }

    @Test
    void whenDelegate_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.policy.codes.process", "200..201")).getProcessCodes()).containsExactlyInAnyOrder(200, 201);
    }

    @Test
    void whenNoPolicy_thenDefault() {
        assertThat(config(emptyMap()).getSkipCodes()).containsExactlyInAnyOrder(rangeClosed(300, 399).boxed().distinct().toArray(Integer[]::new));
    }

    @Test
    void whenPolicy_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.policy.codes.skip", "300..301")).getSkipCodes()).containsExactlyInAnyOrder(300, 301);
    }

    private static StatusCodeHttpResponsePolicyConfig config(Map<String, Object> settings) {
        return new StatusCodeHttpResponsePolicyConfig(settings);
    }
}
