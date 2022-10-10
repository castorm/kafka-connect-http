package com.github.castorm.kafka.connect.infra.client;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 Cástor Rodríguez
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

import io.reactivex.rxjava3.core.Observable;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@RequiredArgsConstructor
public class KafkaClient {

    private final String bootstrapServers;

    public Observable<ConsumerRecord<String, String>> observeTopic(String topic) {
        return Observable.create(emitter -> {
            AtomicBoolean running = new AtomicBoolean(true);
            emitter.setCancellable(() -> running.set(false));
            if (!emitter.isDisposed()) {
                try (KafkaConsumer<String, String> consumer = createKafkaConsumer(topic)) {
                    while (running.get()) {
                        ConsumerRecords<String, String> records1 = consumer.poll(ofSeconds(1));
                        stream(records1.spliterator(), false).forEach(emitter::onNext);
                    }
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });
    }

    private KafkaConsumer<String, String> createKafkaConsumer(String topic) {
        HashMap<String, Object> props = new HashMap<>();
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.assign(consumer.partitionsFor(topic).stream().map(info -> new TopicPartition(info.topic(), info.partition())).collect(toList()));
        return consumer;
    }
}
