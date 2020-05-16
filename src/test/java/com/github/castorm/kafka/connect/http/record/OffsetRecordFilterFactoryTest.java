package com.github.castorm.kafka.connect.http.record;

import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.record.spi.SourceRecordFilterFactory;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;

import static com.github.castorm.kafka.connect.http.record.OffsetRecordFilterFactoryTest.Fixture.record;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OffsetRecordFilterFactoryTest {

    OffsetRecordFilterFactory factory;

    @Mock
    SourceRecordFilterFactory delegate;

    @BeforeEach
    void setUp() {
        given(delegate.create(any())).willReturn(__ -> false);
        factory = new OffsetRecordFilterFactory(delegate);
    }

    @Test
    void givenNotSeen_whenCreateAndTest_thenFalse() {
        assertThat(factory.create(Offset.of(ImmutableMap.of("i", 5))).test(record(3))).isFalse();
    }

    @Test
    void givenSeen_whenCreateAndTest_thenTrue() {

        Predicate<SourceRecord> predicate = factory.create(Offset.of(ImmutableMap.of("i", 5)));

        predicate.test(record(5));

        assertThat(predicate.test(record(6))).isTrue();
    }

    interface Fixture {
        static SourceRecord record(int index) {
            return new SourceRecord(null, ImmutableMap.of("i", index), null, null, null, null, null, null, 0L);
        }
    }
}
