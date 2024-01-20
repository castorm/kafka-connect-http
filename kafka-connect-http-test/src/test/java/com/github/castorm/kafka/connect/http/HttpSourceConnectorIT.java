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

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.github.castorm.kafka.connect.ConnectorUtils.getConfigMap;
import static com.github.castorm.kafka.connect.ConnectorUtils.readFileFromClasspath;
import static com.github.castorm.kafka.connect.ConnectorUtils.replaceVariables;
import static com.github.castorm.kafka.connect.KafkaConnectFake.readAllRecords;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class HttpSourceConnectorIT {

    WireMockServer wireMockServer;

    ImmutableMap<String, String> properties;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        properties = ImmutableMap.of("server.url", "localhost:" + wireMockServer.port());
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    void validateConnectorWithKafkaTemplateConfig() {
        Map<String, String> config = getConfigMap(replaceVariables(readFileFromClasspath("connectors/connector1.json"), properties));

        List<SourceRecord> records = readAllRecords(config);

        assertThat(records).hasSize(2);
        assertThat(records).extracting(SourceRecord::topic).containsExactly("topic-name1", "topic-name2");
        assertThat(records).extracting(record -> (String) record.sourceOffset().get("key")).containsExactly("TICKT-0002", "TICKT-0003");
        assertThat(records).extracting(record -> (String) record.sourceOffset().get("timestamp")).containsExactly("2020-01-01T00:00:02Z", "2020-01-01T00:00:03Z");
        assertThat(records).extracting(record -> (String) record.sourceOffset().get("index")).containsExactly("topic-name1", "topic-name2");
    }

    @Test
    void validateConnectorWithoutKafkaTemplateConfig() {

        Map<String, String> config = getConfigMap(replaceVariables(readFileFromClasspath("connectors/connector2.json"), properties));

        List<SourceRecord> records = readAllRecords(config);

        assertThat(records).hasSize(2);
        assertThat(records).extracting(SourceRecord::topic).allMatch(config.get("kafka.topic")::equals);
        assertThat(records).extracting(record -> (String) record.sourceOffset().get("key")).containsExactly("TICKT-0002", "TICKT-0003");
        assertThat(records).extracting(record -> (String) record.sourceOffset().get("timestamp")).containsExactly("2020-01-01T00:00:02Z", "2020-01-01T00:00:03Z");
    }
}
