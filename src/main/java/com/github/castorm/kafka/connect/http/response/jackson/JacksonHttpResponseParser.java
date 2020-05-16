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
import com.github.castorm.kafka.connect.http.model.HttpRecord;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class JacksonHttpResponseParser implements HttpResponseParser {

    private final Function<Map<String, ?>, JacksonHttpResponseParserConfig> configFactory;

    private JacksonHttpRecordParser recordParser;

    private TimestampParser timestampParser;

    public JacksonHttpResponseParser() {
        this(JacksonHttpResponseParserConfig::new);
    }

    JacksonHttpResponseParser(Function<Map<String, ?>, JacksonHttpResponseParserConfig> configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        JacksonHttpResponseParserConfig config = configFactory.apply(configs);
        recordParser = config.getRecordParser();
        timestampParser = config.getTimestampParser();
    }

    @Override
    public List<HttpRecord> parse(HttpResponse response) {

        return recordParser.getRecords(response.getBody())
                .map(this::mapToRecord)
                .collect(toList());
    }

    private HttpRecord mapToRecord(JsonNode node) {

        Instant timestamp = recordParser.getTimestamp(node)
                .map(timestampParser::parse)
                .orElseGet(Instant::now);

        return HttpRecord.builder()
                .key(recordParser.getKey(node).orElseGet(() -> randomUUID().toString()))
                .value(recordParser.getValue(node))
                .offset(Offset.of(recordParser.getOffsets(node), timestamp))
                .build();
    }
}
