package com.github.castorm.kafka.connect.http.response;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import com.github.castorm.kafka.connect.http.model.HttpRecord;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.castorm.kafka.connect.http.response.StatusCodeFilterResponseParserTest.Fixture.record;
import static com.github.castorm.kafka.connect.http.response.StatusCodeFilterResponseParserTest.Fixture.response;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StatusCodeFilterResponseParserTest {

    StatusCodeFilterResponseParser parser;

    @Mock
    StatusCodeFilterResponseParserConfig config;

    @Mock
    HttpResponseParser delegate;

    @BeforeEach
    void setUp() {
        parser = new StatusCodeFilterResponseParser(__ -> config);
        given(config.getDelegateParser()).willReturn(delegate);
        parser.configure(emptyMap());
    }

    @Test
    void givenCode199_whenParse_thenIllegalState() {
        assertThat(catchThrowable(() -> parser.parse(response.withCode(199)))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenCode200_whenParse_thenDelegate() {

        parser.parse(response.withCode(200));

        then(delegate).should().parse(response.withCode(200));
    }

    @Test
    void givenCode200_whenParse_thenResponseFromDelegate() {

        given(delegate.parse(response.withCode(200))).willReturn(ImmutableList.of(record));

        assertThat(parser.parse(response.withCode(200))).containsExactly(record);
    }

    @Test
    void givenCode299_whenParse_thenDelegate() {

        parser.parse(response.withCode(299));

        then(delegate).should().parse(response.withCode(299));
    }

    @Test
    void givenCode300_whenParse_thenDoNothing() {

        parser.parse(response.withCode(300));

        then(delegate).should(never()).parse(any());
    }

    @Test
    void givenCode300_whenParse_thenEmptyList() {
        assertThat(parser.parse(response.withCode(300))).isEmpty();
    }

    @Test
    void givenCode400_whenParse_thenIllegalState() {
        assertThat(catchThrowable(() -> parser.parse(response.withCode(400)))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenCode500_whenParse_thenIllegalState() {
        assertThat(catchThrowable(() -> parser.parse(response.withCode(500)))).isInstanceOf(IllegalStateException.class);
    }

    interface Fixture {
        HttpResponse response = HttpResponse.builder().build();
        HttpRecord record = HttpRecord.builder().key("myRecord").build();
    }
}
