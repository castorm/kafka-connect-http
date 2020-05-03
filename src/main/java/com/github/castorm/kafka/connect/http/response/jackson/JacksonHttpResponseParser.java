package com.github.castorm.kafka.connect.http.response.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class JacksonHttpResponseParser implements HttpResponseParser {

    private final ObjectMapper mapper;

    private final JacksonHttpResponseItemMapper itemMapper;

    private JsonPointer itemsPointer;
    private JsonPointer itemKeyPointer;
    private JsonPointer itemValuePointer;
    private JsonPointer itemTimestampPointer;
    private Map<String, JsonPointer> itemOffsets;

    public JacksonHttpResponseParser() {
        this.mapper = new ObjectMapper();
        this.itemMapper = new JacksonHttpResponseItemMapper();
    }

    @Override
    public void configure(Map<String, ?> configs) {
        JacksonHttpResponseParserConfig config = new JacksonHttpResponseParserConfig(configs);
        itemsPointer = config.getItemsPointer();
        itemKeyPointer = config.getItemKeyPointer();
        itemValuePointer = config.getItemValuePointer();
        itemTimestampPointer = config.getItemTimestampPointer();
        itemOffsets = config.getItemOffsets();
    }

    @Override
    public List<HttpResponseItem> parse(HttpResponse response) {

        JsonNode body = deserialize(response.getBody());

        return itemMapper.getItems(body, itemsPointer)
                .map(this::mapToItem)
                .collect(toList());
    }

    private HttpResponseItem mapToItem(JsonNode node) {

        return HttpResponseItem.builder()
                .key(itemMapper.getKey(node, itemKeyPointer))
                .value(serialize(itemMapper.getValue(node, itemValuePointer)))
                .timestamp(itemMapper.getTimestamp(node, itemTimestampPointer))
                .offset(itemMapper.getOffset(node, itemOffsets))
                .build();
    }

    @SneakyThrows(IOException.class)
    private JsonNode deserialize(byte[] body) {
        return mapper.readTree(body);
    }

    @SneakyThrows(IOException.class)
    private String serialize(JsonNode node) {
        return mapper.writeValueAsString(node);
    }
}
