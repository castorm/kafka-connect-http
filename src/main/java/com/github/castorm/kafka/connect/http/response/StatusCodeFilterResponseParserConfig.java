package com.github.castorm.kafka.connect.http.response;

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

import com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;

@Getter
public class StatusCodeFilterResponseParserConfig extends AbstractConfig {

    private static final String PARSER_DELEGATE = "http.response.parser.delegate";

    private final HttpResponseParser delegateParser;

    public StatusCodeFilterResponseParserConfig(Map<String, ?> originals) {
        super(config(), originals);
        delegateParser = getConfiguredInstance(PARSER_DELEGATE, HttpResponseParser.class);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(PARSER_DELEGATE, CLASS, JacksonHttpResponseParser.class, HIGH, "Response Parser Delegate Class");
    }
}
