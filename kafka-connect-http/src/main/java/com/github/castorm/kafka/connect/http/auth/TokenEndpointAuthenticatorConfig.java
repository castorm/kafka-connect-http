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

import java.util.Map;

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.types.Password;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class TokenEndpointAuthenticatorConfig extends AbstractConfig {
    private static final String AUTH_URL = "http.auth.url";
    private static final String AUTH_BODY = "http.auth.body";
    private static final String TOKEN_KEY_PATH = "http.auth.tokenkeypath";

    private final String authUrl;
    private final Password authBody;
    private final String tokenKeyPath;

    public TokenEndpointAuthenticatorConfig(Map<?, ?> originals) {
        super(config(), originals);
        authUrl = getString(AUTH_URL);
        authBody = getPassword(AUTH_BODY);
        tokenKeyPath = getString(TOKEN_KEY_PATH);

    }

    public static ConfigDef config() {
        return new ConfigDef().define(AUTH_BODY, ConfigDef.Type.PASSWORD, "", HIGH, "Auth payload JSON")
                .define(TOKEN_KEY_PATH, STRING, "access_token", HIGH, "Auth request response token key")
                .define(AUTH_URL, STRING, "", HIGH, "Auth endpoint");
    }

}
