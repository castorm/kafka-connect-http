package com.github.castorm.kafka.connect.http.response.spi;

import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import org.apache.kafka.common.Configurable;

import java.util.List;

public interface HttpResponseParser extends Configurable {

    List<HttpResponseItem> parse(HttpResponse response);
}
