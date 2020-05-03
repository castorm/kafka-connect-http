package com.github.castorm.kafka.connect.http.request.template.spi;

import java.util.Map;

@FunctionalInterface
public interface Template {

    String apply(Map<String, ?> offset);
}
