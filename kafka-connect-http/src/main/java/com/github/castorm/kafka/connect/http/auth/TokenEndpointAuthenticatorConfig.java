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
    private static final String AUTH_URI = "http.auth.uri";
    private static final String PAYLOAD = "http.auth.payload";
    private static final String TOKEN_KEY_PATH = "http.auth.tokenkeypath";

    private final String authUri;
    private final Password authPayload;
    private final String tokenKeyPath;

    public TokenEndpointAuthenticatorConfig(Map<?, ?> originals) {
        super(config(), originals);
        authUri = getString(AUTH_URI);
        authPayload = getPassword(PAYLOAD);
        tokenKeyPath = getString(TOKEN_KEY_PATH);

    }

    public static ConfigDef config() {
        return new ConfigDef().define(PAYLOAD, ConfigDef.Type.PASSWORD, "", HIGH, "Auth payload JSON")
                .define(TOKEN_KEY_PATH, STRING, "/access_token", HIGH, "Auth request response token key")
                .define(AUTH_URI, STRING, "", HIGH, "Auth endpoint");
    }

}
