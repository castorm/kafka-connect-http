package com.github.castorm.kafka.connect.http.request.template;

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
        assertThat(catchThrowable(() -> configWithout("http.source.url"))).isInstanceOf(ConfigException.class);
    }

    @Test
    void whenUrl_thenInitialized() {
        assertThat(config("http.source.url", value).getUrl()).isEqualTo(value);
    }

    @Test
    void whenMissingMethod_thenDefault() {
        assertThat(configWithout("http.source.method").getMethod()).isEqualTo("GET");
    }

    @Test
    void whenMethod_thenInitialized() {
        assertThat(config("http.source.method", value).getMethod()).isEqualTo(value);
    }

    @Test
    void whenMissingHeaders_thenDefault() {
        assertThat(configWithout("http.source.headers").getHeaders()).isEqualTo("");
    }

    @Test
    void whenHeaders_thenInitialized() {
        assertThat(config("http.source.headers", value).getHeaders()).isEqualTo(value);
    }

    @Test
    void whenMissingQueryParams_thenDefault() {
        assertThat(configWithout("http.source.query-params").getQueryParams()).isEqualTo("");
    }

    @Test
    void whenQueryParams_thenInitialized() {
        assertThat(config("http.source.query-params", value).getQueryParams()).isEqualTo(value);
    }

    @Test
    void whenMissingBody_thenDefault() {
        assertThat(configWithout("http.source.body").getBody()).isEqualTo("");
    }

    @Test
    void whenBody_thenInitialized() {
        assertThat(config("http.source.body", value).getBody()).isEqualTo(value);
    }

    @Test
    void whenMissingTemplateFactory_thenDefault() {
        assertThat(configWithout("http.source.template.factory").getTemplateFactory()).isInstanceOf(NoTemplateFactory.class);
    }

    @Test
    void whenTemplateFactory_thenInitialized() {
        assertThat(config("http.source.template.factory", TestTemplateFactory.class.getName()).getTemplateFactory()).isInstanceOf(TestTemplateFactory.class);
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
                put("http.source.url", "url");
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
