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

import com.github.castorm.kafka.connect.http.request.template.freemarker.BackwardsCompatibleFreeMarkerTemplateFactory;
import com.github.castorm.kafka.connect.http.request.template.spi.Template;
import com.github.castorm.kafka.connect.http.request.template.spi.TemplateFactory;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryConfigTest.Fixture.configWithout;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryConfigTest.Fixture.defaultMap;
import static com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactoryConfigTest.Fixture.value;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TemplateHttpRequestFactoryConfigTest {

    @Test
    void whenMissingUrl_thenException() {
        assertThat(catchThrowable(() -> configWithout("http.request.url"))).isInstanceOf(ConfigException.class);
    }

    @Test
    void whenUrl_thenInitialized() {
        assertThat(config("http.request.url", value).getUrl()).isEqualTo(value);
    }

    @Test
    void whenMissingMethod_thenDefault() {
        assertThat(configWithout("http.request.method").getMethod()).isEqualTo("GET");
    }

    @Test
    void whenMethod_thenInitialized() {
        assertThat(config("http.request.method", value).getMethod()).isEqualTo(value);
    }

    @Test
    void whenMissingHeaders_thenDefault() {
        assertThat(configWithout("http.request.headers").getHeaders()).isEqualTo("");
    }

    @Test
    void whenHeaders_thenInitialized() {
        assertThat(config("http.request.headers", value).getHeaders()).isEqualTo(value);
    }

    @Test
    void whenMissingQueryParams_thenDefault() {
        assertThat(configWithout("http.request.params").getQueryParams()).isEqualTo("");
    }

    @Test
    void whenQueryParams_thenInitialized() {
        assertThat(config("http.request.params", value).getQueryParams()).isEqualTo(value);
    }

    @Test
    void whenMissingBody_thenDefault() {
        assertThat(configWithout("http.request.body").getBody()).isEqualTo("");
    }

    @Test
    void whenBody_thenInitialized() {
        assertThat(config("http.request.body", value).getBody()).isEqualTo(value);
    }

    @Test
    void whenMissingTemplateFactory_thenDefault() {
        assertThat(configWithout("http.request.template.factory").getTemplateFactory()).isInstanceOf(BackwardsCompatibleFreeMarkerTemplateFactory.class);
    }

    @Test
    void whenTemplateFactory_thenInitialized() {
        assertThat(config("http.request.template.factory", TestTemplateFactory.class.getName()).getTemplateFactory()).isInstanceOf(TestTemplateFactory.class);
    }

    public static class TestTemplateFactory implements TemplateFactory {

        @Override
        public Template create(String template) {
            return null;
        }
    }

    interface Fixture {
        String value = "url";

        static Map<String, String> defaultMap() {
            return new HashMap<String, String>() {{
                put("http.request.url", "url");
            }};
        }

        static TemplateHttpRequestFactoryConfig config(String key, String value) {
            Map<String, String> customMap = defaultMap();
            customMap.put(key, value);
            return new TemplateHttpRequestFactoryConfig(customMap);
        }

        static TemplateHttpRequestFactoryConfig configWithout(String key) {
            Map<String, String> customMap = defaultMap();
            customMap.remove(key);
            return new TemplateHttpRequestFactoryConfig(customMap);
        }
    }
}
