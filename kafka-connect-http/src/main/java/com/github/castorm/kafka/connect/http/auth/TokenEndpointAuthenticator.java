package com.github.castorm.kafka.connect.http.auth;

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
        String credentialsBody = config.getAuthPayload().value();
        RequestBody requestBody = RequestBody.create(credentialsBody,
                MediaType.parse("applicationjson; charset=utf-8"));
        Request request = new Request.Builder().url(config.getAuthUri()).post(requestBody).build();
        String response = execute(request);

        ObjectMapper objectMapper = new ObjectMapper();

        // TODO: Should be anonymous type to make it generic. but how does that work in
        // java? regex?
        TokenEndpointResponse responseParsed;
        try {
            responseParsed = objectMapper.readValue(response, TokenEndpointResponse.class);
        } catch (JsonProcessingException e) {
            throw new ConnectException("Error: " + e.getMessage(), e);
        }

        return Optional.of("Bearer " + responseParsed.accessToken);
    }

    private String execute(Request request) {
        OkHttpClient httpClient = new OkHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RetriableException(e);
        }
    }

}
