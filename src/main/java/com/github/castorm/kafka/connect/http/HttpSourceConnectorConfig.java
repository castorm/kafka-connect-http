package com.github.castorm.kafka.connect.http;

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
