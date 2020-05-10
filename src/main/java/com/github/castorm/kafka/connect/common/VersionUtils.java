package com.github.castorm.kafka.connect.common;

/*-
 * #%L
 * kafka-connect-http-plugin
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
        try (InputStream input = newInputStream(Paths.get("version.properties"))) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("version", DEFAULT_VERSION);
        } catch (IOException ex) {
            log.error("Error loading version.properties");
            return DEFAULT_VERSION;
        }
    }
}
