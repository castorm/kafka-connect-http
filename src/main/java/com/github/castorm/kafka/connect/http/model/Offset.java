package com.github.castorm.kafka.connect.http.model;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.EPOCH;

@ToString
@EqualsAndHashCode
public class Offset {

    private static final String TIMESTAMP_KEY = "timestamp";

    private final Map<String, ?> properties;

    private Offset(Map<String, ?> properties) {
        this.properties = properties;
    }

    public static Offset of(Map<String, ?> properties) {
        Map<String, Object> props = new HashMap<>(properties);
        if (!properties.containsKey(TIMESTAMP_KEY)) {
            props.put(TIMESTAMP_KEY, EPOCH.toString());
        }
        return new Offset(props);
    }

    public static Offset of(Map<String, ?> properties, Instant timestamp) {
        Map<String, Object> props = new HashMap<>(properties);
        props.put(TIMESTAMP_KEY, timestamp.toString());
        return new Offset(props);
    }

    public Map<String, ?> toMap() {
        return properties;
    }

    public Instant getTimestamp() {
        return Instant.parse((CharSequence) properties.get(TIMESTAMP_KEY));
    }
}
