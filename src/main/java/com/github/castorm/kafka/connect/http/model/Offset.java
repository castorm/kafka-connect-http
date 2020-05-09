package com.github.castorm.kafka.connect.http.model;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.EPOCH;

@EqualsAndHashCode
public class Offset {

    private static final String TIMESTAMP_ISO_KEY = "timestamp_iso";

    private final Map<String, ?> properties;

    private Offset(Map<String, ?> properties) {
        this.properties = properties;
    }

    public static Offset of(Map<String, ?> properties, Instant timestamp) {
        Map<String, Object> props = new HashMap<>(properties);
        props.put(TIMESTAMP_ISO_KEY, timestamp.toString());
        return new Offset(props);
    }

    public static Offset of(Map<String, ?> properties) {
        Map<String, Object> props = new HashMap<>(properties);
        if (props.containsKey(TIMESTAMP_ISO_KEY)) {
            Instant.parse((CharSequence) props.get(TIMESTAMP_ISO_KEY));
        } else {
            props.put(TIMESTAMP_ISO_KEY, EPOCH.toString());
        }
        return new Offset(props);
    }

    public Map<String, ?> toMap() {
        return properties;
    }

    public Instant getTimestamp() {
        return Instant.parse((CharSequence) properties.get(TIMESTAMP_ISO_KEY));
    }
}
