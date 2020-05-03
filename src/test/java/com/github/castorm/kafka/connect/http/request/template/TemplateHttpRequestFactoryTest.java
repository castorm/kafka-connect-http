package com.github.castorm.kafka.connect.http.request.template;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.castorm.kafka.connect.http.model.HttpRequest.HttpMethod.POST;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryTest.Fixture.url;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryTest.Fixture.value;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class TemplateHttpRequestFactoryTest {

    TemplateHttpRequestFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TemplateHttpRequestFactory();
    }

    @Test
    void givenMethod_whenCreateRequest_thenMethodMapped() {

        given("http.source.method", "POST");

        assertThat(factory.createRequest().getMethod()).isEqualTo(POST);
    }

    @Test
    void givenUrl_whenCreateRequest_thenMethodMapped() {

        factory.configure(ImmutableMap.of("http.source.url", url));

        assertThat(factory.createRequest().getUrl()).isEqualTo(url);
    }

    @Test
    void givenHeaders_whenCreateRequest_thenHeadersMapped() {

        given("http.source.headers", "Header:Value");

        assertThat(factory.createRequest().getHeaders()).containsEntry("Header", singletonList("Value"));
    }

    @Test
    void givenQueryParams_whenCreateRequest_thenQueryParamsMapped() {

        given("http.source.query-params", "param=value");

        assertThat(factory.createRequest().getQueryParams()).containsEntry("param", singletonList("value"));
    }

    @Test
    void givenBody_whenCreateRequest_thenBodyMapped() {

        given("http.source.body", value);

        assertThat(factory.createRequest().getBody()).isEqualTo(value.getBytes());
    }

    private void given(String key, String value) {
        factory.configure(ImmutableMap.of("http.source.url", url, key, value));
    }

    interface Fixture {
        String value = "value";
        String url = "url";
    }
}
