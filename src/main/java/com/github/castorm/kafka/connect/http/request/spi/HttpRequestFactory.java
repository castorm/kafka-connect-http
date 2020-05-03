package com.github.castorm.kafka.connect.http.request.spi;

import com.github.castorm.kafka.connect.http.model.HttpRequest;
import org.apache.kafka.common.Configurable;

import java.util.Map;

public interface HttpRequestFactory extends Configurable {

    void setOffset(Map<String, ?> offset);

    HttpRequest createRequest();
}
