package com.github.castorm.kafka.connect.http;

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
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.HttpSourceConnectorConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.HttpSourceConnectorConfigTest.Fixture.configWithout;
import static org.assertj.core.api.Assertions.assertThat;

class HttpSourceConnectorConfigTest {

    @Test
    void whenPollIntervalMillis_thenDefault() {
        assertThat(configWithout("http.source.poll.interval.millis").getPollIntervalMillis()).isEqualTo(60000L);
    }

    @Test
    void whenPollIntervalMillis_thenInitialized() {
        assertThat(config("http.source.poll.interval.millis", "42").getPollIntervalMillis()).isEqualTo(42L);
    }

    @Test
    void whenClient_thenDefault() {
        assertThat(configWithout("http.client").getClient()).isInstanceOf(OkHttpClient.class);
    }

    @Test
    void whenClient_thenInitialized() {
        assertThat(config("http.client", TestHttpClient.class.getName()).getClient()).isInstanceOf(TestHttpClient.class);
    }

    @Test
    void whenRequestFactory_thenDefault() {
        assertThat(configWithout("http.source.request.factory").getRequestFactory()).isInstanceOf(TemplateHttpRequestFactory.class);
    }

    @Test
    void whenRequestFactory_thenInitialized() {
        assertThat(config("http.source.request.factory", TestRequestFactory.class.getName()).getRequestFactory()).isInstanceOf(TestRequestFactory.class);
    }

    @Test
    void whenResponseParser_thenDefault() {
        assertThat(configWithout("http.source.response.parser").getResponseParser()).isInstanceOf(JacksonHttpResponseParser.class);
    }

    @Test
    void whenResponseParser_thenInitialized() {
        assertThat(config("http.source.response.parser", TestResponseParser.class.getName()).getResponseParser()).isInstanceOf(TestResponseParser.class);
    }

    @Test
    void whenRecordMapper_thenDefault() {
        assertThat(configWithout("http.source.record.mapper").getRecordMapper()).isInstanceOf(SchemedSourceRecordMapper.class);
    }

    @Test
    void whenRecordMapper_thenInitialized() {
        assertThat(config("http.source.record.mapper", TestRecordMapper.class.getName()).getRecordMapper()).isInstanceOf(TestRecordMapper.class);
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
                put("http.source.url", "foo");
                put("http.source.response.json.item.key.pointer", "/bar");
                put("http.source.response.json.item.offset.value.pointer", "/baz");
                put("http.source.response.json.item.offset.key", "key");
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
