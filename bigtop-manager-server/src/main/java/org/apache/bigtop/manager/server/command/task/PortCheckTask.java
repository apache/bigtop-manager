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
 * Server-side task: wait for a TCP port to become reachable on a host.
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
        // Server-side only
        return Command.CUSTOM;
    }

    @Override
    protected String getCustomCommand() {
        return "port_check";
    }

    @Override
    protected Boolean doRun(String hostname, Integer grpcPort) {
        // Not used
        return true;
    }

    @Override
    public Boolean run() {
        boolean ok = waitForPortOpen(targetHost, targetPort, timeoutMs, intervalMs);
        if (ok) {
            onSuccess();
        } else {
            onFailure();
        }
        return ok;
    }

    private static boolean waitForPortOpen(String host, int port, long timeoutMs, long intervalMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable last = null;

        while (System.currentTimeMillis() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 2000);
                return true;
            } catch (Throwable t) {
                last = t;
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.warn("Port check timeout for {}:{}, lastError={}", host, port, last == null ? null : last.getMessage());
        return false;
    }

    @Override
    public String getName() {
        return "Wait port " + targetHost + ":" + targetPort;
    }
}
