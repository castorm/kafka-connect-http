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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.model.KvRecord;
import com.github.castorm.kafka.connect.http.response.spi.KvRecordHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.UUID.nameUUIDFromBytes;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class JacksonKvRecordHttpResponseParser implements KvRecordHttpResponseParser {

    private final Function<Map<String, ?>, JacksonKvRecordHttpResponseParserConfig> configFactory;

    private JacksonRecordParser recordParser;

    private TimestampParser timestampParser;

    public JacksonKvRecordHttpResponseParser() {
        this(JacksonKvRecordHttpResponseParserConfig::new);
    }

    @Override
    public void configure(Map<String, ?> configs) {
        JacksonKvRecordHttpResponseParserConfig config = configFactory.apply(configs);
        recordParser = config.getRecordParser();
        timestampParser = config.getTimestampParser();
    }

    @Override
    public List<KvRecord> parse(HttpResponse response) {
        return recordParser.getRecords(response.getBody())
                .map(this::map)
                .collect(toList());
    }

    private KvRecord map(JsonNode node) {

        String key = recordParser.getKey(node).orElseGet(() -> generateConsistentKey(node));
        Optional<Instant> timestamp = recordParser.getTimestamp(node).map(timestampParser::parse);
        Map<String, Object> offsets = recordParser.getOffsets(node);

        Offset offset = timestamp
                .map(ts -> Offset.of(offsets, key, ts))
                .orElseGet(() -> Offset.of(offsets, key));

        return KvRecord.builder()
                .key(key)
                .value(recordParser.getValue(node))
                .offset(offset)
                .build();
    }

    private String generateConsistentKey(JsonNode node) {
        return nameUUIDFromBytes(node.toString().getBytes()).toString();
    }
}
