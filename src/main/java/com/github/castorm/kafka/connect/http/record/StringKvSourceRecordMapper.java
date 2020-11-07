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

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static org.apache.kafka.connect.data.SchemaBuilder.string;

@Deprecated
@RequiredArgsConstructor
public class StringKvSourceRecordMapper implements KvSourceRecordMapper {

    private static Map<String, ?> sourcePartition = emptyMap();

    private static final Schema keySchema = string().build();

    private static final Schema valueSchema = string().build();

    private final Function<Map<String, ?>, SourceRecordMapperConfig> configFactory;

    private SourceRecordMapperConfig config;

    public StringKvSourceRecordMapper() {
        this(SourceRecordMapperConfig::new);
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
                record.getKey(),
                valueSchema,
                record.getValue(),
                offset.getTimestamp().map(Instant::toEpochMilli).orElseGet(System::currentTimeMillis));
    }
}
