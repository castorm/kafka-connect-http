package com.github.castorm.kafka.connect.http.client.okhttp;

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

import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static okhttp3.HttpUrl.parse;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

@Slf4j
public class OkHttpClient implements HttpClient {

    private okhttp3.OkHttpClient client;

    @Override
    public void configure(Map<String, ?> configs) {

        OkHttpClientConfig config = new OkHttpClientConfig(configs);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(log::debug);
        interceptor.setLevel(BASIC);
        client = new okhttp3.OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MILLISECONDS))
                .connectTimeout(config.getConnectionTimeoutMillis(), MILLISECONDS)
                .readTimeout(config.getReadTimeoutMillis(), MILLISECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor)
                .build();
    }

    @Override
    public HttpResponse execute(HttpRequest httpRequest) throws IOException {

        Request request = fromHttpRequest(httpRequest);

        Response response = client.newCall(request).execute();

        return toHttpResponse(response);
    }

    private static Request fromHttpRequest(HttpRequest request) {
        Request.Builder builder = new Request.Builder();
        builder.url(mapUrl(request.getUrl(), request.getQueryParams()));
        addHeaders(builder, request);
        addMethodWithBody(builder, request);
        return builder.build();
    }

    private static void addHeaders(Request.Builder builder, HttpRequest request) {
        request.getHeaders().forEach((name, values) -> values.forEach(value -> builder.addHeader(name, value)));
    }

    private static void addMethodWithBody(Request.Builder builder, HttpRequest request) {
        switch (request.getMethod()) {
            case HEAD:
                builder.head();
                break;
            case PUT:
                mapBody(request).ifPresent(builder::put);
                break;
            case POST:
                mapBody(request).ifPresent(builder::post);
                break;
            case PATCH:
                mapBody(request).ifPresent(builder::patch);
                break;
            case GET:
            default:
                builder.get();
                break;
        }
    }

    private static Optional<RequestBody> mapBody(HttpRequest request) {
        if (request.getBody() != null) {
            return Optional.of(RequestBody.create(request.getBody()));
        }
        return empty();
    }

    private static HttpUrl mapUrl(String url, Map<String, List<String>> queryParams) {
        HttpUrl.Builder urlBuilder = parse(url).newBuilder();
        queryParams.forEach((k, list) -> list.forEach(v -> urlBuilder.addEncodedQueryParameter(k, v)));
        return urlBuilder.build();
    }

    private static HttpResponse toHttpResponse(Response response) throws IOException {
        return HttpResponse.builder()
                .code(response.code())
                .body(response.body() != null ? response.body().bytes() : new byte[0])
                .headers(response.headers().toMultimap())
                .build();
    }
}
