package com.github.castorm.kafka.connect.http;

/*-
 * #%L
 * kafka-connect-http
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.castorm.kafka.connect.http.client.okhttp.OkHttpClient;
import com.github.castorm.kafka.connect.http.client.spi.HttpClient;
import com.github.castorm.kafka.connect.http.record.OffsetRecordFilterFactory;
import com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordSorter;
import com.github.castorm.kafka.connect.http.request.spi.HttpRequestFactory;
import com.github.castorm.kafka.connect.http.request.template.TemplateHttpRequestFactory;
import com.github.castorm.kafka.connect.http.response.PolicyHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.timer.AdaptableIntervalTimer;
import com.github.castorm.kafka.connect.timer.TimerThrottler;
import com.github.castorm.kafka.connect.timer.spi.Timer;
import lombok.Getter;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static com.github.castorm.kafka.connect.common.ConfigUtils.breakDownMap;
import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Importance.LOW;
import static org.apache.kafka.common.config.ConfigDef.Type.STRING;
import static org.apache.kafka.common.config.ConfigDef.Type.CLASS;
import static org.apache.kafka.common.config.ConfigDef.Type.BOOLEAN;

@Getter
class HttpSourceConnectorConfig extends AbstractConfig {

    private static final String TIMER = "http.timer";
    private static final String CLIENT = "http.client";
    private static final String REQUEST_FACTORY = "http.request.factory";
    private static final String RESPONSE_PARSER = "http.response.parser";
    private static final String RECORD_SORTER = "http.record.sorter";
    private static final String RECORD_FILTER_FACTORY = "http.record.filter.factory";
    private static final String OFFSET_INITIAL = "http.offset.initial";
    private static final String HANDLE_PAGINATION = "http.request.pagination.handle";
    private static final String NEXT_PAGE_URL_MODE = "http.request.pagination.mode";
    private static final String NEXT_PAGE_BASE_URL = "http.request.pagination.baseurl";
    private static final String OVERWRITE = "overwrite";
    private static final String APPEND = "append";

    private final TimerThrottler throttler;
    private final HttpRequestFactory requestFactory;
    private final HttpClient client;
    private final HttpResponseParser responseParser;
    private final SourceRecordFilterFactory recordFilterFactory;
    private final SourceRecordSorter recordSorter;
    private final Map<String, String> initialOffset;
    private final Boolean handlePagination;
    private final Boolean appendNextPageUrl;
    private final String baseUrl;

    HttpSourceConnectorConfig(Map<String, ?> originals) {
        super(config(), originals);
        Timer timer = getConfiguredInstance(TIMER, Timer.class);
        throttler = new TimerThrottler(timer);
        requestFactory = getConfiguredInstance(REQUEST_FACTORY, HttpRequestFactory.class);
        client = getConfiguredInstance(CLIENT, HttpClient.class);
        responseParser = getConfiguredInstance(RESPONSE_PARSER, HttpResponseParser.class);
        recordSorter = getConfiguredInstance(RECORD_SORTER, SourceRecordSorter.class);
        recordFilterFactory = getConfiguredInstance(RECORD_FILTER_FACTORY, SourceRecordFilterFactory.class);
        initialOffset = breakDownMap(getString(OFFSET_INITIAL));
        handlePagination = getBoolean(HANDLE_PAGINATION);
        appendNextPageUrl = getString(NEXT_PAGE_URL_MODE).equalsIgnoreCase(APPEND);
        baseUrl = getString(NEXT_PAGE_BASE_URL);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(TIMER, CLASS, AdaptableIntervalTimer.class, HIGH, "Poll Timer Class")
                .define(CLIENT, CLASS, OkHttpClient.class, HIGH, "Request Client Class")
                .define(REQUEST_FACTORY, CLASS, TemplateHttpRequestFactory.class, HIGH, "Request Factory Class")
                .define(RESPONSE_PARSER, CLASS, PolicyHttpResponseParser.class, HIGH, "Response Parser Class")
                .define(RECORD_SORTER, CLASS, OrderDirectionSourceRecordSorter.class, LOW, "Record Sorter Class")
                .define(RECORD_FILTER_FACTORY, CLASS, OffsetRecordFilterFactory.class, LOW, "Record Filter Factory Class")
                .define(OFFSET_INITIAL, STRING, "", HIGH, "Starting offset")
                .define(HANDLE_PAGINATION, BOOLEAN, false, LOW, "Handle Pagination")
                .define(NEXT_PAGE_URL_MODE, STRING, OVERWRITE, ConfigDef.ValidString.in(APPEND, OVERWRITE), LOW, "Append or overwrite the next page URL")
                .define(NEXT_PAGE_BASE_URL, STRING, "", LOW, "Base URL in case of append mode");
    }
}
