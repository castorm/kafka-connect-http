package com.github.castorm.kafka.connect.http.response.jackson;

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

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class JacksonHttpResponseParserConfig extends AbstractConfig {

    private static final String ITEMS_POINTER = "http.source.response.json.items.pointer";
    private static final String ITEM_KEY_POINTER = "http.source.response.json.item.key.pointer";
    private static final String ITEM_VALUE_POINTER = "http.source.response.json.item.value.pointer";
    private static final String ITEM_TIMESTAMP_POINTER = "http.source.response.json.item.timestamp.pointer";
    private static final String ITEM_OFFSET_VALUE_POINTER = "http.source.response.json.item.offset.value.pointer";
    private static final String ITEM_OFFSET_KEY = "http.source.response.json.item.offset.key";

    private final JsonPointer itemsPointer;
    private final Optional<JsonPointer> itemKeyPointer;
    private final JsonPointer itemValuePointer;
    private final Optional<JsonPointer> itemTimestampPointer;
    private final Map<String, JsonPointer> itemOffsets;

    JacksonHttpResponseParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        itemsPointer = JsonPointer.compile(getString(ITEMS_POINTER));
        itemKeyPointer = ofNullable(getString(ITEM_KEY_POINTER)).map(JsonPointer::compile);
        itemValuePointer = JsonPointer.compile(getString(ITEM_VALUE_POINTER));
        itemTimestampPointer = ofNullable(getString(ITEM_TIMESTAMP_POINTER)).map(JsonPointer::compile);

        List<JsonPointer> itemOffsetValuePointers = Stream.of(getString(ITEM_OFFSET_VALUE_POINTER).split(","))
                .map(String::trim)
                .map(JsonPointer::compile)
                .collect(toList());
        List<String> itemOffsetKeys = Stream.of(getString(ITEM_OFFSET_KEY).split(","))
                .map(String::trim)
                .collect(toList());

        if (itemOffsetKeys.size() != itemOffsetValuePointers.size()) {
            throw new IllegalStateException("Size of " + ITEM_OFFSET_KEY + " and " + ITEM_OFFSET_VALUE_POINTER + " must coincide");
        }

        itemOffsets = range(0, itemOffsetKeys.size()).boxed()
                .map(i -> new SimpleEntry<>(itemOffsetKeys.get(i), itemOffsetValuePointers.get(i)))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(ITEMS_POINTER, STRING, "/", HIGH, "Items JsonPointer")
                .define(ITEM_KEY_POINTER, STRING, null, HIGH, "Item Key JsonPointer")
                .define(ITEM_VALUE_POINTER, STRING, "/", HIGH, "Item Value JsonPointer")
                .define(ITEM_TIMESTAMP_POINTER, STRING, null, HIGH, "Item Timestamp JsonPointer")
                .define(ITEM_OFFSET_VALUE_POINTER, STRING, HIGH, "Item Offset Value JsonPointer")
                .define(ITEM_OFFSET_KEY, STRING, HIGH, "Item Offset Key");
    }
}
