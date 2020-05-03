package com.github.castorm.kafka.connect.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VersionUtils {

    public static String getVersion(Class<?> clazz) {
        try {
            return clazz.getPackage().getImplementationVersion();
        } catch (Exception ex) {
            return "0.0.0.0";
        }
    }
}
