/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.common.utils;

public class CaseUtils {

    public static final String SEPARATOR_HYPHEN = "-";

    public static final String SEPARATOR_UNDERSCORE = "_";

    public static String toLowerCase(String input) {
        return input == null ? null : input.toLowerCase();
    }

    public static String toUpperCase(String input) {
        return input == null ? null : input.toUpperCase();
    }

    public static String toCamelCase(String input) {
        return toCamelCase(input, SEPARATOR_HYPHEN);
    }

    public static String toCamelCase(String input, String separator) {
        return toCamelCase(input, separator, true);
    }

    public static String toCamelCase(String input, String separator, Boolean capitalizeFirstLetter) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] parts = input.split(separator);
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && !capitalizeFirstLetter) {
                parts[i] = parts[i].toLowerCase();
            } else {
                parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1).toLowerCase();
            }
        }

        return String.join("", parts);
    }

    public static String toHyphenCase(String input) {
        return toSpecificCase(input, SEPARATOR_HYPHEN);
    }

    public static String toUnderScoreCase(String input) {
        return toSpecificCase(input, SEPARATOR_UNDERSCORE);
    }

    private static String toSpecificCase(String input, String separator) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1" + separator + "$2";
        String result = input.replaceAll(regex, replacement);
        return result.toLowerCase();
    }
}
