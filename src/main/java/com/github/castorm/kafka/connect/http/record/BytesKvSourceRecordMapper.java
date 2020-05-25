package com.github.castorm.kafka.connect.http.record;

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

import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.model.KvRecord;
import com.github.castorm.kafka.connect.http.record.spi.KvSourceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static org.apache.kafka.connect.data.SchemaBuilder.bytes;

@RequiredArgsConstructor
public class BytesKvSourceRecordMapper implements KvSourceRecordMapper {

    private static Map<String, ?> sourcePartition = emptyMap();

    private static final Schema keySchema = bytes().build();

    private static final Schema valueSchema = bytes().build();

    private final Function<Map<String, ?>, BytesKvSourceRecordMapperConfig> configFactory;

    private BytesKvSourceRecordMapperConfig config;

    private Charset charset;

    public BytesKvSourceRecordMapper() {
        this(BytesKvSourceRecordMapperConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        config = configFactory.apply(settings);
    }

    @Override
    public SourceRecord map(KvRecord record) {

        Offset offset = record.getOffset();
        return new SourceRecord(
                sourcePartition,
                offset.toMap(),
                config.getTopic(),
                null,
                keySchema,
                record.getKey().getBytes(config.getCharset()),
                valueSchema,
                record.getValue().getBytes(config.getCharset()),
                offset.getTimestamp().toEpochMilli());
    }
}
