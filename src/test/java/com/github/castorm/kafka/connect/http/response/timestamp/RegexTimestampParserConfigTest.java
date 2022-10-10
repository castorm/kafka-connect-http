package com.github.castorm.kafka.connect.http.response.timestamp;

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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.castorm.kafka.connect.http.response.timestamp.RegexTimestampParserConfigTest.Fixture.config;
import static org.assertj.core.api.Assertions.assertThat;

public class RegexTimestampParserConfigTest {

  @Test
  void whenItemTimestampParserDelegateClassConfigured_thenInitialized() {
    assertThat(config(ImmutableMap.of("http.response.record.timestamp.parser.regex.delegate", "com.github.castorm.kafka.connect.http.response.timestamp.EpochMillisTimestampParser")).getDelegateParser())
        .isInstanceOf(EpochMillisTimestampParser.class);
    assertThat(config(ImmutableMap.of("http.response.record.timestamp.parser.regex.delegate", "com.github.castorm.kafka.connect.http.response.timestamp.EpochMillisTimestampParser")).getTimestampRegex())
        .isEqualTo(".*");
  }

  @Test
  void whenItemTimestampParserRegexConfigured_thenInitialized() {
    assertThat(config(ImmutableMap.of("http.response.record.timestamp.parser.regex", "(?:\\/Date\\()(.*?)(?:\\+0000\\)\\/)")).getDelegateParser())
        .isInstanceOf(DateTimeFormatterTimestampParser.class);
    assertThat(config(ImmutableMap.of("http.response.record.timestamp.parser.regex", "(?:\\/Date\\()(.*?)(?:\\+0000\\)\\/)")).getTimestampRegex())
        .isEqualTo("(?:\\/Date\\()(.*?)(?:\\+0000\\)\\/)");
  }


  interface Fixture {
    static RegexTimestampParserConfig config(Map<String, String> settings) {
      return new RegexTimestampParserConfig(settings);
    }
  }
}
