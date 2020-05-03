package com.github.castorm.kafka.connect.http.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Value
@Builder
public class HttpResponse {

    Integer code;

    byte[] body;

    @Default
    Map<String, List<String>> headers = emptyMap();
}
