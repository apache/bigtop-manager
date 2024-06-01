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
package org.apache.bigtop.manager.stack.common.log;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class TaskLogWriter {

    private static Consumer<String> writer;

    public static void setWriter(Consumer<String> writer) {
        TaskLogWriter.writer = writer;
    }

    public static void info(String str) {
        if (TaskLogWriter.writer != null) {
            TaskLogWriter.writer.accept("[INFO ] " + getFormattedTime() + " - " + str);
        }
    }

    public static void warn(String str) {
        if (TaskLogWriter.writer != null) {
            TaskLogWriter.writer.accept("[WARN ] " + getFormattedTime() + " - " + str);
        }
    }

    public static void error(String str) {
        if (TaskLogWriter.writer != null) {
            TaskLogWriter.writer.accept("[ERROR] " + getFormattedTime() + " - " + str);
        }
    }

    private static String getFormattedTime() {
        ZoneId zoneId = ZoneOffset.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String time = zonedDateTime.format(formatter);
        String offset = zonedDateTime.getOffset().getId().replace("Z", "+00:00");

        return time + " " + offset;
    }

    public static void clearWriter() {
        TaskLogWriter.writer = null;
    }
}
