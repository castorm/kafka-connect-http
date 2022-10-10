package com.github.castorm.kafka.connect;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;

@UtilityClass
public class ConnectorUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ObjectReader MAP_READER = OBJECT_MAPPER.readerFor(new TypeReference<Map<String, String>>() {
    });

    @SneakyThrows(URISyntaxException.class)
    public static String readFileFromClasspath(String fileName) {
        return readFile(Paths.get(ConnectorUtils.class.getClassLoader().getResource(fileName).toURI()));
    }

    public static String readFile(String fileName) {
        return readFile(Paths.get(fileName));
    }

    @SneakyThrows({IOException.class})
    public static String readFile(Path path) {
        return new String(readAllBytes(path));
    }

    public static String replaceVariables(String content, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
        }
        return content;
    }

    @SneakyThrows(IOException.class)
    public static Map<String, String> getConfigMap(String connectorConfigJson) {
        return MAP_READER.readValue(OBJECT_MAPPER.readTree(connectorConfigJson).get("config"));
    }
}
