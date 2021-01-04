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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.Configurable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class JacksonRecordParser implements Configurable {

    private final Function<Map<String, ?>, JacksonRecordParserConfig> configFactory;

    private final JacksonSerializer serializer;

    private List<JsonPointer> keyPointer;
    private Optional<JsonPointer> timestampPointer;
    private Map<String, JsonPointer> offsetPointers;
    private JsonPointer valuePointer;

    public JacksonRecordParser() {
        this(new JacksonSerializer(new ObjectMapper()));
    }

    public JacksonRecordParser(JacksonSerializer serializer) {
        this(JacksonRecordParserConfig::new, serializer);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        JacksonRecordParserConfig config = configFactory.apply(settings);
        keyPointer = config.getKeyPointer();
        valuePointer = config.getValuePointer();
        offsetPointers = config.getOffsetPointers();
        timestampPointer = config.getTimestampPointer();
    }

    /**
     * @deprecated Replaced by Offset
     */
    @Deprecated
    Optional<String> getKey(JsonNode node) {
        String key = keyPointer.stream()
                .map(pointer -> serializer.getObjectAt(node, pointer).asText())
                .filter(it -> !it.isEmpty())
                .collect(joining("+"));
        return key.isEmpty() ? Optional.empty() : Optional.of(key);
    }

    /**
     * @deprecated Replaced by Offset
     */
    @Deprecated
    Optional<String> getTimestamp(JsonNode node) {
        return timestampPointer.map(pointer -> serializer.getObjectAt(node, pointer).asText());
    }

    Map<String, Object> getOffset(JsonNode node) {
        return offsetPointers.entrySet().stream()
                .collect(toMap(Entry::getKey, entry -> serializer.getObjectAt(node, entry.getValue()).asText()));
    }

    String getValue(JsonNode node) {

        JsonNode value = serializer.getObjectAt(node, valuePointer);

        return value.isObject() ? serializer.serialize(value) : value.asText();
    }
}
