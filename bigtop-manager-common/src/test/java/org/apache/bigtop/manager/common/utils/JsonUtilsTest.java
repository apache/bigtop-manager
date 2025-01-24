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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class JsonUtilsTest {

    private static final String TEST_FILE_NAME = "test.json";
    private static final String TEST_JSON_STRING = "{\"name\":\"test\",\"value\":123}";
    private static final String TEST_JSON_STRING_NULL = "{\"name\":null,\"value\":123}";
    private static final String TEST_JSON_STRING_EMPTY = "{}";

    // Test file
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test file
        testFile = new File(TEST_FILE_NAME);
        if (!testFile.exists()) {
            Files.createFile(testFile.toPath());
        }
    }

    @AfterEach
    void tearDown() {
        // Delete test file
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    // Test writing an object to a file and reading it back
    @Test
    void testWriteAndReadToFile() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "test");
        testData.put("value", 123);

        JsonUtils.writeToFile(testFile, testData);

        Map<String, Object> readData = JsonUtils.readFromFile(testFile, new TypeReference<>() {});
        assertNotNull(readData);
        assertEquals("test", readData.get("name"));
        assertEquals(123, readData.get("value"));
    }

    // Test reading an object from a string
    @Test
    void testReadFromString() {
        Map<String, Object> readData = JsonUtils.readFromString(TEST_JSON_STRING, new TypeReference<>() {});
        assertNotNull(readData);
        assertEquals("test", readData.get("name"));
        assertEquals(123, readData.get("value"));
    }

    // Test reading an object from a string as a Map
    @Test
    void testReadFromStringToMap() {
        Map<String, Object> readData =
                JsonUtils.readFromString(TEST_JSON_STRING, new HashMap<String, Object>().getClass());
        assertNotNull(readData);
        assertEquals("test", readData.get("name"));
        assertEquals(123, readData.get("value"));
    }

    // Test reading a JSON tree
    @Test
    void testReadTree() {
        try {
            Files.writeString(testFile.toPath(), TEST_JSON_STRING);
            JsonNode jsonNode = JsonUtils.readTree(testFile.getName());
            assertNotNull(jsonNode);
            assertEquals("test", jsonNode.get("name").asText());
            assertEquals(123, jsonNode.get("value").asInt());
        } catch (IOException e) {
            log.error("IO exception occurred while reading the file", e);
            fail();
        }
    }

    // Test writing an object to a string
    @Test
    void testWriteAsString() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "test");
        testData.put("value", 123);

        String jsonString = JsonUtils.writeAsString(testData);
        assertNotNull(jsonString);
        assertEquals("{\"name\":\"test\",\"value\":123}", jsonString);
    }

    // Test writing an object to a pretty formatted string
    @Test
    void testIndentWriteAsString() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "test");
        testData.put("value", 123);

        String jsonString = JsonUtils.indentWriteAsString(testData);
        assertNotNull(jsonString);
        assertEquals("{\r\n  \"name\" : \"test\",\r\n  \"value\" : 123\r\n}", jsonString);
    }

    // Test edge case: object is null
    @Test
    void testWriteAndReadNull() {
        Map<String, Object> testData = null;

        String jsonString = JsonUtils.writeAsString(testData);
        assertNull(jsonString);

        Map<String, Object> readData = JsonUtils.readFromString(jsonString);
        assertNull(readData);
    }

    // Test edge case: JSON string is null
    @Test
    void testReadNullString() {
        Map<String, Object> readData = JsonUtils.readFromString(null, new TypeReference<>() {});
        assertNull(readData);
    }

    // Test edge case: JSON string is empty
    @Test
    void testReadEmptyString() {
        Map<String, Object> readData = JsonUtils.readFromString(TEST_JSON_STRING_EMPTY, new TypeReference<>() {});
        assertNotNull(readData);
        assertTrue(readData.isEmpty());
    }

    // Test edge case: JSON string contains a null value
    @Test
    void testReadStringWithNull() {
        Map<String, Object> readData = JsonUtils.readFromString(TEST_JSON_STRING_NULL, new TypeReference<>() {});
        assertNotNull(readData);
        assertNull(readData.get("name"));
        assertEquals(123, readData.get("value"));
    }

    // Test edge case: reading a non-existent file
    @Test
    void testReadNonexistentFile() {
        File nonExistentFile = new File("nonexistent.json");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            JsonUtils.readFromFile(nonExistentFile, new TypeReference<>() {});
        });

        assertNotNull(exception);
    }

    // Test edge case: reading an empty file
    @Test
    void testReadEmptyFile() {
        try {
            Files.writeString(testFile.toPath(), "");

            Exception exception = assertThrows(RuntimeException.class, () -> {
                JsonUtils.readFromFile(testFile, new TypeReference<>() {});
            });

            assertNotNull(exception);
        } catch (IOException e) {
            log.error("IO exception occurred while writing to the file", e);
            fail();
        }
    }

    // Test edge case: reading a file with invalid JSON content
    @Test
    void testReadInvalidJsonFile() {
        try {
            Files.writeString(testFile.toPath(), "invalid json");

            Exception exception = assertThrows(RuntimeException.class, () -> {
                JsonUtils.readFromFile(testFile, new TypeReference<>() {});
            });

            assertNotNull(exception);
        } catch (IOException e) {
            log.error("IO exception occurred while writing to the file", e);
            fail();
        }
    }

    // Test edge case: reading a pretty formatted JSON tree
    @Test
    void testReadIndentedTree() {
        try {
            String indentedJson = JsonUtils.indentWriteAsString(new HashMap<String, Object>() {
                {
                    put("name", "test");
                    put("value", 123);
                }
            });
            Files.writeString(testFile.toPath(), indentedJson);

            JsonNode jsonNode = JsonUtils.readTree(testFile.getName());
            assertNotNull(jsonNode);
            assertEquals("test", jsonNode.get("name").asText());
            assertEquals(123, jsonNode.get("value").asInt());
        } catch (IOException e) {
            log.error("IO exception occurred while reading the file", e);
            fail();
        }
    }
}
