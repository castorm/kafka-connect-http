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

import org.apache.kafka.common.config.types.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.net.ConnectException;

@ExtendWith(MockitoExtension.class)
class TokenEndpointAuthenticatorTest {

    @Mock
    TokenEndpointAuthenticatorConfig config;

    TokenEndpointAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new TokenEndpointAuthenticator(__ -> config);
    }

    @Test
    void whenCredentials_thenHeader() {

        given(config.getAuthUri()).willReturn("http://dkcph-pmstapp1:9001/Auth");
        given(config.getTokenKeyPath()).willReturn("/accessToken");
        given(config.getAuthPayload())
                .willReturn(new Password("{  \"login\": \"mamor\",  \"password\": \"Something Secret\"}"));

        authenticator.configure(emptyMap());

        assertThat(authenticator.getAuthorizationHeader()).contains("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.");
    }

    @Test
    void whenNoCredentials_thenHeaderEmpty() {

        given(config.getAuthUri()).willReturn("");
        given(config.getTokenKeyPath()).willReturn("");
        given(config.getAuthPayload()).willReturn(new Password(""));

        authenticator.configure(emptyMap());

        assertThatThrownBy(() -> authenticator.getAuthorizationHeader()).isInstanceOf(ConnectException.class);

    }
}
