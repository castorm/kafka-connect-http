package com.github.castorm.kafka.connect.http.request.template;

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
