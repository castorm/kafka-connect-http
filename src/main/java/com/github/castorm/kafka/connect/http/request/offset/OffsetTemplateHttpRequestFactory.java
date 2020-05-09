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

import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.request.offset.spi.OffsetTemplate;
import com.github.castorm.kafka.connect.http.request.offset.spi.OffsetTemplateFactory;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;

import java.util.Map;

import static com.github.castorm.kafka.connect.common.MapUtils.breakDownHeaders;
import static com.github.castorm.kafka.connect.common.MapUtils.breakDownQueryParams;

public class OffsetTemplateHttpRequestFactory implements HttpRequestFactory {

    private String method;

    private OffsetTemplate urlTpl;

    private OffsetTemplate headersTpl;

    private OffsetTemplate queryParamsTpl;

    private OffsetTemplate bodyTpl;

    @Override
    public void configure(Map<String, ?> configs) {
        OffsetTemplateHttpRequestFactoryConfig config = new OffsetTemplateHttpRequestFactoryConfig(configs);
        OffsetTemplateFactory offsetTemplateFactory = config.getOffsetTemplateFactory();

        method = config.getMethod();
        urlTpl = offsetTemplateFactory.create(config.getUrl());
        headersTpl = offsetTemplateFactory.create(config.getHeaders());
        queryParamsTpl = offsetTemplateFactory.create(config.getQueryParams());
        bodyTpl = offsetTemplateFactory.create(config.getBody());
    }

    @Override
    public HttpRequest createRequest(Offset offset) {
        Map<String, ?> offsetMap = offset.toMap();
        return HttpRequest.builder()
                .method(HttpRequest.HttpMethod.valueOf(method))
                .url(urlTpl.apply(offsetMap))
                .headers(breakDownHeaders(headersTpl.apply(offsetMap)))
                .queryParams(breakDownQueryParams(queryParamsTpl.apply(offsetMap)))
                .body(bodyTpl.apply(offsetMap).getBytes())
                .build();
    }
}
