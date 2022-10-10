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
import com.joestelmach.natty.Parser;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class NattyTimestampParser implements TimestampParser {

    private final Parser parser = new Parser();

    private final Function<Map<String, ?>, NattyTimestampParserConfig> configFactory;

    private Optional<ZoneId> zoneId;

    public NattyTimestampParser() {
        this(NattyTimestampParserConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        zoneId = configFactory.apply(settings).getTimestampZoneId();
    }

    @Override
    public Instant parse(String timestamp) {
        return ofNullable(parser.parse(timestamp)).orElse(emptyList()).stream().findFirst()
                .flatMap(it -> it.getDates().stream().findFirst())
                .map(this::mapWithZoneId)
                .orElseThrow(() -> new IllegalStateException("Couldn't parse timestamp " + timestamp));
    }

    private Instant mapWithZoneId(Date it) {
        return zoneId
                .map(zoneId -> it.toInstant().atZone(systemDefault()).withZoneSameLocal(zoneId).toInstant())
                .orElseGet(it::toInstant);
    }
}
