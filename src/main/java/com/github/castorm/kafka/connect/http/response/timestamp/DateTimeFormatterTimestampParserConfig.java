package com.github.castorm.kafka.connect.http.response.timestamp;

/*-
 * #%L
 * kafka-connect-http-plugin
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

import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class DateTimeFormatterTimestampParserConfig extends AbstractConfig {

    private static final String ITEM_TIMESTAMP_PATTERN = "http.response.item.timestamp.parser.pattern";
    private static final String ITEM_TIMESTAMP_ZONE = "http.response.item.timestamp.parser.zone";

    private final DateTimeFormatter itemTimestampFormatter;

    DateTimeFormatterTimestampParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        itemTimestampFormatter = DateTimeFormatter.ofPattern(getString(ITEM_TIMESTAMP_PATTERN))
                .withZone(ZoneId.of(getString(ITEM_TIMESTAMP_ZONE)));
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(ITEM_TIMESTAMP_PATTERN, STRING, "yyyy-MM-dd'T'HH:mm:ss.SSSX", LOW, "Timestamp format pattern")
                .define(ITEM_TIMESTAMP_ZONE, STRING, "UTC", LOW, "Timestamp ZoneId");
    }
}
