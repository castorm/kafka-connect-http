package com.github.castorm.kafka.connect.http.request.template;

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

import com.github.castorm.kafka.connect.http.request.template.freemarker.BackwardsCompatibleFreeMarkerTemplateFactory;
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

    public static final String URL = "http.request.url";
    private static final String METHOD = "http.request.method";
    private static final String HEADERS = "http.request.headers";
    private static final String QUERY_PARAMS = "http.request.params";
    private static final String BODY = "http.request.body";
    private static final String TEMPLATE_FACTORY = "http.request.template.factory";

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
                .define(TEMPLATE_FACTORY, CLASS, BackwardsCompatibleFreeMarkerTemplateFactory.class, LOW, "Template Factory Class");
    }
}
