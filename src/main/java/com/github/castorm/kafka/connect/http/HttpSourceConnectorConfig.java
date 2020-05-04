package com.github.castorm.kafka.connect.http;

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

import com.github.castorm.kafka.connect.http.client.okhttp.OkHttpClient;
import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.poll.IntervalDelayPollInterceptor;
import com.github.castorm.kafka.connect.http.poll.spi.PollInterceptor;
import com.github.castorm.kafka.connect.http.record.SchemedSourceRecordMapper;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordMapper;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;

@Getter
class HttpSourceConnectorConfig extends AbstractConfig {

    private static final String POLL_HOOKS = "http.source.poll.interceptor";
    private static final String CLIENT = "http.client";
    private static final String REQUEST_FACTORY = "http.source.request.factory";
    private static final String RESPONSE_PARSER = "http.source.response.parser";
    private static final String RECORD_MAPPER = "http.source.record.mapper";

    private final PollInterceptor pollInterceptor;
    private final HttpRequestFactory requestFactory;
    private final HttpClient client;
    private final HttpResponseParser responseParser;
    private final SourceRecordMapper recordMapper;

    HttpSourceConnectorConfig(Map<String, ?> originals) {
        super(config(), originals);
        pollInterceptor = getConfiguredInstance(POLL_HOOKS, PollInterceptor.class);
        requestFactory = getConfiguredInstance(REQUEST_FACTORY, HttpRequestFactory.class);
        client = getConfiguredInstance(CLIENT, HttpClient.class);
        responseParser = getConfiguredInstance(RESPONSE_PARSER, HttpResponseParser.class);
        recordMapper = getConfiguredInstance(RECORD_MAPPER, SourceRecordMapper.class);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(POLL_HOOKS, CLASS, IntervalDelayPollInterceptor.class, HIGH, "Poll Interceptor Class")
                .define(CLIENT, CLASS, OkHttpClient.class, HIGH, "Request Client Class")
                .define(REQUEST_FACTORY, CLASS, TemplateHttpRequestFactory.class, HIGH, "Request Factory Class")
                .define(RESPONSE_PARSER, CLASS, JacksonHttpResponseParser.class, HIGH, "Response Parser Class")
                .define(RECORD_MAPPER, CLASS, SchemedSourceRecordMapper.class, HIGH, "Record Mapper Class");
    }
}
