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

import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.request.template.spi.Template;
import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;

import java.util.Map;

import static com.github.castorm.kafka.connect.common.HttpUtils.breakDownHeaders;
import static com.github.castorm.kafka.connect.common.HttpUtils.breakDownQueryParams;

public class TemplateHttpRequestFactory implements HttpRequestFactory {

    private TemplateFactory templateFactory;

    private String method;

    private Template urlTpl;

    private Template headersTpl;

    private Template queryParamsTpl;

    private Template bodyTpl;

    private Map<String, ?> offset;

    @Override
    public void configure(Map<String, ?> configs) {
        TemplateHttpRequestFactoryConfig config = new TemplateHttpRequestFactoryConfig(configs);
        templateFactory = config.getTemplateFactory();

        method = config.getMethod();
        urlTpl = templateFactory.create(config.getUrl());
        headersTpl = templateFactory.create(config.getHeaders());
        queryParamsTpl = templateFactory.create(config.getQueryParams());
        bodyTpl = templateFactory.create(config.getBody());
    }

    @Override
    public void setOffset(Map<String, ?> offset) {
        this.offset = offset;
    }

    @Override
    public HttpRequest createRequest() {
        return HttpRequest.builder()
                .method(HttpRequest.HttpMethod.valueOf(method))
                .url(urlTpl.apply(offset))
                .headers(breakDownHeaders(headersTpl.apply(offset)))
                .queryParams(breakDownQueryParams(queryParamsTpl.apply(offset)))
                .body(bodyTpl.apply(offset).getBytes())
                .build();
    }
}
