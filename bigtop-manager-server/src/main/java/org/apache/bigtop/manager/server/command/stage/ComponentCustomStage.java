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

import org.apache.bigtop.manager.server.command.task.ComponentCustomTask;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Stage for component custom command.
 */
@Slf4j
public class ComponentCustomStage extends AbstractComponentStage {

    private final String customCommand;

    public ComponentCustomStage(StageContext stageContext, String customCommand) {
        super(stageContext);
        if (customCommand == null || customCommand.isBlank()) {
            throw new IllegalArgumentException(
                    "customCommand must not be blank for ComponentCustomStage, stageContext=" + stageContext);
        }
        this.customCommand = customCommand;
    }

    @Override
    protected Task createTask(String hostname) {
        log.info(
                "ComponentCustomStage.createTask: this={}, customCommand='{}', len={}, blank={}, stageContext={}, hostname={}",
                System.identityHashCode(this),
                customCommand,
                customCommand == null ? null : customCommand.length(),
                customCommand == null ? null : customCommand.isBlank(),
                getStageContext(),
                hostname);
        return new ComponentCustomTask(createTaskContext(hostname), customCommand);
    }

    @Override
    public String getName() {
        String componentDisplay =
                StackUtils.getComponentDTO(stageContext.getComponentName()).getDisplayName();
        String stageName = String.format("Custom: %s (%s)", componentDisplay, customCommand);
        // Limit the length to prevent DataTruncation
        return StringUtils.abbreviate(stageName, 32);
    }
}
