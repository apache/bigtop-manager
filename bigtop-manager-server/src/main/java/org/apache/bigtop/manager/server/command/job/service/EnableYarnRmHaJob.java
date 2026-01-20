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
package org.apache.bigtop.manager.server.command.job.service;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;

import java.util.List;
import java.util.Map;

/**
 * Job for enabling YARN ResourceManager HA. It assumes yarn-site 已经写入 HA 配置，
 * 此 Job 只负责对 resourcemanager 组件执行
 *   CONFIGURE -> STOP -> START 三步，确保 HA RM 生效。
 * 若前端同时要求重启 NodeManager，可在 CommandDTO 的 componentCommands 中附带 "nodemanager"。
 */
public class EnableYarnRmHaJob extends AbstractServiceJob {

    public EnableYarnRmHaJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Map<String, List<String>> componentHostsMap = getComponentHostsMap();

        // 1. CONFIGURE resourcemanager (及可能的 nodemanager)
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, Command.CONFIGURE, commandDTO));
        // 2. STOP resourcemanager
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, Command.STOP, commandDTO));
        // 3. START resourcemanager
        stages.addAll(ComponentStageHelper.createComponentStages(componentHostsMap, Command.START, commandDTO));

        // 若没有 Stage 被创建（理论上不应该），抛异常
        if (stages.isEmpty()) {
            throw new IllegalStateException("EnableYarnRmHaJob has no stages to execute. Check componentHostsMap");
        }
    }

    @Override
    public String getName() {
        return "Enable YARN RM HA";
    }

    @Override
    protected Map<String, List<String>> getComponentHostsMap() {
        return jobContext.getCommandDTO().getComponentCommands().stream()
                .collect(java.util.stream.Collectors.toMap(
                        cc -> cc.getComponentName().toLowerCase(),
                        cc -> cc.getHostnames()
                ));
    }
}

