package com.github.castorm.kafka.connect.http.request.offset;

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

import com.github.castorm.kafka.connect.http.request.offset.spi.OffsetTemplateFactory;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static com.github.castorm.kafka.connect.common.MapUtils.breakDownMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Importance.MEDIUM;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class OffsetTemplateHttpRequestFactoryConfig extends AbstractConfig {

    private static final String URL = "http.request.url";
    private static final String METHOD = "http.request.method";
    private static final String HEADERS = "http.request.headers";
    private static final String QUERY_PARAMS = "http.request.params";
    private static final String BODY = "http.request.body";
    private static final String TEMPLATE_FACTORY = "http.request.template.factory";
    private static final String OFFSET_INITIAL = "http.request.offset.initial";

    private final String url;

    private final String method;

    private final String headers;

    private final String queryParams;

    private final String body;

    private final OffsetTemplateFactory offsetTemplateFactory;

    private final Map<String, String> initialOffset;

    OffsetTemplateHttpRequestFactoryConfig(Map<String, ?> originals) {
        super(config(), originals);
        url = getString(URL);
        method = getString(METHOD);
        headers = getString(HEADERS);
        queryParams = getString(QUERY_PARAMS);
        body = getString(BODY);
        offsetTemplateFactory = getConfiguredInstance(TEMPLATE_FACTORY, OffsetTemplateFactory.class);
        initialOffset = breakDownMap(getString(OFFSET_INITIAL));
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(URL, STRING, HIGH, "HTTP URL OffsetTemplate")
                .define(METHOD, STRING, "GET", HIGH, "HTTP Method OffsetTemplate")
                .define(HEADERS, STRING, "", MEDIUM, "HTTP Headers OffsetTemplate")
                .define(QUERY_PARAMS, STRING, "", MEDIUM, "HTTP Query Params OffsetTemplate")
                .define(BODY, STRING, "", LOW, "HTTP Body OffsetTemplate")
                .define(TEMPLATE_FACTORY, CLASS, NoOffsetTemplateFactory.class, LOW, "OffsetTemplate Factory Class")
                .define(OFFSET_INITIAL, STRING, "", HIGH, "Starting offset");
    }
}
