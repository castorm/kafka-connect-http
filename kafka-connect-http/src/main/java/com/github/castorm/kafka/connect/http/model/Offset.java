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

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
public class Offset {

    private static final String KEY_KEY = "key";

    private static final String IDENTIFIER_KEY = "identifier";

    private final Map<String, ?> properties;

    private Offset(Map<String, ?> properties) {
        this.properties = properties;
    }

    public static Offset of(Map<String, ?> properties) {
        return new Offset(properties);
    }

    public static Offset of(Map<String, ?> properties, BigInteger key) {
        Map<String, Object> props = new HashMap<>(properties);
        props.put(KEY_KEY, key);
        return new Offset(props);
    }

    public static Offset of(Map<String, ?> properties, BigInteger key, int identifier) {
        Map<String, Object> props = new HashMap<>(properties);
        props.put(KEY_KEY, key);
        props.put(IDENTIFIER_KEY, identifier);
        return new Offset(props);
    }

    public Map<String, ?> toMap() {
        return properties;
    }

    public Optional<String> getKey() {
        return ofNullable((String) properties.get(KEY_KEY));
    }
}


