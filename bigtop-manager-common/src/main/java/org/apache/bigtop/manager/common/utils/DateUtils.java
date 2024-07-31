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

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public final class DateUtils {

    /**
     * date format pattern of yyyy-MM-dd HH:mm:ss
     */
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateUtils() {
        throw new UnsupportedOperationException("Construct DateUtils");
    }

    public static String format(Timestamp timestamp) {
        return format(timestamp, DEFAULT_PATTERN);
    }

    public static String format(Timestamp timestamp, String pattern) {
        if (Objects.isNull(timestamp)) {
            return StringUtils.EMPTY;
        }

        return format(new Date(timestamp.getTime()), pattern);
    }

    public static String format(Date date) {
        return format(date, DEFAULT_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if (Objects.isNull(date)) {
            return StringUtils.EMPTY;
        }

        return date2LocalDateTime(date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * date to local datetime
     *
     * @param date   date
     * @param zoneId zoneId
     * @return local datetime
     */
    private static LocalDateTime date2LocalDateTime(Date date, ZoneId zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), zoneId);
    }
}
