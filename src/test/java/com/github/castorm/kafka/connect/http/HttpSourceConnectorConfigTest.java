package com.github.castorm.kafka.connect.http;

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

import com.github.castorm.kafka.connect.http.client.okhttp.OkHttpClient;
import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.model.HttpRequest;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapper;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordMapper;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.HttpSourceConnectorConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.HttpSourceConnectorConfigTest.Fixture.configWithout;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class HttpSourceConnectorConfigTest {

    @Test
    void whenNoClient_thenDefault() {
        assertThat(configWithout("http.client").getClient()).isInstanceOf(OkHttpClient.class);
    }

    @Test
    void whenClient_thenInitialized() {
        assertThat(config("http.client", TestHttpClient.class.getName()).getClient()).isInstanceOf(TestHttpClient.class);
    }

    @Test
    void whenNoRequestFactory_thenDefault() {
        assertThat(configWithout("http.request.factory").getRequestFactory()).isInstanceOf(TemplateHttpRequestFactory.class);
    }

    @Test
    void whenRequestFactory_thenInitialized() {
        assertThat(config("http.request.factory", TestRequestFactory.class.getName()).getRequestFactory()).isInstanceOf(TestRequestFactory.class);
    }

    @Test
    void whenNoResponseParser_thenDefault() {
        assertThat(configWithout("http.response.parser").getResponseParser()).isInstanceOf(JacksonHttpResponseParser.class);
    }

    @Test
    void whenResponseParser_thenInitialized() {
        assertThat(config("http.response.parser", TestResponseParser.class.getName()).getResponseParser()).isInstanceOf(TestResponseParser.class);
    }

    @Test
    void whenNoRecordMapper_thenDefault() {
        assertThat(configWithout("http.record.mapper").getRecordMapper()).isInstanceOf(SchemedSourceRecordMapper.class);
    }

    @Test
    void whenRecordMapper_thenInitialized() {
        assertThat(config("http.record.mapper", TestRecordMapper.class.getName()).getRecordMapper()).isInstanceOf(TestRecordMapper.class);
    }

    @Test
    void whenNoInitialOffset_thenDefault() {
        assertThat(configWithout("http.offset.initial").getInitialOffset()).isEqualTo(emptyMap());
    }

    @Test
    void whenInitialOffset_thenInitialized() {
        assertThat(config("http.offset.initial", "k=v").getInitialOffset()).isEqualTo(ImmutableMap.of("k", "v"));
    }

    public static class TestHttpClient implements HttpClient {
        public HttpResponse execute(HttpRequest request) { return null; }
        public void configure(Map<String, ?> map) {}
    }

    public static class TestRequestFactory implements HttpRequestFactory {
        public void setOffset(Map<String, ?> offset) {}
        public HttpRequest createRequest() { return null; }
        public void configure(Map<String, ?> map) {}
    }

    public static class TestResponseParser implements HttpResponseParser {
        public List<HttpResponseItem> parse(HttpResponse response) { return null; }
        public void configure(Map<String, ?> map) {}
    }

    public static class TestRecordMapper implements SourceRecordMapper {
        public SourceRecord map(HttpResponseItem item) { return null; }
        public void configure(Map<String, ?> map) {}
    }

    interface Fixture {
        static Map<String, String> defaultMap() {
            return new HashMap<String, String>() {{
                put("kafka.topic", "topic");
                put("http.request.url", "foo");
                put("http.response.json.item.offset.value.pointer", "/baz");
            }};
        }

        static HttpSourceConnectorConfig config(String key, String value) {
            Map<String, String> customMap = defaultMap();
            customMap.put(key, value);
            return new HttpSourceConnectorConfig(customMap);
        }

        static HttpSourceConnectorConfig configWithout(String key) {
            Map<String, String> customMap = defaultMap();
            customMap.remove(key);
            return new HttpSourceConnectorConfig(customMap);
        }
    }
}
