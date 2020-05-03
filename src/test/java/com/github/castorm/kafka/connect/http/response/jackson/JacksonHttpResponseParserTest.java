package com.github.castorm.kafka.connect.http.response.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.bytes;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.itemKeyPointer;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.itemOffsetKey;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.itemOffsetValuePointer;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.itemTimestampPointer;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.itemValuePointer;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.itemsPointer;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserTest.Fixture.response;
import static java.util.stream.Stream.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonHttpResponseParserTest {

    @InjectMocks
    JacksonHttpResponseParser parser;

    @Mock
    ObjectMapper mapper;

    @Mock
    JacksonHttpResponseItemMapper itemMapper;

    @Mock
    JsonNode root;

    @Mock
    JsonNode item;

    @Mock
    JsonNode value;

    @BeforeEach
    void setUp() {
        parser.configure(configMap());
    }

    private static Map<String, String> configMap() {
        return new HashMap<String, String>() {{
            put("http.source.response.json.items.pointer", itemsPointer);
            put("http.source.response.json.item.key.pointer", itemKeyPointer);
            put("http.source.response.json.item.value.pointer", itemValuePointer);
            put("http.source.response.json.item.timestamp.pointer", itemTimestampPointer);
            put("http.source.response.json.item.offset.value.pointer", itemOffsetValuePointer);
            put("http.source.response.json.item.offset.key", itemOffsetKey);
        }};
    }

    @Test
    void givenNoItems_thenEmpty() throws IOException {

        givenItems(empty());

        assertThat(parser.parse(response)).isEmpty();
    }

    @Test
    void givenOneItem_thenKeyMapped() throws IOException {

        givenItems(Stream.of(item));
        given(itemMapper.getKey(item, compile(itemKeyPointer))).willReturn("key");

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getKey).isEqualTo("key");
    }

    @Test
    void givenOneItem_thenValueMapped() throws IOException {

        givenItems(Stream.of(item));
        given(itemMapper.getValue(item, compile(itemValuePointer))).willReturn(value);
        given(mapper.writeValueAsString(value)).willReturn("value");

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getValue).isEqualTo("value");
    }

    @Test
    void givenOneItem_thenTimestampMapped() throws IOException {

        givenItems(Stream.of(item));
        given(itemMapper.getTimestamp(item, compile(itemTimestampPointer))).willReturn(42L);

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getTimestamp).isEqualTo(42L);
    }

    @Test
    void givenOneItem_thenOffsetMapped() throws IOException {

        givenItems(Stream.of(item));
        given(itemMapper.getOffset(item, ImmutableMap.of(itemOffsetKey, compile(itemOffsetValuePointer)))).willReturn(ImmutableMap.of("offset-key", "offset-value"));

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getOffset).isEqualTo(ImmutableMap.of("offset-key", "offset-value"));
    }

    private void givenItems(Stream<JsonNode> items) throws IOException {
        given(mapper.readTree(eq(bytes))).willReturn(root);
        given(itemMapper.getItems(eq(root), eq(compile(itemsPointer)))).willReturn(items);
    }

    interface Fixture {
        byte[] bytes = "bytes".getBytes();
        HttpResponse response = HttpResponse.builder().body(bytes).build();
        String itemsPointer = "/items";
        String itemKeyPointer = "/key";
        String itemValuePointer = "/value";
        String itemTimestampPointer = "/timestamp";
        String itemOffsetValuePointer = "/offset-value";
        String itemOffsetKey = "offset-key";
    }
}
