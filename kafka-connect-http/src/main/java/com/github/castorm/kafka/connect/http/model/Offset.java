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
import java.util.Optional;

import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
public class Offset {

    private static final String KEY_KEY = "key";

    private static final String TIMESTAMP_KEY = "timestamp";

    private static final String ENDPOINT_KEY = "endpoint";

    private final Map<String, ?> properties;

    private Offset(Map<String, ?> properties) {
        this.properties = properties;
    }

    public static Map<String, ?> getPartition(String endpoint) {
         return Map.of("endpoint", endpoint);
    }

    public Map<String, ?> getPartition() {
        return Map.of("endpoint", getEndpoint().get());
    }

    public static Offset of(Map<String, ?> properties) {
        return new Offset(properties);
    }

    public static Offset of(Map<String, ?> properties, String key, String endpoint) {
        Map<String, Object> props = new HashMap<>(properties);
        props.put(KEY_KEY, key);
        props.put(ENDPOINT_KEY, endpoint);
        return new Offset(props);
    }

    public static Offset of(Map<String, ?> properties, String key, Instant timestamp, String endpoint) {
        Map<String, Object> props = new HashMap<>(properties);
        props.put(KEY_KEY, key);
        props.put(TIMESTAMP_KEY, timestamp.toString());
        props.put(ENDPOINT_KEY, endpoint);
        return new Offset(props);
    }

    public Map<String, ?> toMap() {
        return properties;
    }

    public Optional<String> getKey() {
        return ofNullable((String) properties.get(KEY_KEY));
    }

    public Optional<Instant> getTimestamp() {
        return ofNullable((String) properties.get(TIMESTAMP_KEY)).map(Instant::parse);
    }

    public Optional<String> getEndpoint() {
        return ofNullable((String) properties.get(ENDPOINT_KEY));
    }
}
