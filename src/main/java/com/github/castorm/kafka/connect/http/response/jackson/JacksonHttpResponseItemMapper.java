package com.github.castorm.kafka.connect.http.response.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

class JacksonHttpResponseItemMapper {

    private static final JsonPointer JSON_ROOT = compile("/");

    Stream<JsonNode> getItems(JsonNode body, JsonPointer pointer) {

        JsonNode items = getRequiredAt(body, pointer);

        return items.isArray() ? stream(items.spliterator(), false) : Stream.of(items);
    }

    String getKey(JsonNode node, JsonPointer pointer) {
        return getRequiredAt(node, pointer).asText();
    }

    JsonNode getValue(JsonNode node, JsonPointer pointer) {
        return getRequiredAt(node, pointer);
    }

    Long getTimestamp(JsonNode node, JsonPointer pointer) {
        JsonNode value = getAt(node, pointer);
        if (!value.isMissingNode()) {
            return value.asLong();
        }
        return currentTimeMillis();
    }

    Map<String, Object> getOffset(JsonNode node, Map<String, JsonPointer> pointers) {
        return pointers.entrySet().stream().collect(toMap(Entry::getKey, entry -> node.at(entry.getValue()).asText()));
    }

    private static JsonNode getRequiredAt(JsonNode body, JsonPointer itemsPointer) {
        return JSON_ROOT.equals(itemsPointer) ? body : body.requiredAt(itemsPointer);
    }

    private static JsonNode getAt(JsonNode body, JsonPointer itemsPointer) {
        return JSON_ROOT.equals(itemsPointer) ? body : body.at(itemsPointer);
    }
}
