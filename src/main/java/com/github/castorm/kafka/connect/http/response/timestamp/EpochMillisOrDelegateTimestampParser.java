package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import static java.lang.Long.parseLong;

@RequiredArgsConstructor
public class EpochMillisOrDelegateTimestampParser implements TimestampParser {

    private final Function<Map<String, ?>, EpochMillisOrDelegateTimestampParserConfig> configFactory;

    private TimestampParser delegate;

    public EpochMillisOrDelegateTimestampParser() {
        this(EpochMillisOrDelegateTimestampParserConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        EpochMillisOrDelegateTimestampParserConfig config = configFactory.apply(settings);
        delegate = config.getDelegateParser();
    }

    @Override
    public Instant parse(String timestamp) {
        try {
            return Instant.ofEpochMilli(parseLong(timestamp));
        } catch (NumberFormatException e) {
            return delegate.parse(timestamp);
        }
    }
}
