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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CaseUtilsTest {

    @Test
    public void testToLowerCase() {
        // Normal case
        assertEquals("hello world", CaseUtils.toLowerCase("Hello World"));
        // Input is an empty string
        assertEquals("", CaseUtils.toLowerCase(""));
        // Input is null
        assertNull(CaseUtils.toLowerCase(null));
    }

    @Test
    public void testToUpperCase() {
        // Normal case
        assertEquals("HELLO WORLD", CaseUtils.toUpperCase("Hello World"));
        // Input is an empty string
        assertEquals("", CaseUtils.toUpperCase(""));
        // Input is null
        assertNull(CaseUtils.toUpperCase(null));
    }

    @Test
    public void testToCamelCase() {
        // Normal case
        assertEquals("HelloWorld", CaseUtils.toCamelCase("hello-world"));
        // Using underscore separator
        assertEquals("HelloWorld", CaseUtils.toCamelCase("hello_world", CaseUtils.SEPARATOR_UNDERSCORE));
        // First letter lowercase
        assertEquals("helloWorld", CaseUtils.toCamelCase("hello-world", CaseUtils.SEPARATOR_HYPHEN, false));
        // Input is an empty string
        assertEquals("", CaseUtils.toCamelCase(""));
        // Input is null
        assertNull(CaseUtils.toCamelCase(null));
    }

    @Test
    public void testToHyphenCase() {
        // Normal case
        assertEquals("hello-world", CaseUtils.toHyphenCase("HelloWorld"));
        // Input is an empty string
        assertEquals("", CaseUtils.toHyphenCase(""));
        // Input is null
        assertNull(CaseUtils.toHyphenCase(null));
    }

    @Test
    public void testToUnderScoreCase() {
        // Normal case
        assertEquals("hello_world", CaseUtils.toUnderScoreCase("HelloWorld"));
        // Input is an empty string
        assertEquals("", CaseUtils.toUnderScoreCase(""));
        // Input is null
        assertNull(CaseUtils.toUnderScoreCase(null));
    }
}
