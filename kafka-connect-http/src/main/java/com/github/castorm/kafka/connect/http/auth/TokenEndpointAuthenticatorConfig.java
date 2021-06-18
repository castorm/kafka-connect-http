package com.github.castorm.kafka.connect.http.auth;

import java.util.Map;

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.types.Password;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class TokenEndpointAuthenticatorConfig extends AbstractConfig {
    private static final String AUTH_ENDPOINT = "http.auth.uri";
    private static final String USER = "http.auth.user";
    private static final String PASSWORD = "http.auth.password";

    private final String user;
    private final Password password;
    private final String authEndpoint;

    public TokenEndpointAuthenticatorConfig(Map<?, ?> originals) {
        super(config(), originals);
        user = getString(USER);
        password = getPassword(PASSWORD);
        authEndpoint = getString(AUTH_ENDPOINT);
    }

    public static ConfigDef config() {
        return new ConfigDef().define(USER, STRING, "", HIGH, "Username")
                .define(PASSWORD, ConfigDef.Type.PASSWORD, "", HIGH, "Password")
                .define(AUTH_ENDPOINT, STRING, "/auth", HIGH, "Auth endpoint");
    }

}
