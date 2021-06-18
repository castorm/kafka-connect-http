package com.github.castorm.kafka.connect.http.auth;

import com.github.castorm.kafka.connect.http.auth.spi.HttpAuthenticator;

import org.apache.commons.lang.NotImplementedException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TokenEndpointAuthenticator implements HttpAuthenticator {
    private final Function<Map<String, ?>, TokenEndpointAuthenticatorConfig> configFactory;

    public TokenEndpointAuthenticator() {
        this(TokenEndpointAuthenticatorConfig::new);
    }

    public TokenEndpointAuthenticator(Function<Map<String, ?>, TokenEndpointAuthenticatorConfig> configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public Optional<String> getAuthorizationHeader() {
        // TODO This is where we get the token via a request
        throw new NotImplementedException();
    }
}
