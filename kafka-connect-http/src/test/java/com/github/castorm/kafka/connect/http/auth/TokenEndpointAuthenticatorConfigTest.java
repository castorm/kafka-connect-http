package com.github.castorm.kafka.connect.http.auth;

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
import org.apache.kafka.common.config.types.Password;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class TokenEndpointAuthenticatorConfigTest {

    @Test
    void whenNoPayload_thenDefault() {
        assertThat(config(emptyMap()).getAuthPayload()).isEqualTo(new Password(""));
    }

    @Test
    void whenPayload_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.auth.payload", "{\"foo\": \"bar\"}")).getAuthPayload())
                .isEqualTo(new Password("password"));
    }

    @Test
    void whenAuthEndpoint_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.auth.uri", "http://lol/login")).getAuthUri())
                .isEqualTo("http://lol/login");
    }

    @Test
    void whenNoAuthEndpoint_thenDefault() {
        assertThat(config(emptyMap()).getAuthUri()).isEmpty();
    }

    @Test
    void whenTokenKeyPath_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.auth.tokenkeypath", "/token")).getTokenKeyPath()).isEqualTo("/token");
    }

    @Test
    void whenNoTokenKeyPath_thenDefault() {
        assertThat(config(emptyMap()).getTokenKeyPath()).isEqualTo("/access_token");
    }

    private static TokenEndpointAuthenticatorConfig config(Map<String, String> config) {
        return new TokenEndpointAuthenticatorConfig(config);
    }
}
