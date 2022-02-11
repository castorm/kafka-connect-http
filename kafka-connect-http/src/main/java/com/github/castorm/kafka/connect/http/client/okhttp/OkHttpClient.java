package com.github.castorm.kafka.connect.http.client.okhttp;

/*-
 * #%L
 * kafka-connect-http
 * %%
 * Copyright (C) 2020 CastorM
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

import com.github.castorm.kafka.connect.http.auth.spi.HttpAuthenticator;
import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.net.Proxy.NO_PROXY;
import static java.net.Proxy.Type.HTTP;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static okhttp3.Credentials.basic;
import static okhttp3.HttpUrl.parse;
import static okhttp3.RequestBody.create;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;
import static okhttp3.logging.HttpLoggingInterceptor.Level.NONE;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Slf4j
public class OkHttpClient implements HttpClient {

    private okhttp3.OkHttpClient client;

    private HttpAuthenticator authenticator;

    @Override
    public void configure(Map<String, ?> configs) {

        OkHttpClientConfig config = new OkHttpClientConfig(configs);

        authenticator = config.getAuthenticator();

        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MILLISECONDS))
                .connectTimeout(config.getConnectionTimeoutMillis(), MILLISECONDS)
                .readTimeout(config.getReadTimeoutMillis(), MILLISECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(createLoggingInterceptor())
                .addInterceptor(chain -> chain.proceed(authorize(chain.request())))
                .authenticator((route, response) -> authorize(response.request()))
                .proxy(resolveProxy(config.getProxyHost(), config.getProxyPort()))
                .proxyAuthenticator(resolveProxyAuthenticator(config.getProxyUsername(), config.getProxyPassword()));

        resolveSslSocketFactory(builder, config.getKeyStore(), config.getKeyStorePassword().value());

        client = builder.build();
    }

    private Request authorize(Request request) {
        return authenticator.getAuthorizationHeader()
                .map(header -> request.newBuilder().header(AUTHORIZATION, header).build())
                .orElse(request);
    }

    private static HttpLoggingInterceptor createLoggingInterceptor() {
        if (log.isTraceEnabled()) {
            return new HttpLoggingInterceptor(log::trace).setLevel(BODY);
        } else if (log.isDebugEnabled()) {
            return new HttpLoggingInterceptor(log::debug).setLevel(BASIC);
        } else {
            return new HttpLoggingInterceptor(log::info).setLevel(NONE);
        }
    }

    private static Proxy resolveProxy(String host, Integer port) {
        return isEmpty(host) ? NO_PROXY : new Proxy(HTTP, new InetSocketAddress(host, port));
    }

    private static Authenticator resolveProxyAuthenticator(String username, String password) {
        return isEmpty(username) ? Authenticator.NONE :
                (route, response) -> response.request().newBuilder()
                        .header("Proxy-Authorization", basic(username, password))
                        .build();
    }

    private static void resolveSslSocketFactory(okhttp3.OkHttpClient.Builder builder, String keyStorePath, String keyStorePassword) {
        if (keyStorePath.isEmpty()) {
            return;
        }

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to create keystore", e);
        }

        try (InputStream is = new FileInputStream(keyStorePath)) {
            keyStore.load(is, keyStorePassword.toCharArray());
        }
        catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(String.format("Unable to load keystore '%s'", keyStorePath), e);
        }

        KeyManagerFactory kmf;
        try {
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePassword.toCharArray());
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException("Unable to initialize key manager", e);
        }

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Unable to initialize SSL context", e);
        }

        TrustManagerFactory trustManagerFactory;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException("Unable to initialize trust manager", e);
        }

        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }

        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
    }

    @Override
    @SneakyThrows(IOException.class)
    public HttpResponse execute(HttpRequest httpRequest) {

        Request request = mapHttpRequest(httpRequest);

        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            return mapHttpResponse(response);
        }
    }

    private static Request mapHttpRequest(HttpRequest request) {
        Request.Builder builder = new Request.Builder();
        builder.url(mapUrl(request.getUrl(), request.getQueryParams()));
        addHeaders(builder, request);
        addMethodWithBody(builder, request);
        return builder.build();
    }

    private static HttpUrl mapUrl(String url, Map<String, List<String>> queryParams) {
        HttpUrl httpUrl = parse(url);
        if (httpUrl == null) {
            throw new IllegalStateException(String.format("Illegal url: %s", url));
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        queryParams.forEach((k, list) -> list.forEach(v -> urlBuilder.addEncodedQueryParameter(k, v)));
        return urlBuilder.build();
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
            return Optional.of(create(request.getBody()));
        }
        return empty();
    }

    private static HttpResponse mapHttpResponse(Response response) throws IOException {
        return HttpResponse.builder()
                .code(response.code())
                .body(response.body() != null ? response.body().bytes() : new byte[0])
                .headers(response.headers().toMultimap())
                .build();
    }
}
