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
import org.apache.kafka.connect.errors.ConnectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TokenEndpointAuthenticatorTest {

    @Mock
    TokenEndpointAuthenticatorConfig config;
    TokenEndpointAuthenticator authenticator;
    MockWebServer webServer;
    MockResponse response;

    @BeforeEach
    void setUp() {
        authenticator = new TokenEndpointAuthenticator(__ -> config);
        webServer = new MockWebServer();
        response = new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8").setBody(
                "{\"accessToken\":\"someRandomJwtTokenHeader.someLongTokenIThatProbablyDoesntLookLikeThisAtAll\",\"expiresIn\":123456}");
    }

    @Test
    void whenCredentials_thenAccessToken() {

        webServer.enqueue(response);

        given(config.getAuthUrl()).willReturn(webServer.url("/Auth").toString());
        given(config.getTokenKeyPath()).willReturn("accessToken");
        given(config.getAuthBody())
                .willReturn(new Password("{  \"login\": \"myUser\",  \"password\": \"myPassword\"}"));

        authenticator.configure(emptyMap());

        assertThat(authenticator.getAuthorizationHeader().toString())
                .contains("Bearer someRandomJwtTokenHeader.");
    }

    @Test
    void whenNoCredentials_thenException() {

        given(config.getAuthUrl()).willReturn("http://google.com/");
        given(config.getAuthBody()).willReturn(new Password(""));

        authenticator.configure(emptyMap());

        assertThatThrownBy(() -> authenticator.getAuthorizationHeader()).isInstanceOf(ConnectException.class);

    }

    @Test
    void whenInvalidUri_thenException() {

        given(config.getAuthUrl()).willReturn("this makes no sense");
        given(config.getAuthBody())
                .willReturn(new Password("{  \"login\": \"myUser\",  \"password\": \"myPassword\"}"));

        authenticator.configure(emptyMap());

        assertThatThrownBy(() -> authenticator.getAuthorizationHeader()).isInstanceOf(ConnectException.class);

    }
}
