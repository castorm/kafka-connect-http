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
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class DateTimeFormatterTimestampParser implements TimestampParser {

    private final Function<Map<String, ?>, DateTimeFormatterTimestampParserConfig> configFactory;

    private DateTimeFormatter timestampFormatter;

    public DateTimeFormatterTimestampParser() {
        this(DateTimeFormatterTimestampParserConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        timestampFormatter = configFactory.apply(settings).getRecordTimestampFormatter();
    }

    @Override
    public Instant parse(String timestamp) {
        return OffsetDateTime.parse(timestamp, timestampFormatter).toInstant();
    }
}
