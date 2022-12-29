package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * Kafka Connect HTTP
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

import com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.ASC;
import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.DESC;
import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.IMPLICIT;
import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorter.OrderDirection.ASC_FORCED_BY_TIMESTAMP;
import static com.github.castorm.kafka.connect.http.record.OrderDirectionSourceRecordSorterTest.Fixture.*;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderDirectionSourceRecordSorterTest {

    OrderDirectionSourceRecordSorter sorter;

    @Mock
    OrderDirectionSourceRecordSorterConfig config;

    @Test
    void givenAsc_whenOrderedRecords_thenAsIs() {

        givenDirection(ASC);

        assertThat(sorter.sort(ordered)).containsExactly(older, mid, newer);
    }

    @Test
    void givenAsc_whenReverseOrderedRecords_thenAsIs() {

        givenDirection(ASC);

        assertThat(sorter.sort(reverseOrdered)).containsExactly(newer, mid, older);
    }

    @Test
    void givenDesc_whenOrderedRecords_thenReversed() {

        givenDirection(DESC);

        assertThat(sorter.sort(ordered)).containsExactly(newer, mid, older);
    }

    @Test
    void givenDesc_whenReverseOrderedRecords_thenReversed() {

        givenDirection(DESC);

        assertThat(sorter.sort(reverseOrdered)).containsExactly(older, mid, newer);
    }

    @Test
    void givenImplicit_whenOrderedRecords_thenAsIs() {

        givenDirection(IMPLICIT);

        assertThat(sorter.sort(ordered)).containsExactly(older, mid, newer);
    }

    @Test
    void givenImplicit_whenReverseOrderedRecords_thenAsIs() {

        givenDirection(IMPLICIT);

        assertThat(sorter.sort(reverseOrdered)).containsExactly(older, mid, newer);
    }

    @Test
    void givenAscByTimestamp_whenOrderedRecords_thenAsIs() {

        givenDirection(ASC_FORCED_BY_TIMESTAMP);

        assertThat(sorter.sort(ordered)).containsExactly(older, mid, newer);
    }

    @Test
    void givenAscByTimestamp_whenReverseOrderedRecords_thenAsIs() {

        givenDirection(ASC_FORCED_BY_TIMESTAMP);

        assertThat(sorter.sort(reverseOrdered)).containsExactly(older, mid, newer);
    }

    @Test
    void givenAscByTimestamp_whenUnOrderedRecords_thenAsIs() {

        givenDirection(ASC_FORCED_BY_TIMESTAMP);

        assertThat(sorter.sort(unordered)).containsExactly(older, mid, newer);
    }

    private void givenDirection(OrderDirection asc) {
        sorter = new OrderDirectionSourceRecordSorter(__ -> config);
        given(config.getOrderDirection()).willReturn(asc);
        sorter.configure(Collections.emptyMap());
    }

    interface Fixture {
        SourceRecord older = new SourceRecord(null, null, null, null, null, null, null, null, MIN_VALUE);
        SourceRecord mid = new SourceRecord(null, null, null, null, null, null, null, null, 0L);
        SourceRecord newer = new SourceRecord(null, null, null, null, null, null, null, null, MAX_VALUE);
        List<SourceRecord> ordered = asList(older, mid, newer);

        List<SourceRecord> unordered = asList(older, newer, mid);
        List<SourceRecord> reverseOrdered = asList(newer, mid, older);
    }
}
