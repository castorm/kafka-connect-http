package com.github.castorm.kafka.connect.http.response.jackson;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.model.HttpResponseItem;
import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class JacksonHttpResponseParser implements HttpResponseParser {

    private final Function<Map<String, ?>, JacksonHttpResponseParserConfig> configFactory;

    private JacksonItemParser itemParser;

    private TimestampParser timestampParser;

    public JacksonHttpResponseParser() {
        this(JacksonHttpResponseParserConfig::new);
    }

    JacksonHttpResponseParser(Function<Map<String, ?>, JacksonHttpResponseParserConfig> configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        JacksonHttpResponseParserConfig config = configFactory.apply(configs);
        itemParser = config.getItemParser();
        timestampParser = config.getTimestampParser();
    }

    @Override
    public List<HttpResponseItem> parse(HttpResponse response) {

        return itemParser.getItems(response.getBody())
                .map(this::mapToItem)
                .collect(toList());
    }

    private HttpResponseItem mapToItem(JsonNode node) {

        Instant timestamp = itemParser.getTimestamp(node)
                .map(timestampParser::parse)
                .orElseGet(Instant::now);

        return HttpResponseItem.builder()
                .key(itemParser.getKey(node).orElseGet(() -> randomUUID().toString()))
                .value(itemParser.getValue(node))
                .offset(Offset.of(itemParser.getOffsets(node), timestamp))
                .build();
    }
}
