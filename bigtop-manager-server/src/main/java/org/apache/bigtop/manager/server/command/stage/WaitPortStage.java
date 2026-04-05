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
package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.server.command.task.PortCheckTask;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.command.task.TaskContext;

import java.util.List;

/**
 * A stage to wait for a given port to become available on a list of hosts.
 */
public class WaitPortStage extends AbstractStage {

    private final int port;
    private final long timeoutMs;
    private final long intervalMs;

    public WaitPortStage(StageContext stageContext, List<String> hosts, int port, long timeoutMs, long intervalMs) {
        super(stageContext);
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.intervalMs = intervalMs;
        this.stageContext.setHostnames(hosts);
    }

    @Override
    protected void beforeCreateTasks() {
        // No-op
    }

    @Override
    protected Task createTask(String hostname) {
        TaskContext taskContext = new TaskContext();
        taskContext.setClusterId(stageContext.getClusterId());
        taskContext.setClusterName(stageContext.getClusterName());
        taskContext.setHostname(hostname);
        taskContext.setServiceName(stageContext.getServiceName());
        taskContext.setComponentName(stageContext.getComponentName());
        taskContext.setComponentDisplayName(stageContext.getComponentName());
        taskContext.setUserGroup(stageContext.getUserGroup());
        taskContext.setRootDir(stageContext.getRootDir());

        return new PortCheckTask(taskContext, hostname, port, timeoutMs, intervalMs);
    }

    @Override
    public String getName() {
        return "Wait ports " + port;
    }
}
