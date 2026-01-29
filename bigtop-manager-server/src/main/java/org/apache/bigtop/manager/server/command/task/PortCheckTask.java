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

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A server-side task to check if a specific port on a target host is open and listening.
 * It retries until a timeout is reached.
 */
@Slf4j
public class PortCheckTask extends AbstractTask {

    private final String targetHost;
    private final int targetPort;
    private final long timeoutMs;
    private final long intervalMs;

    public PortCheckTask(TaskContext taskContext, String targetHost, int targetPort, long timeoutMs, long intervalMs) {
        super(taskContext);
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.timeoutMs = timeoutMs;
        this.intervalMs = intervalMs;
    }

    @Override
    protected Command getCommand() {
        // This is a server-side task, not sent to an agent.
        return Command.CUSTOM;
    }

    @Override
    protected String getCustomCommand() {
        return "port_check";
    }

    @Override
    protected Boolean doRun(String hostname, Integer grpcPort) {
        // The logic is entirely within the run() method as it executes on the server.
        return true;
    }

    /**
     * Executes the port check logic directly on the server.
     */
    @Override
    public Boolean run() {
        log.info("Starting port check for {}:{} with timeout {}ms", targetHost, targetPort, timeoutMs);
        boolean isPortOpen = waitForPortOpen();
        if (isPortOpen) {
            log.info("Port {}:{} is now open.", targetHost, targetPort);
            onSuccess();
        } else {
            log.error("Port check timed out for {}:{}. It did not become available within {}ms.", targetHost, targetPort, timeoutMs);
            onFailure();
        }
        return isPortOpen;
    }

    private boolean waitForPortOpen() {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable lastException = null;

        while (System.currentTimeMillis() < deadline) {
            try (Socket socket = new Socket()) {
                // Use a short connect timeout for each attempt
                socket.connect(new InetSocketAddress(targetHost, targetPort), 2000);
                return true;
            } catch (Exception e) {
                lastException = e;
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Port check for {}:{} was interrupted.", targetHost, targetPort);
                    return false;
                }
            }
        }

        log.warn("Port check failed for {}:{}. Last error: {}", targetHost, targetPort, lastException != null ? lastException.getMessage() : "N/A");
        return false;
    }

    @Override
    public String getName() {
        return "Wait for port " + targetHost + ":" + targetPort;
    }
}

