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

import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpRequest.HttpMethod;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.model.Partition;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.request.template.spi.Template;
import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;

import java.util.Map;

import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownHeaders;
import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownQueryParams;

public class TemplateHttpRequestFactory implements HttpRequestFactory {

    private String method;

    private Template urlTpl;

    private Template headersTpl;

    private Template queryParamsTpl;

    private Template bodyTpl;

    @Override
    public void configure(Map<String, ?> configs) {
        TemplateHttpRequestFactoryConfig config = new TemplateHttpRequestFactoryConfig(configs);
        TemplateFactory templateFactory = config.getTemplateFactory();

        method = config.getMethod();
        urlTpl = templateFactory.create(config.getUrl());
        headersTpl = templateFactory.create(config.getHeaders());
        queryParamsTpl = templateFactory.create(config.getQueryParams());
        bodyTpl = templateFactory.create(config.getBody());
    }

    @Override
    public HttpRequest createRequest(Partition partition, Offset offset) {
        return HttpRequest.builder()
                .method(HttpMethod.valueOf(method))
                .url(urlTpl.apply(partition, offset))
                .headers(breakDownHeaders(headersTpl.apply(partition, offset)))
                .queryParams(breakDownQueryParams(queryParamsTpl.apply(partition, offset)))
                .body(bodyTpl.apply(partition, offset).getBytes())
                .build();
    }
}
