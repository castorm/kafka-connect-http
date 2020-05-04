package com.github.castorm.kafka.connect.http.request.template;

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

import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Importance.MEDIUM;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;

@Getter
public class TemplateHttpRequestFactoryConfig extends AbstractConfig {

    private static final String URL = "http.source.url";
    private static final String METHOD = "http.source.method";
    private static final String HEADERS = "http.source.headers";
    private static final String QUERY_PARAMS = "http.source.query-params";
    private static final String BODY = "http.source.body";
    private static final String TEMPLATE_FACTORY = "http.source.template.factory";

    private final String url;

    private final String method;

    private final String headers;

    private final String queryParams;

    private final String body;

    private final TemplateFactory templateFactory;

    TemplateHttpRequestFactoryConfig(Map<String, ?> originals) {
        super(config(), originals);
        url = getString(URL);
        method = getString(METHOD);
        headers = getString(HEADERS);
        queryParams = getString(QUERY_PARAMS);
        body = getString(BODY);
        templateFactory = getConfiguredInstance(TEMPLATE_FACTORY, TemplateFactory.class);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(URL, STRING, HIGH, "HTTP URL Template")
                .define(METHOD, STRING, "GET", HIGH, "HTTP Method Template")
                .define(HEADERS, STRING, "", MEDIUM, "HTTP Headers Template")
                .define(QUERY_PARAMS, STRING, "", MEDIUM, "HTTP Query Params Template")
                .define(BODY, STRING, "", LOW, "HTTP Body Template")
                .define(TEMPLATE_FACTORY, CLASS, NoTemplateFactory.class, LOW, "Template Factory Class");
    }
}
