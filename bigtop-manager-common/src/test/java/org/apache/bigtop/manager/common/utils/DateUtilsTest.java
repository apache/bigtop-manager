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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateUtilsTest {

    @Test
    public void testFormatTimestamp() {
        Timestamp timestamp = Timestamp.valueOf("2023-01-01 12:00:00");

        String formatted = DateUtils.format(timestamp);
        assertEquals("2023-01-01 12:00:00", formatted);

        String customFormatted = DateUtils.format(timestamp, "yyyy-MM-dd");
        assertEquals("2023-01-01", customFormatted);
    }

    @Test
    public void testFormatDate() {
        Date date = new Date(1672545600000L);
        TimeZone systemTimeZone = TimeZone.getDefault();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(systemTimeZone);
        String expectedFormatted = sdf.format(date);
        String formatted = DateUtils.format(date);
        assertEquals(expectedFormatted, formatted);

        SimpleDateFormat sdfSystem = new SimpleDateFormat("yyyy-MM-dd");
        sdfSystem.setTimeZone(systemTimeZone);
        String expectedCustomFormatted = sdfSystem.format(date);
        String customFormatted = DateUtils.format(date, "yyyy-MM-dd");
        assertEquals(expectedCustomFormatted, customFormatted);
    }

    @Test
    public void testFormatNullTimestamp() {
        String formatted = DateUtils.format((Timestamp) null);
        assertEquals("", formatted);
    }

    @Test
    public void testFormatNullDate() {
        String formatted = DateUtils.format((Date) null);
        assertEquals("", formatted);
    }

    @Test
    public void testFormatWithNullPattern() {
        Date date = new Date(1672531200000L);
        assertThrows(NullPointerException.class, () -> {
            DateUtils.format(date, null);
        });
    }

    @Test
    public void testFormatWithEmptyStringWhenDateIsNull() {
        String result = DateUtils.format((Date) null, "yyyy-MM-dd HH:mm:ss");
        assertEquals("", result);
    }
}
