package com.github.castorm.kafka.connect.http.response.jackson;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.configWithout;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.defaultMap;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.offsetConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class JacksonHttpResponseParserConfigTest {

    @Test
    void whenItemsPointerConfigured_thenInitialized() {
        assertThat(config("http.source.response.json.items.pointer", "/test-pointer").getItemsPointer()).isEqualTo(compile("/test-pointer"));
    }

    @Test
    void whenMissingItemKeyPointerConfigured_thenInitialized() {
        assertThat(configWithout("http.source.response.json.item.key.pointer").getItemKeyPointer()).isEmpty();
    }

    @Test
    void whenItemKeyPointerConfigured_thenInitialized() {
        assertThat(config("http.source.response.json.item.key.pointer", "/test-pointer").getItemKeyPointer()).isEqualTo(Optional.of(compile("/test-pointer")));
    }

    @Test
    void whenItemValuePointerConfigured_thenInitialized() {
        assertThat(config("http.source.response.json.item.value.pointer", "/test-pointer").getItemValuePointer()).isEqualTo(compile("/test-pointer"));
    }

    @Test
    void whenMissingTimestampPointerConfigured_thenInitialized() {
        assertThat(configWithout("http.source.response.json.item.timestamp.pointer").getItemTimestampPointer()).isEmpty();
    }

    @Test
    void whenItemTimestampPointerConfigured_thenInitialized() {
        assertThat(config("http.source.response.json.item.timestamp.pointer", "/test-pointer").getItemTimestampPointer()).isEqualTo(Optional.of(compile("/test-pointer")));
    }

    @Test
    void whenMissingOffsetKeyConfigured_thenException() {
        assertThat(catchThrowable(() -> configWithout("http.source.response.json.item.offset.key"))).isInstanceOf(ConfigException.class);
    }

    @Test
    void whenMissingOffsetValuePointerConfigured_thenException() {
        assertThat(catchThrowable(() -> configWithout("http.source.response.json.item.offset.value.pointer"))).isInstanceOf(ConfigException.class);
    }

    @Test
    void whenItemOffsetPointersConfigured_thenInitialized() {
        assertThat(offsetConfig("key", "/value").getItemOffsets()).isEqualTo(ImmutableMap.of("key", compile("/value")));
    }

    @Test
    void whenItemOffsetMultiplePointersConfigured_thenInitialized() {
        assertThat(offsetConfig("key,key2", "/value,/value2").getItemOffsets()).isEqualTo(ImmutableMap.of("key", compile("/value"), "key2", compile("/value2")));
    }

    @Test
    void whenItemOffsetMultiplePointersWithSpacesConfigured_thenInitialized() {
        assertThat(offsetConfig("  key  ,  key2  ", "  /value  ,  /value2  ").getItemOffsets()).isEqualTo(ImmutableMap.of("key", compile("/value"), "key2", compile("/value2")));
    }

    @Test
    void whenItemOffsetMoreValuesPointersLengthConfigured_thenInitialized() {
        assertThat(catchThrowable(() -> offsetConfig("key", "/value,/value2"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenItemOffsetMoreKeysPointersLengthConfigured_thenInitialized() {
        assertThat(catchThrowable(() -> offsetConfig("key,key2", "/value"))).isInstanceOf(IllegalStateException.class);
    }

    interface Fixture {
        static Map<String, String> defaultMap() {
            return new HashMap<String, String>() {{
                put("http.source.response.json.items.pointer", "/");
                put("http.source.response.json.item.key.pointer", "/");
                put("http.source.response.json.item.value.pointer", "/");
                put("http.source.response.json.item.timestamp.pointer", "/");
                put("http.source.response.json.item.offset.value.pointer", "/");
                put("http.source.response.json.item.offset.key", "/");
            }};
        }

        static JacksonHttpResponseParserConfig config(String key, String value) {
            Map<String, String> customMap = defaultMap();
            customMap.put(key, value);
            return new JacksonHttpResponseParserConfig(customMap);
        }

        static JacksonHttpResponseParserConfig configWithout(String key) {
            Map<String, String> customMap = defaultMap();
            customMap.remove(key);
            return new JacksonHttpResponseParserConfig(customMap);
        }

        static JacksonHttpResponseParserConfig offsetConfig(String keys, String values) {
            Map<String, String> customMap = defaultMap();
            customMap.put("http.source.response.json.item.offset.value.pointer", values);
            customMap.put("http.source.response.json.item.offset.key", keys);
            return new JacksonHttpResponseParserConfig(customMap);
        }
    }
}
