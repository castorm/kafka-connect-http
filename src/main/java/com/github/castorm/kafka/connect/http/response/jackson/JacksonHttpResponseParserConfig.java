package com.github.castorm.kafka.connect.http.response.jackson;

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

import com.github.castorm.kafka.connect.http.response.timestamp.DateTimeFormatterTimestampParser;
import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;

@Getter
public class JacksonHttpResponseParserConfig extends AbstractConfig {

    private static final String ITEM_ITEM_PARSER_CLASS = "http.response.item.parser.class";
    private static final String ITEM_ITEM_TIMESTAMP_PARSER_CLASS = "http.response.item.timestamp.parser.class";

    private final JacksonItemParser itemParser;
    private final TimestampParser timestampParser;

    JacksonHttpResponseParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        itemParser = getConfiguredInstance(ITEM_ITEM_PARSER_CLASS, JacksonItemParser.class);
        timestampParser = getConfiguredInstance(ITEM_ITEM_TIMESTAMP_PARSER_CLASS, TimestampParser.class);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(ITEM_ITEM_PARSER_CLASS, CLASS, JacksonItemParser.class, LOW, "Item Timestamp parser class")
                .define(ITEM_ITEM_TIMESTAMP_PARSER_CLASS, CLASS, DateTimeFormatterTimestampParser.class, LOW, "Item Timestamp parser class");
    }
}
