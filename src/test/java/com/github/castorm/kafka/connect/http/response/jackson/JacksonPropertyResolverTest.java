package com.github.castorm.kafka.connect.http.response.jackson;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonPropertyResolverTest.Fixture.array;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonPropertyResolverTest.Fixture.deserialize;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonPropertyResolverTest.Fixture.item1;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonPropertyResolverTest.Fixture.item2;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonPropertyResolverTest.Fixture.itemArray;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JacksonPropertyResolverTest {

    JacksonPropertyResolver resolver = new JacksonPropertyResolver();

    @Test
    void whenGetArrayAtPointerObject_thenObject() {
        assertThat(resolver.getArrayAt(deserialize(item1), compile("/"))).containsExactly(deserialize(item1));
    }

    @Test
    void whenGetArrayAtPointerArray_thenAllItems() {
        assertThat(resolver.getArrayAt(deserialize(array), compile("/"))).containsExactly(deserialize(item1), deserialize(item2));
    }

    @Test
    void whenGetArrayAtPointerItems_thenAllItems() {
        assertThat(resolver.getArrayAt(deserialize(itemArray), compile("/items"))).containsExactly(deserialize(item1), deserialize(item2));
    }

    @Test
    void whenGetObjectAtRoot_thenRoot() {
        assertThat(resolver.getObjectAt(deserialize(item1), compile("/"))).isEqualTo(deserialize(item1));
    }

    @Test
    void whenGetObjectAtProperty_thenProperty() {
        assertThat(resolver.getObjectAt(deserialize(item1), compile("/k1"))).isEqualTo(deserialize(item1).at("/k1"));
    }

    interface Fixture {
        ObjectMapper mapper = new ObjectMapper();
        String item1 = "{\"k1\":\"v1\"}";
        String item2 = "{\"k2\":\"v2\"}";
        String array = "[" + item1 + "," + item2 + "]";
        String itemArray = "{\"items\":[" + item1 + "," + item2 + "]}";

        @SneakyThrows
        static JsonNode deserialize(String body) {
            return mapper.readTree(body);
        }
    }
}
