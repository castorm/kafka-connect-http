package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
