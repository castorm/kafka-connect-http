package com.github.castorm.kafka.connect.http.response.timestamp;

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

import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;

@Getter
public class EpochMillisOrDelegateTimestampParserConfig extends AbstractConfig {

    private static final String PARSER_DELEGATE = "http.response.record.timestamp.parser.delegate";

    private final TimestampParser delegateParser;

    public EpochMillisOrDelegateTimestampParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        delegateParser = getConfiguredInstance(PARSER_DELEGATE, TimestampParser.class);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(PARSER_DELEGATE, CLASS, DateTimeFormatterTimestampParser.class, HIGH, "Timestamp Parser Delegate Class");
    }
}
