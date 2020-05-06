package com.github.castorm.kafka.connect.http.client.okhttp;

/*-
 * #%L
 * kafka-connect-http-plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
                .define(CONNECTION_MAX_IDLE, INT, 5, HIGH, "Max Idle Connections");
    }
}
