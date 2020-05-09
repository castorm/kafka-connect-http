package com.github.castorm.kafka.connect.http.request.offset;

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

import com.github.castorm.kafka.connect.http.request.offset.spi.OffsetTemplate;
import com.github.castorm.kafka.connect.http.request.offset.spi.OffsetTemplateFactory;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.request.offset.OffsetTemplateHttpRequestFactoryConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.request.offset.OffsetTemplateHttpRequestFactoryConfigTest.Fixture.configWithout;
import static com.github.castorm.kafka.connect.http.request.offset.OffsetTemplateHttpRequestFactoryConfigTest.Fixture.defaultMap;
import static com.github.castorm.kafka.connect.http.request.offset.OffsetTemplateHttpRequestFactoryConfigTest.Fixture.value;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class OffsetTemplateHttpRequestFactoryConfigTest {

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
        assertThat(configWithout("http.request.template.factory").getOffsetTemplateFactory()).isInstanceOf(NoOffsetTemplateFactory.class);
    }

    @Test
    void whenTemplateFactory_thenInitialized() {
        assertThat(config("http.request.template.factory", TestOffsetTemplateFactory.class.getName()).getOffsetTemplateFactory()).isInstanceOf(TestOffsetTemplateFactory.class);
    }

    @Test
    void whenNoInitialOffset_thenDefault() {
        assertThat(configWithout("http.request.offset.initial").getInitialOffset()).isEqualTo(emptyMap());
    }

    @Test
    void whenInitialOffset_thenInitialized() {
        assertThat(config("http.request.offset.initial", "k=v").getInitialOffset()).isEqualTo(ImmutableMap.of("k", "v"));
    }

    public static class TestOffsetTemplateFactory implements OffsetTemplateFactory {

        @Override
        public OffsetTemplate create(String template) {
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

        static OffsetTemplateHttpRequestFactoryConfig config(String key, String value) {
            Map<String, String> customMap = defaultMap();
            customMap.put(key, value);
            return new OffsetTemplateHttpRequestFactoryConfig(customMap);
        }

        static OffsetTemplateHttpRequestFactoryConfig configWithout(String key) {
            Map<String, String> customMap = defaultMap();
            customMap.remove(key);
            return new OffsetTemplateHttpRequestFactoryConfig(customMap);
        }
    }
}
