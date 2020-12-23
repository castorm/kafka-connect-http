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

import com.github.castorm.kafka.connect.http.response.timestamp.spi.TimestampParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.castorm.kafka.connect.http.response.timestamp.RegexTimestampParserTest.Fixture.regex;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class RegexTimestampParserTest {

  RegexTimestampParser parser;

  @Mock
  TimestampParser delegate;

  @Mock
  RegexTimestampParserConfig config;

  @BeforeEach
  void setUp() {

    parser = new RegexTimestampParser(__ -> config);
    given(config.getDelegateParser()).willReturn(delegate);
    given(config.getTimestampRegex()).willReturn(regex);
    parser.configure(emptyMap());
  }

  @Test
  void givenLongFormatter_whenParse_thenDelegated() {

    given(delegate.parse("123456789")).willReturn(ofEpochMilli(123456789L));

    assertThat(parser.parse("Date123456789")).isEqualTo(ofEpochMilli(123456789L));
  }

  @Test
  void givenFormatter_whenParse_thenDelegated() {

    parser.parse("Date2011-12-03T10:15:30+01");
    then(delegate).should().parse("2011-12-03T10:15:30+01");
  }

  @Test
  void givenNotNumber_whenParse_thenReturnedFromDelegate() {

    given(delegate.parse("2011-12-03T10:15:30+01")).willReturn(ofEpochMilli(123));

    assertThat(parser.parse("Date2011-12-03T10:15:30+01")).isEqualTo(ofEpochMilli(123));
  }

  interface Fixture {
    String regex = "(?:Date)(.*)";
  }
}
