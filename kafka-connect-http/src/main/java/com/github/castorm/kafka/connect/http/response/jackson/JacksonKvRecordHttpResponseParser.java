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

import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.model.KvRecord;
import com.github.castorm.kafka.connect.http.response.jackson.model.JacksonRecord;
import com.github.castorm.kafka.connect.http.response.spi.KvRecordHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
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

    private JacksonResponseRecordParser responseParser;

    public JacksonKvRecordHttpResponseParser() {
        this(JacksonKvRecordHttpResponseParserConfig::new);
    }

    @Override
    public void configure(Map<String, ?> configs) {
        JacksonKvRecordHttpResponseParserConfig config = configFactory.apply(configs);
        responseParser = config.getResponseParser();
    }

    @Override
    public List<KvRecord> parse(HttpResponse response) {
        return responseParser.getRecords(response.getBody())
                .map(this::map)
                .collect(toList());
    }

    private KvRecord map(JacksonRecord record) {

        Map<String, Object> offsets = record.getOffset();



        Offset offset =  Offset.of(offsets);

        return KvRecord.builder()
                .key(record.getKey())
                .value(record.getBody())
                .offset(offset)
                .build();
    }

    private static String generateConsistentKey(String body) {
        return nameUUIDFromBytes(body.getBytes()).toString();
    }
}
