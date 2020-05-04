package com.github.castorm.kafka.connect.http.response.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
import static java.util.Collections.emptyMap;
import static java.util.stream.Stream.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JacksonHttpResponseParserTest {

    JacksonHttpResponseParser parser;

    @Mock
    Function<Map<String, ?>, JacksonHttpResponseParserConfig> configFactory;

    @Mock
    ObjectMapper mapper;

    @Mock
    JacksonHttpResponseItemMapper itemMapper;

    @Mock
    JacksonHttpResponseParserConfig config;

    @Mock
    JsonNode root;

    @Mock
    JsonNode item;

    @Mock
    JsonNode value;

    @BeforeEach
    void setUp() {
        parser = new JacksonHttpResponseParser(configFactory, mapper, itemMapper);
        given(configFactory.apply(any())).willReturn(config);
    }

    @Test
    void givenNoItems_thenEmpty() throws IOException {

        givenItems(empty());

        assertThat(parser.parse(response)).isEmpty();
    }

    @Test
    void givenOneItem_thenKeyMapped() throws IOException {

        given(config.getItemKeyPointer()).willReturn(Optional.of(itemKeyPointer));
        givenItems(Stream.of(item));
        given(itemMapper.getKey(item, itemKeyPointer)).willReturn("key");

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getKey).isEqualTo("key");
    }

    @Test
    void givenOneItem_thenValueMapped() throws IOException {

        given(config.getItemValuePointer()).willReturn(itemValuePointer);
        givenItems(Stream.of(item));
        given(itemMapper.getValue(item, itemValuePointer)).willReturn(value);
        given(mapper.writeValueAsString(value)).willReturn("value");

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getValue).isEqualTo("value");
    }

    @Test
    void givenOneItem_thenTimestampMapped() throws IOException {

        given(config.getItemTimestampPointer()).willReturn(Optional.of(itemTimestampPointer));
        givenItems(Stream.of(item));
        given(itemMapper.getTimestamp(item, itemTimestampPointer)).willReturn(42L);

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getTimestamp).isEqualTo(42L);
    }

    @Test
    void givenNoTimestampPointer_thenTimestampDefault() throws IOException {

        given(config.getItemTimestampPointer()).willReturn(Optional.empty());
        givenItems(Stream.of(item));

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getTimestamp).isNotNull();
    }

    @Test
    void givenOneItem_thenOffsetMapped() throws IOException {

        given(config.getItemOffsets()).willReturn(ImmutableMap.of(itemOffsetKey, itemOffsetValuePointer));
        givenItems(Stream.of(item));
        given(itemMapper.getOffset(item, ImmutableMap.of(itemOffsetKey, itemOffsetValuePointer))).willReturn(ImmutableMap.of("offset-key", "offset-value"));

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getOffset).isEqualTo(ImmutableMap.of("offset-key", "offset-value"));
    }

    @Test
    void givenNoKeyPointer_thenKeyDefault() throws IOException {

        given(config.getItemKeyPointer()).willReturn(Optional.empty());
        givenItems(Stream.of(item));

        assertThat(parser.parse(response)).first().extracting(HttpResponseItem::getKey).isNotNull();
    }

    private void givenItems(Stream<JsonNode> items) throws IOException {
        given(config.getItemsPointer()).willReturn(itemsPointer);
        parser.configure(emptyMap());
        given(mapper.readTree(eq(bytes))).willReturn(root);
        given(itemMapper.getItems(eq(root), eq(itemsPointer))).willReturn(items);
    }

    interface Fixture {
        byte[] bytes = "bytes".getBytes();
        HttpResponse response = HttpResponse.builder().body(bytes).build();
        JsonPointer itemsPointer = compile("/items");
        JsonPointer itemKeyPointer = compile("/key");
        JsonPointer itemValuePointer = compile("/value");
        JsonPointer itemTimestampPointer = compile("/timestamp");
        JsonPointer itemOffsetValuePointer = compile("/offset-value");
        String itemOffsetKey = "offset-key";
    }
}
