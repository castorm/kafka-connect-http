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
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonPointer.compile;
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
        return getRequiredAt(node, pointer).asLong();
    }

    Map<String, Object> getOffset(JsonNode node, Map<String, JsonPointer> pointers) {
        return pointers.entrySet().stream().collect(toMap(Entry::getKey, entry -> node.at(entry.getValue()).asText()));
    }

    private static JsonNode getRequiredAt(JsonNode body, JsonPointer itemsPointer) {
        return JSON_ROOT.equals(itemsPointer) ? body : body.requiredAt(itemsPointer);
    }
}
