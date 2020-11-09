package com.github.castorm.kafka.connect.common;

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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.Files.newInputStream;

@Slf4j
@UtilityClass
public class VersionUtils {

    private static final String DEFAULT_VERSION = "0.0.0.0";

    public static String getVersion() {
        try (InputStream input = openFile("version.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("version", DEFAULT_VERSION);
        } catch (Exception ex) {
            log.warn("Error loading version.properties, default version " + DEFAULT_VERSION + " will be used");
            return DEFAULT_VERSION;
        }
    }

    private static InputStream openFile(String fileName) throws IOException {
        InputStream stream = VersionUtils.class.getClassLoader().getResourceAsStream(fileName);
        return stream != null ? stream : newInputStream(Paths.get(fileName));
    }
}
