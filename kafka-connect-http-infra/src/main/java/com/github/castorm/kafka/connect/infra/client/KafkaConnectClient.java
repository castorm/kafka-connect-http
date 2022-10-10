package com.github.castorm.kafka.connect.infra.client;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 Cástor Rodríguez
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.Map;

import static com.github.castorm.kafka.connect.ConnectorUtils.getConfigMap;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

@Slf4j
@RequiredArgsConstructor
public class KafkaConnectClient {

    private final String kafkaConnectUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor(log::info).setLevel(BODY))
            .build();

    @SneakyThrows
    public Map<String, String> createConnector(String configJson) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.method("POST", RequestBody.create(configJson, MediaType.parse("application/json")));
        requestBuilder.header("Accept", "application/json");
        requestBuilder.url(String.format("http://%s/connectors", kafkaConnectUrl));
        client.newCall(requestBuilder.build()).execute();
        return getConfigMap(configJson);
    }
}
