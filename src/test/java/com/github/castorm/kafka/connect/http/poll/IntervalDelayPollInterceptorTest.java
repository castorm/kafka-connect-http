package com.github.castorm.kafka.connect.http.poll;

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
