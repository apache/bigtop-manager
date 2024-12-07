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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ProxyUtils {
    public static double getDoubleSafely(JsonNode parentNode, String key, int index) {
        JsonNode listNode = parentNode.get(key);
        if (listNode != null && listNode.isArray() && index < listNode.size())
            return listNode.get(index).asDouble();
        return 0.0;
    }

    public static Long getLongSafely(JsonNode parentNode, String key, int index) {
        JsonNode listNode = parentNode.get(key);
        if (listNode != null && listNode.isArray() && index < listNode.size())
            return listNode.get(index).asLong();
        return 0L;
    }

    public static JsonNode array2node(double[][] array, int num, int cores) {
        ObjectMapper mapper = new ObjectMapper();
        double[] cache = new double[6];
        for (int i = 0; i < num; i++) for (int j = 0; j < 6; j++) cache[j] += array[i][j];
        ArrayNode node = mapper.createArrayNode();
        // 数据排序为日期小在前，日期大在后
        for (int j = 0; j < 6; j++) node.add(cache[j] / cores);
        return node;
    }

    public static JsonNode array2node(double[] array) {
        ArrayNode node = new ObjectMapper().createArrayNode();
        for (int j = 0; j < 6; j++) node.add(array[j]);
        return node;
    }

    public static JsonNode array2node(long[] array) {
        ArrayNode node = new ObjectMapper().createArrayNode();
        for (int j = 0; j < 6; j++) node.add(array[j]);
        return node;
    }

    public static JsonNode array2node(long[] array1, long[] array2) {
        ArrayNode node = new ObjectMapper().createArrayNode();
        for (int j = 0; j < 6; j++)
            if (array2[j] <= 0) node.add(0.0);
            else node.add((double) (array2[j] - array1[j]) / array2[j]);
        return node;
    }

    public static JsonNode array2node(long[][] array1, long[][] array2, int num) {
        ObjectMapper mapper = new ObjectMapper();
        long[] cache1 = new long[6];
        long[] cache2 = new long[6];
        for (int i = 0; i < num; i++) {
            for (int j = 0; j < 6; j++) {
                cache1[j] += array1[i][j];
                cache2[j] += array2[i][j];
            }
        }
        ArrayNode node = mapper.createArrayNode();
        // The data is sorted with earlier dates coming first and later dates following.
        for (int j = 0; j < 6; j++)
            if (cache2[j] <= 0) node.add(0.0);
            else node.add((double) (cache2[j] - cache1[j]) / cache2[j]);
        return node;
    }

    public static ArrayList<Long> getTimeStampsList(int step) {
        // format
        String currentTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDateTime = LocalDateTime.parse(currentDateStr + " " + currentTimeStr, formatter);
        int roundedMinute = (currentDateTime.getMinute() / step) * step;
        LocalDateTime roundedCurrentDateTime =
                currentDateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
        // get 8 point
        ArrayList<Long> timestamps = new ArrayList<>();
        ZoneId zid = ZoneId.systemDefault();
        for (int i = 0; i < 7; i++) {
            LocalDateTime pastTime = roundedCurrentDateTime.minus(Duration.ofMinutes((long) step * i));
            long timestamp = pastTime.atZone(zid).toInstant().toEpochMilli() / 1000L;
            timestamps.add(timestamp);
        }
        return timestamps;
    }

    public static String Number2Param(int step) {
        return String.format("%sm", step);
    }
}
