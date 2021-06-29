package com.github.castorm.kafka.connect.http.auth;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 - 2021 Cástor Rodríguez
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.castorm.kafka.connect.http.auth.spi.HttpAuthenticator;

import org.apache.kafka.connect.errors.RetriableException;

import org.apache.kafka.connect.errors.ConnectException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TokenEndpointAuthenticator implements HttpAuthenticator {
    private final Function<Map<String, ?>, TokenEndpointAuthenticatorConfig> configFactory;
    private TokenEndpointAuthenticatorConfig config;

    public TokenEndpointAuthenticator() {
        this(TokenEndpointAuthenticatorConfig::new);
    }

    public TokenEndpointAuthenticator(Function<Map<String, ?>, TokenEndpointAuthenticatorConfig> configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        this.config = configFactory.apply(configs);
    }

    @Override
    public Optional<String> getAuthorizationHeader() {
        String credentialsBody = config.getAuthBody().value();
        RequestBody requestBody = RequestBody.create(credentialsBody,
                MediaType.parse("application/json; charset=utf-8"));
        String response = execute(requestBody);

        ObjectMapper objectMapper = new ObjectMapper();

        String accessToken;
        try {
            accessToken = objectMapper.readTree(response).path(config.getTokenKeyPath()).asText();
        } catch (JsonProcessingException e) {
            throw new RetriableException("Error: " + e.getMessage(), e);
        }

        if (accessToken.isBlank()) {
            throw new RetriableException("Error: No access token found at " + config.getTokenKeyPath());
        }

        return Optional.of("Bearer " + accessToken);
    }

    private String execute(RequestBody requestBody) {
        OkHttpClient httpClient = new OkHttpClient();
        try {
            Request request = new Request.Builder().url(config.getAuthUrl()).post(requestBody).build();
            Response response = httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RetriableException("Error: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ConnectException("Error: " + e.getMessage(), e);
        }
    }

}
