package com.github.castorm.kafka.connect.http.response.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseItemMapperTest.Fixture.deserialize;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseItemMapperTest.Fixture.item1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseItemMapperTest.Fixture.item2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseItemMapperTest.Fixture.itemArray;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseItemMapperTest.Fixture.itemNested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(MockitoExtension.class)
class JacksonHttpResponseItemMapperTest {

    JacksonHttpResponseItemMapper mapper = new JacksonHttpResponseItemMapper();

    @Test
    void whenGetItemsRoot_thenRoot() {
        assertThat(mapper.getItems(deserialize("{}"), compile("/")))
                .containsExactly(deserialize("{}"));
    }

    @Test
    void whenGetItemsArray_thenAllReturned() {
        assertThat(mapper.getItems(deserialize(itemArray), compile("/items")))
                .containsExactly(deserialize(item1), deserialize(item2));
    }

    @Test
    void whenGetItemsNested_thenSingleReturned() {
        assertThat(mapper.getItems(deserialize(itemNested), compile("/items")))
                .containsExactly(deserialize(item1));
    }

    @Test
    void whenGetMissingItemsProperty_thenException() {
        assertThat(catchThrowable(() -> mapper.getItems(deserialize(itemArray), compile("/item-no"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenGetKey_thenKey() {
        assertThat(mapper.getKey(deserialize("{\"k1\":\"v1\"}"), compile("/k1")))
                .isEqualTo("v1");
    }

    @Test
    void whenGetMissingKey_thenException() {
        assertThat(catchThrowable(() -> mapper.getKey(deserialize("{\"k1\":\"v1\"}"), compile("/k2"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenGetValue_thenValue() {
        assertThat(mapper.getValue(deserialize("{\"k1\":\"v1\"}"), compile("/k1")))
                .isEqualTo(deserialize("{\"k1\":\"v1\"}").at("/k1"));
    }

    @Test
    void whenGetMissingValue_thenException() {
        assertThat(catchThrowable(() -> mapper.getValue(deserialize("{\"k1\":\"v1\"}"), compile("/k2"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenGetTimestamp_thenTimestamp() {
        assertThat(mapper.getTimestamp(deserialize("{\"k1\": 42}"), compile("/k1")))
                .isEqualTo(42L);
    }

    @Test
    void whenGetOffset_thenOffset() {
        assertThat(mapper.getOffset(deserialize("{\"k1\":\"v1\"}"), ImmutableMap.of("key", compile("/k1"))))
                .isEqualTo(ImmutableMap.of("key", "v1"));
    }

    interface Fixture {
        ObjectMapper mapper = new ObjectMapper();
        String item1 = "{\"k1\":\"v1\"}";
        String item2 = "{\"k2\":\"v2\"}";
        String itemArray = "{\"items\":[" + item1 + "," + item2 + "]}";
        String itemNested = "{\"items\":" + item1 + "}";

        @SneakyThrows
        static JsonNode deserialize(String body) {
            return mapper.readTree(body);
        }
    }
}
