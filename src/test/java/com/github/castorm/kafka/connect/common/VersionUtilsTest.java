package com.github.castorm.kafka.connect.common;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
