package com.github.castorm.kafka.connect.common;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.delete;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class VersionUtilsTest {

    private static final Path VERSION_PROPERTIES = Paths.get("version.properties");

    @Test
    void givenNoFile_thenDefaultVersion() throws IOException {

        deleteIfExists(VERSION_PROPERTIES);

        assertThat(VersionUtils.getVersion()).isEqualTo("0.0.0.0");
    }

    @Test
    void givenFileWithoutVersion_thenFileVersion() throws IOException {

        write(VERSION_PROPERTIES, emptyList(), CREATE_NEW);

        String version = VersionUtils.getVersion();

        delete(VERSION_PROPERTIES);

        assertThat(version).isEqualTo("0.0.0.0");
    }

    @Test
    void givenFileWithVersion_thenFileVersion() throws IOException {

        write(VERSION_PROPERTIES, singletonList("version=1.2.3.4"), CREATE_NEW);

        String version = VersionUtils.getVersion();

        delete(VERSION_PROPERTIES);

        assertThat(version).isEqualTo("1.2.3.4");
    }
}
