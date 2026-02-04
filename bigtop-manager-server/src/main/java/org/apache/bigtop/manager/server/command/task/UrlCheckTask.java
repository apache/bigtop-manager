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
package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.enums.Command;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@Slf4j
public class UrlCheckTask extends AbstractTask {

    private final String targetUrl;
    private final String expectedContent;
    private final long timeoutMs;
    private final long intervalMs;

    public UrlCheckTask(
            TaskContext taskContext, String targetUrl, String expectedContent, long timeoutMs, long intervalMs) {
        super(taskContext);
        this.targetUrl = targetUrl;
        this.expectedContent = expectedContent;
        this.timeoutMs = timeoutMs;
        this.intervalMs = intervalMs;
    }

    @Override
    protected Command getCommand() {
        return Command.CUSTOM;
    }

    @Override
    protected String getCustomCommand() {
        return "url_check";
    }

    @Override
    protected Boolean doRun(String hostname, Integer grpcPort) {
        return true; // Server-side task
    }

    @Override
    public Boolean run() {
        log.info(
                "Starting URL check for [{}] with timeout {}ms, waiting for content: [{}]",
                targetUrl,
                timeoutMs,
                expectedContent);
        boolean isReady = waitForUrlContent();
        if (isReady) {
            log.info("URL [{}] is now ready.", targetUrl);
            onSuccess();
        } else {
            log.error("URL check timed out for [{}]. It did not become ready within {}ms.", targetUrl, timeoutMs);
            onFailure();
        }
        return isReady;
    }

    private boolean waitForUrlContent() {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable lastException = null;

        while (System.currentTimeMillis() < deadline) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(targetUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String response = reader.lines().collect(Collectors.joining());
                        if (response.contains(expectedContent)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                lastException = e;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("URL check for [{}] was interrupted.", targetUrl);
                return false;
            }
        }

        log.warn(
                "URL check failed for [{}]. Last error: {}",
                targetUrl,
                lastException != null ? lastException.getMessage() : "N/A");
        return false;
    }

    @Override
    public String getName() {
        return "Wait for URL " + targetUrl;
    }
}
