package com.github.castorm.kafka.connect.http.auth;

/*-
 * #%L
 * kafka-connect-http
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

import com.github.castorm.kafka.connect.http.auth.spi.HttpAuthenticationType;
import com.github.castorm.kafka.connect.http.auth.spi.HttpAuthenticator;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.MEDIUM;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class ConfigurableHttpAuthenticatorConfig extends AbstractConfig {

    private static final String AUTH_TYPE = "http.auth.type";

    private final HttpAuthenticator authenticator;

    ConfigurableHttpAuthenticatorConfig(Map<String, ?> originals) {
        super(config(), originals);
        authenticator = getAuthenticator(originals);
    }

    private HttpAuthenticator getAuthenticator(Map<String, ?> originals) {
        switch (HttpAuthenticationType.valueOf(getString(AUTH_TYPE).toUpperCase())) {
            case BASIC:
                BasicHttpAuthenticator auth = new BasicHttpAuthenticator();
                auth.configure(originals);
                return auth;
            default:
                return new NoneHttpAuthenticator();
        }
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(AUTH_TYPE, STRING, HttpAuthenticationType.NONE.name(), MEDIUM, "Authentication Type");
    }
}
