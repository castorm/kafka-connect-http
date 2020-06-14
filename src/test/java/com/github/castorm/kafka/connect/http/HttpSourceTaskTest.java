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

import com.github.castorm.kafka.connect.http.model.Offset;
import com.github.castorm.kafka.connect.http.model.Partition;
import com.github.castorm.kafka.connect.timer.spi.ManagedThrottler;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.HttpSourceTaskTest.Fixture.*;
import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
class HttpSourceTaskTest {

    HttpSourceTask task;

    @Mock
    HttpSourceTaskConfig config;

    @Mock
    ManagedThrottler throttler;

    @Mock
    HttpSourceTaskPartition taskPartition1;

    @Mock
    HttpSourceTaskPartition taskPartition2;

    @Mock
    SourceTaskContext context;

    @Mock
    OffsetStorageReader offsetStorageReader;

    @BeforeEach
    void setUp() {
        given(config.getThrottler()).willReturn(throttler);
        given(config.getPartitions()).willReturn(ImmutableMap.of(partition1, taskPartition1, partition2, taskPartition2));
        given(context.offsetStorageReader()).willReturn(offsetStorageReader);
        given(taskPartition1.getPartition()).willReturn(partition1);
        given(taskPartition2.getPartition()).willReturn(partition2);
        given(offsetStorageReader.offset(partition1.toMap())).willReturn((Map<String, Object>) offset1.toMap());
        given(offsetStorageReader.offset(partition2.toMap())).willReturn((Map<String, Object>) offset2.toMap());
        task = new HttpSourceTask(__ -> config);
        task.initialize(context);
        task.start(emptyMap());
        reset(taskPartition1, taskPartition2);
    }

    @Test
    void whenPoll_thenThrottlerShouldSleep() throws InterruptedException {

        task.poll();

        then(throttler).should().sleep();
    }

    @Test
    void whenPoll_thenCheckPartitionReady() throws InterruptedException {

        task.poll();

        then(taskPartition1).should().isReady();
    }

    @Test
    void givenPartitionsNotReady_whenPoll_thenPartitionsNotPolled() throws InterruptedException {

        given(taskPartition1.isReady()).willReturn(false);
        given(taskPartition2.isReady()).willReturn(false);

        task.poll();

        then(taskPartition1).should(never()).poll();
        then(taskPartition2).should(never()).poll();
    }

    @Test
    void givenPartitionReady_whenPoll_thenPartitionIsPolled() throws InterruptedException {

        given(taskPartition1.isReady()).willReturn(true);
        given(taskPartition2.isReady()).willReturn(false);

        task.poll();

        then(taskPartition1).should().poll();
    }

    @Test
    void givenPartitionReadyAndPollReturnsRecord_whenPoll_thenRecord() throws InterruptedException {

        given(taskPartition1.isReady()).willReturn(true);
        given(taskPartition2.isReady()).willReturn(false);
        given(taskPartition1.poll()).willReturn(singletonList(record(partition1, offset1)));

        assertThat(task.poll()).containsExactly(record(partition1, offset1));
    }

    @Test
    void givenTwoPartitionsReadyAndPollReturnsRecords_whenPoll_thenRecordsCombined() throws InterruptedException {

        given(taskPartition1.isReady()).willReturn(true);
        given(taskPartition2.isReady()).willReturn(true);
        given(taskPartition1.poll()).willReturn(singletonList(record(partition1, offset1)));
        given(taskPartition2.poll()).willReturn(singletonList(record(partition2, offset2)));

        assertThat(task.poll()).containsExactly(record(partition1, offset1), record(partition2, offset2));
    }

    @Test
    void givenPartitionReadyAndPollFailed_whenPoll_thenFailed() {

        given(taskPartition1.isReady()).willReturn(true);
        given(taskPartition2.isReady()).willReturn(false);
        given(taskPartition1.poll()).willThrow(new IllegalStateException());

        assertThat(Assertions.catchThrowable(() -> task.poll())).hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenCommitRecord_thenShouldSetOffsetInPartition() {

        task.commitRecord(record(partition1, offset1));

        then(taskPartition1).should().commit(offset1);
    }

    interface Fixture {
        Instant now = now();
        String key = "customKey";
        Partition partition1 = Partition.of(ImmutableMap.of("k", "v1"));
        Partition partition2 = Partition.of(ImmutableMap.of("k", "v2"));
        Offset offset1 = Offset.of(ImmutableMap.of("custom", "value1", "key", "customKey", "timestamp", now.toString()));
        Offset offset2 = Offset.of(ImmutableMap.of("custom", "value2", "key", "customKey", "timestamp", now.toString()));

        static SourceRecord record(Partition partition, Offset offset) {
            return new SourceRecord(partition.toMap(), offset.toMap(), null, null, null, null, null, null, now.toEpochMilli());
        }
    }
}
