package com.github.castorm.kafka.connect.http.poll;

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

import com.github.castorm.kafka.connect.http.poll.IntervalDelayPollInterceptor.Sleeper;
import org.apache.kafka.connect.source.SourceRecord;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class IntervalDelayPollInterceptorTest {

    @InjectMocks
    IntervalDelayPollInterceptor interceptor;

    @Mock
    Function<Map<String, ?>, IntervalDelayPollInterceptorConfig> configFactory;

    @Mock
    IntervalDelayPollInterceptorConfig config;

    @Mock
    Sleeper sleeper;

    @Captor
    ArgumentCaptor<Long> sleepMillis;

    @BeforeEach
    void setUp() {
        lenient().when(configFactory.apply(any())).thenReturn(config);
    }

    @Test
    void givenUpToDate_whenBeforePoll_thenSleep() throws InterruptedException {

        interceptor.upToDate = true;

        interceptor.beforePoll();

        then(sleeper).should().sleep(sleepMillis.capture());

        assertThat(sleepMillis.getValue()).isCloseTo(60000L, Offset.offset(1000L));
    }

    @Test
    void givenNotUpToDate_whenBeforePoll_thenDoNothing() throws InterruptedException {

        interceptor.upToDate = false;

        interceptor.beforePoll();

        then(sleeper).should(never()).sleep(any());
    }

    @Test
    void whenAfterPollEmpty_thenUpToDate() {

        interceptor.afterPoll(emptyList());

        assertThat(interceptor.upToDate).isTrue();
    }

    @Test
    void whenAfterPollNotEmpty_thenNotUpToDate() {

        interceptor.afterPoll(asList(new SourceRecord(null, null, null, null, null)));

        assertThat(interceptor.upToDate).isFalse();
    }
}
