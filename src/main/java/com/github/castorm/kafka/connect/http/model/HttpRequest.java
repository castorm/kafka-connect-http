package com.github.castorm.kafka.connect.http.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

import java.util.List;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.model.HttpRequest.HttpMethod.GET;
import static java.util.Collections.emptyMap;

@Value
@Builder
public class HttpRequest {

    @Default
    HttpMethod method = GET;

    String url;

    @Default
    Map<String, List<String>> queryParams = emptyMap();

    @Default
    Map<String, List<String>> headers = emptyMap();

    byte[] body;

    public enum HttpMethod {
        GET, HEAD, POST, PUT, PATCH
    }
}
