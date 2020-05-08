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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.common.Configurable;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

@RequiredArgsConstructor
public class JacksonItemParser implements Configurable {

    private static final JsonPointer JSON_ROOT = compile("/");

    private final Function<Map<String, ?>, JacksonItemParserConfig> configFactory;

    private final Supplier<ObjectMapper> objectMapperFactory;

    private ObjectMapper objectMapper;
    private JsonPointer itemsPointer;
    private Optional<JsonPointer> keyPointer;
    private JsonPointer valuePointer;
    private Map<String, JsonPointer> offsetPointers;
    private Optional<JsonPointer> timestampPointer;

    public JacksonItemParser() {
        this(JacksonItemParserConfig::new, ObjectMapper::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        JacksonItemParserConfig config = configFactory.apply(settings);
        objectMapper = objectMapperFactory.get();
        itemsPointer = config.getItemsPointer();
        keyPointer = config.getKeyPointer();
        valuePointer = config.getValuePointer();
        offsetPointers = config.getOffsetPointers();
        timestampPointer = config.getTimestampPointer();
    }

    Stream<JsonNode> getItems(byte[] body) {

        JsonNode deserializedBody = deserialize(body);

        JsonNode items = getRequiredAt(deserializedBody, itemsPointer);

        return items.isArray() ? stream(items.spliterator(), false) : Stream.of(items);
    }

    Optional<String> getKey(JsonNode node) {
        return keyPointer.map(it -> getRequiredAt(node, it).asText());
    }

    String getValue(JsonNode node) {

        JsonNode value = getRequiredAt(node, valuePointer);

        return value.isObject() ? serialize(value) : value.asText();
    }

    Optional<String> getTimestamp(JsonNode node) {
        return timestampPointer
                .map(it -> getRequiredAt(node, it))
                .map(JsonNode::asText);
    }

    Map<String, Object> getOffsets(JsonNode node) {
        return offsetPointers.entrySet().stream().collect(toMap(Entry::getKey, entry -> getRequiredAt(node, entry.getValue()).asText()));
    }

    private static JsonNode getRequiredAt(JsonNode body, JsonPointer itemsPointer) {
        return JSON_ROOT.equals(itemsPointer) ? body : body.requiredAt(itemsPointer);
    }

    @SneakyThrows(IOException.class)
    private JsonNode deserialize(byte[] body) {
        return objectMapper.readTree(body);
    }

    @SneakyThrows(IOException.class)
    private String serialize(JsonNode node) {
        return objectMapper.writeValueAsString(node);
    }
}
