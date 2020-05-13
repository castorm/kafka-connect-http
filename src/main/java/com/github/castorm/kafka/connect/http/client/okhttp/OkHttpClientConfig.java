package com.github.castorm.kafka.connect.http.client.okhttp;

/*-
 * #%L
 * kafka-connect-http-plugin
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

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.INT;
import static org.apache.kafka.common.config.ConfigDef.Type.LONG;

@Getter
public class OkHttpClientConfig extends AbstractConfig {

    private static final String CONNECTION_TIMEOUT_MILLIS = "http.client.connection.timeout.millis";
    private static final String READ_TIMEOUT_MILLIS = "http.client.read.timeout.millis";
    private static final String CONNECTION_KEEP_ALIVE_DURATION_MILLIS = "http.client.ttl.millis";
    private static final String CONNECTION_MAX_IDLE = "http.client.max-idle";

    private final Long connectionTimeoutMillis;
    private final Long readTimeoutMillis;
    private final Long keepAliveDuration;
    private final Integer maxIdleConnections;

    OkHttpClientConfig(Map<String, ?> originals) {
        super(config(), originals);
        connectionTimeoutMillis = getLong(CONNECTION_TIMEOUT_MILLIS);
        readTimeoutMillis = getLong(READ_TIMEOUT_MILLIS);
        keepAliveDuration = getLong(CONNECTION_KEEP_ALIVE_DURATION_MILLIS);
        maxIdleConnections = getInt(CONNECTION_MAX_IDLE);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(CONNECTION_TIMEOUT_MILLIS, LONG, 2000, HIGH, "Connection Timeout Millis")
                .define(READ_TIMEOUT_MILLIS, LONG, 2000, HIGH, "Read Timeout Millis")
                .define(CONNECTION_KEEP_ALIVE_DURATION_MILLIS, LONG, 300000, HIGH, "Keep Alive Duration Millis")
                .define(CONNECTION_MAX_IDLE, INT, 1, HIGH, "Max Idle Connections");
    }
}
