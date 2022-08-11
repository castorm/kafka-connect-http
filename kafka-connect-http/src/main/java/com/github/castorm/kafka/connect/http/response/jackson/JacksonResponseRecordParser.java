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
import com.github.castorm.kafka.connect.http.response.jackson.model.JacksonRecord;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.Configurable;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.castorm.kafka.connect.common.CollectionUtils.merge;

@RequiredArgsConstructor
public class JacksonResponseRecordParser implements Configurable {

    private final Function<Map<String, ?>, JacksonRecordParserConfig> configFactory;

    private final JacksonRecordParser recordParser;

    private final JacksonSerializer serializer;

    private JsonPointer recordsPointer;

    private JsonPointer keyPointer;

    public JacksonResponseRecordParser() {
        this(new JacksonRecordParser(), new JacksonSerializer(new ObjectMapper()));
    }

    public JacksonResponseRecordParser(JacksonRecordParser recordParser, JacksonSerializer serializer) {
        this(JacksonRecordParserConfig::new, recordParser, serializer);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        JacksonRecordParserConfig config = configFactory.apply(settings);
        recordsPointer = config.getRecordsPointer();
        keyPointer = config.getKeyPointer().stream().findFirst().orElseGet(JsonPointer::empty);
    }

    Stream<JacksonRecord> getRecords(byte[] body) {


        AtomicInteger iterator = new AtomicInteger();

        JsonNode jsonBody = serializer.deserialize(body);

        Map<String, Object> responseOffset = getResponseOffset(jsonBody);

        return serializer.getArrayAt(jsonBody, recordsPointer)
                .map(jsonRecord -> toJacksonRecord(jsonRecord, responseOffset, iterator.getAndIncrement()));
    }

    private Map<String, Object> getResponseOffset(JsonNode node) {
        return Map.of("blockId", node.get("BlockId"));
    }

    private String getResponseKey(JsonNode node) {
        return node.get(keyPointer.toString()).asText("");

    }

    private JacksonRecord toJacksonRecord(JsonNode jsonRecord, Map<String, Object> responseOffset, int index) {
        return JacksonRecord.builder()
                .key(getResponseKey(jsonRecord))
                .offset(merge(responseOffset, Map.of("index", index)))
                .body(recordParser.getValue(jsonRecord))
                .build();
    }
}
