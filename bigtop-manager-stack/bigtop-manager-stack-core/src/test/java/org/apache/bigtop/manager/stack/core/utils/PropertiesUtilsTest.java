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
package org.apache.bigtop.manager.stack.core.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertiesUtilsTest {

    private File testFile;

    @BeforeEach
    public void setUp() throws IOException {
        testFile = File.createTempFile("test", ".properties");
    }

    @AfterEach
    public void tearDown() {
        if (testFile != null) {
            testFile.delete();
        }
    }

    @Test
    public void testWritePropertiesWithMap() throws IOException {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        configMap.put("key2", "value2");

        PropertiesUtils.writeProperties(testFile.getAbsolutePath(), configMap);

        Properties properties = new Properties();
        properties.load(new FileReader(testFile, StandardCharsets.UTF_8));

        assertEquals("value1", properties.getProperty("key1"));
        assertEquals("value2", properties.getProperty("key2"));
    }

    @Test
    public void testWritePropertiesWithList() throws IOException {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "key1");
        map1.put("value", "value1");

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "key2");
        map2.put("value", "value2");

        List<Map<String, Object>> configList = List.of(map1, map2);

        PropertiesUtils.writeProperties(testFile.getAbsolutePath(), configList);

        Properties properties = new Properties();
        properties.load(new FileReader(testFile, StandardCharsets.UTF_8));

        assertEquals("value1", properties.getProperty("key1"));
        assertEquals("value2", properties.getProperty("key2"));
    }

    @Test
    public void testReadProperties() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        configMap.put("key2", "value2");

        PropertiesUtils.writeProperties(testFile.getAbsolutePath(), configMap);

        Map<Object, Object> properties = PropertiesUtils.readProperties(testFile.getAbsolutePath());

        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
    }
}
