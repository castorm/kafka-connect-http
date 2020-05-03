package com.github.castorm.kafka.connect.http.response.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

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
    private final JsonPointer itemKeyPointer;
    private final JsonPointer itemValuePointer;
    private final JsonPointer itemTimestampPointer;
    private final Map<String, JsonPointer> itemOffsets;

    JacksonHttpResponseParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        itemsPointer = JsonPointer.compile(getString(ITEMS_POINTER));
        itemKeyPointer = JsonPointer.compile(getString(ITEM_KEY_POINTER));
        itemValuePointer = JsonPointer.compile(getString(ITEM_VALUE_POINTER));
        itemTimestampPointer = JsonPointer.compile(getString(ITEM_TIMESTAMP_POINTER));

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
                .define(ITEM_KEY_POINTER, STRING, HIGH, "Item Key JsonPointer")
                .define(ITEM_VALUE_POINTER, STRING, "/", HIGH, "Item Value JsonPointer")
                .define(ITEM_TIMESTAMP_POINTER, STRING, "/timestamp", HIGH, "Item Timestamp JsonPointer")
                .define(ITEM_OFFSET_VALUE_POINTER, STRING, HIGH, "Item Offset Value JsonPointer")
                .define(ITEM_OFFSET_KEY, STRING, HIGH, "Item Offset Key");
    }
}
