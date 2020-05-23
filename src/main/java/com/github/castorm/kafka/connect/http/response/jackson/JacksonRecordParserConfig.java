package com.github.castorm.kafka.connect.http.response.jackson;

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

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownList;
import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.MEDIUM;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class JacksonRecordParserConfig extends AbstractConfig {

    private static final String LIST_POINTER = "http.response.list.pointer";
    private static final String ITEM_POINTER = "http.response.record.pointer";
    private static final String ITEM_KEY_POINTER = "http.response.record.key.pointer";
    private static final String ITEM_TIMESTAMP_POINTER = "http.response.record.timestamp.pointer";
    private static final String ITEM_OFFSET_VALUE_POINTER = "http.response.record.offset.pointer";

    private final JsonPointer recordsPointer;
    private final List<JsonPointer> keyPointer;
    private final JsonPointer valuePointer;
    private final Optional<JsonPointer> timestampPointer;
    private final Map<String, JsonPointer> offsetPointers;

    JacksonRecordParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        recordsPointer = compile(getString(LIST_POINTER));
        keyPointer = breakDownList(ofNullable(getString(ITEM_KEY_POINTER)).orElse("")).stream().map(JsonPointer::compile).collect(Collectors.toList());
        valuePointer = compile(getString(ITEM_POINTER));
        timestampPointer = ofNullable(getString(ITEM_TIMESTAMP_POINTER)).map(JsonPointer::compile);
        offsetPointers = breakDownMap(getString(ITEM_OFFSET_VALUE_POINTER)).entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey(), compile(entry.getValue())))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(LIST_POINTER, STRING, "/", HIGH, "Item List JsonPointer")
                .define(ITEM_POINTER, STRING, "/", HIGH, "Item JsonPointer")
                .define(ITEM_KEY_POINTER, STRING, null, HIGH, "Item Key JsonPointers")
                .define(ITEM_TIMESTAMP_POINTER, STRING, null, MEDIUM, "Item Timestamp JsonPointer")
                .define(ITEM_OFFSET_VALUE_POINTER, STRING, "", MEDIUM, "Item Offset JsonPointers");
    }
}
