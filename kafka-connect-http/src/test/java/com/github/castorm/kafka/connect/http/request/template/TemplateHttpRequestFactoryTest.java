package com.github.castorm.kafka.connect.http.request.template;

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

import com.github.castorm.kafka.connect.http.model.Offset;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.castorm.kafka.connect.http.model.HttpRequest.HttpMethod.POST;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryTest.Fixture.offset;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryTest.Fixture.url;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryTest.Fixture.value;
import static java.util.Collections.emptyMap;
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

        given("http.request.method", "POST");

        assertThat(factory.createRequest(offset).getMethod()).isEqualTo(POST);
    }

    @Test
    void givenUrl_whenCreateRequest_thenMethodMapped() {

        factory.configure(ImmutableMap.of("http.request.url", url));

        assertThat(factory.createRequest(offset).getUrl()).isEqualTo(url);
    }

    @Test
    void givenHeaders_whenCreateRequest_thenHeadersMapped() {

        given("http.request.headers", "Header:Value");

        assertThat(factory.createRequest(offset).getHeaders()).containsEntry("Header", singletonList("Value"));
    }

    @Test
    void givenQueryParams_whenCreateRequest_thenQueryParamsMapped() {

        given("http.request.params", "param=value");

        assertThat(factory.createRequest(offset).getQueryParams()).containsEntry("param", singletonList("value"));
    }

    @Test
    void givenBody_whenCreateRequest_thenBodyMapped() {

        given("http.request.body", value);

        assertThat(factory.createRequest(offset).getBody()).isEqualTo(value.getBytes());
    }

    private void given(String key, String value) {
        factory.configure(ImmutableMap.of("http.request.url", url, key, value));
    }

    interface Fixture {
        String value = "value";
        String url = "url";
        Offset offset = Offset.of(emptyMap(), "dummy-endpoint");
    }
}
