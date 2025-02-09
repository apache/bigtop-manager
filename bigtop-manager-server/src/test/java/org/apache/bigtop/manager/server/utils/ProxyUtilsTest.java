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
package org.apache.bigtop.manager.server.utils;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyUtilsTest {

    @Test
    // Test getDoubleSafely with normal case
    public void testGetDoubleSafelyHappyPath() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"values\":[1.1, 2.2, 3.3]}";
        JsonNode parentNode = mapper.readTree(jsonString);
        assertEquals(2.2, ProxyUtils.getDoubleSafely(parentNode, "values", 1));
    }

    @Test
    // Test getDoubleSafely with null node case
    public void testGetDoubleSafelyNullNode() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode parentNode = mapper.createObjectNode();
        assertEquals(0.0, ProxyUtils.getDoubleSafely(parentNode, "values", 1));
    }

    @Test
    // Test getDoubleSafely with non-array node case
    public void testGetDoubleSafelyNotArrayNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"values\":1.1}";
        JsonNode parentNode = mapper.readTree(jsonString);
        assertEquals(0.0, ProxyUtils.getDoubleSafely(parentNode, "values", 1));
    }

    @Test
    // Test getDoubleSafely with index out of bounds case
    public void testGetDoubleSafelyIndexOutOfBound() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"values\":[1.1, 2.2, 3.3]}";
        JsonNode parentNode = mapper.readTree(jsonString);
        assertEquals(0.0, ProxyUtils.getDoubleSafely(parentNode, "values", 5));
    }

    @Test
    // Test getLongSafely with normal case
    public void testGetLongSafelyHappyPath() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"values\":[1, 2, 3]}";
        JsonNode parentNode = mapper.readTree(jsonString);
        assertEquals(2L, ProxyUtils.getLongSafely(parentNode, "values", 1));
    }

    @Test
    // Test getLongSafely with null node case
    public void testGetLongSafelyNullNode() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode parentNode = mapper.createObjectNode();
        assertEquals(0L, ProxyUtils.getLongSafely(parentNode, "values", 1));
    }

    @Test
    // Test getLongSafely with non-array node case
    public void testGetLongSafelyNotArrayNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"values\":1}";
        JsonNode parentNode = mapper.readTree(jsonString);
        assertEquals(0L, ProxyUtils.getLongSafely(parentNode, "values", 1));
    }

    @Test
    // Test getLongSafely with index out of bounds case
    public void testGetLongSafelyIndexOutOfBound() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"values\":[1, 2, 3]}";
        JsonNode parentNode = mapper.readTree(jsonString);
        assertEquals(0L, ProxyUtils.getLongSafely(parentNode, "values", 5));
    }

    @Test
    // Test array2node(double[] array) with normal case
    public void testArray2NodeDoubleArrayHappyPath() {
        double[] array = {1.123456789, 2.23456789, 3.3456789, 4.456789, 5.56789, 6.6789};
        JsonNode node = ProxyUtils.array2node(array);
        assertEquals("1.123457", node.get(0).asText());
        assertEquals("2.234568", node.get(1).asText());
        assertEquals("3.345679", node.get(2).asText());
        assertEquals("4.456789", node.get(3).asText());
        assertEquals("5.567890", node.get(4).asText());
        assertEquals("6.678900", node.get(5).asText());
    }

    @Test
    // Test array2node(long[] array) with normal case
    public void testArray2NodeLongArrayHappyPath() {
        long[] array = {1, 2, 3, 4, 5, 6};
        JsonNode node = ProxyUtils.array2node(array);
        assertEquals(1L, node.get(0).asLong());
        assertEquals(2L, node.get(1).asLong());
        assertEquals(3L, node.get(2).asLong());
        assertEquals(4L, node.get(3).asLong());
        assertEquals(5L, node.get(4).asLong());
        assertEquals(6L, node.get(5).asLong());
    }

    @Test
    // Test array2node(long[] array1, long[] array2) with normal case
    public void testArray2NodeTwoLongArraysHappyPath() {
        long[] array1 = {1, 2, 3, 4, 5, 6};
        long[] array2 = {2, 4, 6, 8, 10, 12};
        JsonNode node = ProxyUtils.array2node(array1, array2);
        assertEquals("0.500000", node.get(0).asText());
        assertEquals("0.500000", node.get(1).asText());
        assertEquals("0.500000", node.get(2).asText());
        assertEquals("0.500000", node.get(3).asText());
        assertEquals("0.500000", node.get(4).asText());
        assertEquals("0.500000", node.get(5).asText());
    }

    @Test
    // Test array2node(long[] array1, long[] array2) with array2 containing zero case
    public void testArray2NodeTwoLongArraysWithZero() {
        long[] array1 = {1, 2, 3, 4, 5, 6};
        long[] array2 = {0, 4, 0, 8, 10, 0};
        JsonNode node = ProxyUtils.array2node(array1, array2);
        assertEquals("0.000000", node.get(0).asText());
        assertEquals("0.500000", node.get(1).asText());
        assertEquals("0.000000", node.get(2).asText());
        assertEquals("0.500000", node.get(3).asText());
        assertEquals("0.500000", node.get(4).asText());
        assertEquals("0.000000", node.get(5).asText());
    }

    @Test
    // Test array2node(long[][] array1, long[][] array2, int num) with normal case
    public void testArray2NodeTwoLong2DArraysHappyPath() {
        long[][] array1 = {{1, 2, 3, 4, 5, 6}, {2, 3, 4, 5, 6, 7}};
        long[][] array2 = {{2, 4, 6, 8, 10, 12}, {3, 5, 6, 8, 10, 12}};
        JsonNode node = ProxyUtils.array2node(array1, array2, 2);
        assertEquals("0.400000", node.get(0).asText());
        assertEquals("0.444444", node.get(1).asText());
        assertEquals("0.416667", node.get(2).asText());
        assertEquals("0.437500", node.get(3).asText());
        assertEquals("0.450000", node.get(4).asText());
        assertEquals("0.458333", node.get(5).asText());
    }

    @Test
    // Test array2node(long[][] array1, long[][] array2, int num) with array2 containing zero case
    public void testArray2NodeTwoLong2DArraysWithZero() {
        long[][] array1 = {{1, 2, 3, 4, 5, 6}, {2, 3, 4, 5, 6, 7}};
        long[][] array2 = {{2, 4, 0, 8, 10, 0}, {3, 5, 0, 8, 10, 0}};
        JsonNode node = ProxyUtils.array2node(array1, array2, 2);
        assertEquals("0.400000", node.get(0).asText());
        assertEquals("0.444444", node.get(1).asText());
        assertEquals("0.000000", node.get(2).asText());
        assertEquals("0.437500", node.get(3).asText());
        assertEquals("0.450000", node.get(4).asText());
        assertEquals("0.000000", node.get(5).asText());
    }

    @Test
    // Test getTimeStampsList with normal case
    public void testGetTimeStampsListHappyPath() {
        ArrayList<Long> timestamps = ProxyUtils.getTimeStampsList(10);
        assertEquals(7, timestamps.size());
        // Here we cannot precisely assert the value of each timestamp as they depend on the current time, but we can
        // assert the number of timestamps
    }

    @Test
    // Test Number2Param with normal case
    public void testNumber2ParamHappyPath() {
        assertEquals("10m", ProxyUtils.Number2Param(10));
    }

    @Test
    // Test processInternal with normal case
    public void testProcessInternalHappyPath() {
        assertEquals(600, ProxyUtils.processInternal("10m"));
        assertEquals(3600, ProxyUtils.processInternal("1h"));
        assertEquals(120, ProxyUtils.processInternal("2m"));
    }

    @Test
    // Test processInternal with illegal input case
    public void testProcessInternalIllegalInput() {
        Exception exception = assertThrows(NumberFormatException.class, () -> {
            ProxyUtils.processInternal("abc");
        });
        assertNotNull(exception);
    }
}
