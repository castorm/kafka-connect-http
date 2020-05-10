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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.common.MapUtils.breakDownMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.MEDIUM;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class JacksonHttpRecordParserConfig extends AbstractConfig {

    private static final String ITEMS_POINTER = "http.response.records.pointer";
    private static final String ITEM_KEY_POINTER = "http.response.record.key.pointer";
    private static final String ITEM_VALUE_POINTER = "http.response.record.value.pointer";
    private static final String ITEM_TIMESTAMP_POINTER = "http.response.record.timestamp.pointer";
    private static final String ITEM_OFFSET_VALUE_POINTER = "http.response.record.offset.pointer";

    private final JsonPointer recordsPointer;
    private final Optional<JsonPointer> keyPointer;
    private final JsonPointer valuePointer;
    private final Optional<JsonPointer> timestampPointer;
    private final Map<String, JsonPointer> offsetPointers;

    JacksonHttpRecordParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        recordsPointer = compile(getString(ITEMS_POINTER));
        keyPointer = ofNullable(getString(ITEM_KEY_POINTER)).map(JsonPointer::compile);
        valuePointer = compile(getString(ITEM_VALUE_POINTER));
        timestampPointer = ofNullable(getString(ITEM_TIMESTAMP_POINTER)).map(JsonPointer::compile);
        offsetPointers = breakDownMap(getString(ITEM_OFFSET_VALUE_POINTER)).entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey(), compile(entry.getValue())))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(ITEMS_POINTER, STRING, "/", HIGH, "Items JsonPointer")
                .define(ITEM_KEY_POINTER, STRING, null, HIGH, "Item Key JsonPointer")
                .define(ITEM_VALUE_POINTER, STRING, "/", HIGH, "Item Value JsonPointer")
                .define(ITEM_TIMESTAMP_POINTER, STRING, null, MEDIUM, "Item Timestamp JsonPointer")
                .define(ITEM_OFFSET_VALUE_POINTER, STRING, "", MEDIUM, "Item Offset JsonPointers");
    }
}
