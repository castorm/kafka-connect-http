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

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.types.Password;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class BasicHttpAuthenticatorConfig extends AbstractConfig {

    private static final String USER = "http.auth.user";
    private static final String PASSWORD = "http.auth.password";

    private final String user;
    private final Password password;

    BasicHttpAuthenticatorConfig(Map<String, ?> originals) {
        super(config(), originals);
        user = getString(USER);
        password = getPassword(PASSWORD);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(USER, STRING, "", HIGH, "Username")
                .define(PASSWORD, ConfigDef.Type.PASSWORD, "", HIGH, "Password");
    }
}
