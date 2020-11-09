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

import com.github.castorm.kafka.connect.http.auth.spi.HttpAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConfigurableHttpAuthenticatorTest {

    @Mock
    ConfigurableHttpAuthenticatorConfig config;

    @Mock
    HttpAuthenticator delegate;

    ConfigurableHttpAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new ConfigurableHttpAuthenticator(__ -> config);
    }

    @Test
    void whenHeader_thenDelegated() {

        given(config.getAuthenticator()).willReturn(delegate);
        given(delegate.getAuthorizationHeader()).willReturn(Optional.of("header"));

        authenticator.configure(emptyMap());

        assertThat(authenticator.getAuthorizationHeader()).contains("header");
    }
}
