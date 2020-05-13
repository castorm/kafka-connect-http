package com.github.castorm.kafka.connect.http.response.jackson;

/*-
 * #%L
 * kafka-connect-http-plugin
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
public class JacksonHttpRecordParser implements Configurable {

    private static final JsonPointer JSON_ROOT = compile("/");

    private final Function<Map<String, ?>, JacksonHttpRecordParserConfig> configFactory;

    private final Supplier<ObjectMapper> objectMapperFactory;

    private ObjectMapper objectMapper;
    private JsonPointer recordsPointer;
    private Optional<JsonPointer> keyPointer;
    private JsonPointer valuePointer;
    private Map<String, JsonPointer> offsetPointers;
    private Optional<JsonPointer> timestampPointer;

    public JacksonHttpRecordParser() {
        this(JacksonHttpRecordParserConfig::new, ObjectMapper::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        JacksonHttpRecordParserConfig config = configFactory.apply(settings);
        objectMapper = objectMapperFactory.get();
        recordsPointer = config.getRecordsPointer();
        keyPointer = config.getKeyPointer();
        valuePointer = config.getValuePointer();
        offsetPointers = config.getOffsetPointers();
        timestampPointer = config.getTimestampPointer();
    }

    Stream<JsonNode> getRecords(byte[] body) {

        JsonNode deserializedBody = deserialize(body);

        JsonNode records = getRequiredAt(deserializedBody, recordsPointer);

        return records.isArray() ? stream(records.spliterator(), false) : Stream.of(records);
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

    private static JsonNode getRequiredAt(JsonNode body, JsonPointer recordsPointer) {
        return JSON_ROOT.equals(recordsPointer) ? body : body.requiredAt(recordsPointer);
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
