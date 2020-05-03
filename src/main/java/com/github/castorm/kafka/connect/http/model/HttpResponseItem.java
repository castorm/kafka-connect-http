package com.github.castorm.kafka.connect.http.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Map;

@Value
@Wither
@Builder
public class HttpResponseItem {

    String key;

    String value;

    Map<String, Object> offset;

    Long timestamp;
}
