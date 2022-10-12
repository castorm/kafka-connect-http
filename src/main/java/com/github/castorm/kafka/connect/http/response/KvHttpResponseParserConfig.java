package com.github.castorm.kafka.connect.http.response;

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

import com.github.castorm.kafka.connect.http.record.SchemedKvSourceRecordMapper;
import com.github.castorm.kafka.connect.http.record.spi.KvSourceRecordMapper;
import com.github.castorm.kafka.connect.http.response.jackson.JacksonKvRecordHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.KvRecordHttpResponseParser;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;

@Getter
public class KvHttpResponseParserConfig extends AbstractConfig {

    private static final String RECORD_PARSER_CLASS = "http.response.record.parser";
    private static final String RECORD_MAPPER_CLASS = "http.response.record.mapper";

    private final KvRecordHttpResponseParser recordParser;
    private final KvSourceRecordMapper recordMapper;

    KvHttpResponseParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        recordParser = getConfiguredInstance(RECORD_PARSER_CLASS, KvRecordHttpResponseParser.class);
        recordMapper = getConfiguredInstance(RECORD_MAPPER_CLASS, KvSourceRecordMapper.class);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(RECORD_PARSER_CLASS, CLASS, JacksonKvRecordHttpResponseParser.class, LOW, "Key-Value Record Parser class")
                .define(RECORD_MAPPER_CLASS, CLASS, SchemedKvSourceRecordMapper.class, LOW, "Key-Value Record Factory class");
    }
}
