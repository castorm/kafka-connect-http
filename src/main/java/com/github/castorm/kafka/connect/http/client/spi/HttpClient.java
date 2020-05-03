package com.github.castorm.kafka.connect.http.client.spi;

import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import org.apache.kafka.common.Configurable;

import java.io.IOException;

public interface HttpClient extends Configurable {

    HttpResponse execute(HttpRequest request) throws IOException;
}
