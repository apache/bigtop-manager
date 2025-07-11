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
package org.apache.bigtop.manager.server.command.helper;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.command.stage.ComponentAddStage;
import org.apache.bigtop.manager.server.command.stage.ComponentCheckStage;
import org.apache.bigtop.manager.server.command.stage.ComponentConfigureStage;
import org.apache.bigtop.manager.server.command.stage.ComponentInitStage;
import org.apache.bigtop.manager.server.command.stage.ComponentPrepareStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStopStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ComponentStageHelper {

    public static List<Stage> createComponentStages(Map<String, List<String>> componentHosts, CommandDTO commandDTO) {
        return createComponentStages(componentHosts, commandDTO.getCommand(), commandDTO);
    }

    public static List<Stage> createComponentStages(
            Map<String, List<String>> componentHosts, Command command, CommandDTO commandDTO) {
        return createComponentStages(componentHosts, List.of(command), commandDTO);
    }

    public static List<Stage> createComponentStages(
            Map<String, List<String>> componentHosts, List<Command> commands, CommandDTO commandDTO) {
        List<Stage> stages = new ArrayList<>();
        List<String> componentNames = new ArrayList<>(componentHosts.keySet());
        List<String> todoList = getTodoList(componentNames, commands);

        for (String componentCommand : todoList) {
            String[] split = componentCommand.split("-");
            String componentName = split[0].toLowerCase();
            Command command = Command.valueOf(split[1].toUpperCase());

            List<String> hostnames = componentHosts.get(componentName);
            if (CollectionUtils.isEmpty(hostnames)) {
                continue;
            }

            StageContext stageContext;
            switch (command) {
                case ADD:
                    stageContext = createStageContext(componentName, hostnames, commandDTO);
                    stages.add(new ComponentAddStage(stageContext));
                    break;
                case START:
                    if (!StackUtils.isClientComponent(componentName)) {
                        stageContext = createStageContext(componentName, hostnames, commandDTO);
                        stages.add(new ComponentStartStage(stageContext));
                    }
                    break;
                case STOP:
                    if (!StackUtils.isClientComponent(componentName)) {
                        stageContext = createStageContext(componentName, hostnames, commandDTO);
                        stages.add(new ComponentStopStage(stageContext));
                    }
                    break;
                case CHECK:
                    if (!StackUtils.isClientComponent(componentName)) {
                        stageContext = createStageContext(componentName, List.of(hostnames.get(0)), commandDTO);
                        stages.add(new ComponentCheckStage(stageContext));
                    }
                    break;
                case CONFIGURE:
                    stageContext = createStageContext(componentName, hostnames, commandDTO);
                    stages.add(new ComponentConfigureStage(stageContext));
                    break;
                case INIT:
                    stageContext = createStageContext(componentName, List.of(hostnames.get(0)), commandDTO);
                    stages.add(new ComponentInitStage(stageContext));
                    break;
                case PREPARE:
                    // Prepare phase runs after component started, client component shouldn't create this.
                    if (!StackUtils.isClientComponent(componentName)) {
                        stageContext = createStageContext(componentName, List.of(hostnames.get(0)), commandDTO);
                        stages.add(new ComponentPrepareStage(stageContext));
                    }
                    break;
            }
        }

        return stages;
    }

    private static StageContext createStageContext(
            String componentName, List<String> hostnames, CommandDTO commandDTO) {
        StageContext stageContext = StageContext.fromCommandDTO(commandDTO);

        ServiceDTO serviceDTO = StackUtils.getServiceDTOByComponentName(componentName);

        stageContext.setHostnames(hostnames);
        stageContext.setServiceName(serviceDTO.getName());
        stageContext.setComponentName(componentName);

        return stageContext;
    }

    private static List<String> getTodoList(List<String> componentNames, Command command) {
        return getTodoList(componentNames, List.of(command));
    }

    private static List<String> getTodoList(List<String> componentNames, List<Command> commands) {
        try {
            List<String> orderedList =
                    StackUtils.DAG.getAllNodesList().isEmpty() ? new ArrayList<>() : StackUtils.DAG.topologicalSort();
            orderedList.replaceAll(String::toUpperCase);
            List<String> componentCommandNames = new ArrayList<>();
            for (String componentName : componentNames) {
                for (Command command : commands) {
                    String name =
                            componentName.toUpperCase() + "-" + command.name().toUpperCase();
                    componentCommandNames.add(name);
                }
            }

            // Re-order the commands, since order.json does not contain all commands,
            // only contains which has dependencies, we need to add the rest to the end.
            if (commands.size() == 1) {
                orderedList.retainAll(componentCommandNames);
                componentCommandNames.removeAll(orderedList);
                orderedList.addAll(componentCommandNames);
                return orderedList;
            } else {
                // TODO, order.json currently only contains start/stop dependencies of major components, the situation
                // of commands size greater than 1 is only with init/start/prepare commands, we see this as a special
                // situation and use special logic for it, should use a better solution in the future.
                orderedList.retainAll(componentCommandNames);
                componentCommandNames.removeAll(orderedList);

                List<String> res = new ArrayList<>();
                List<String> commandOrder = List.of("ADD", "CONFIGURE", "INIT", "START", "PREPARE", "CHECK");
                for (String command : commandOrder) {
                    List<String> filtered = componentCommandNames.stream()
                            .filter(s -> s.endsWith(command))
                            .toList();
                    res.addAll(filtered);
                    if (command.equals("START")) {
                        res.addAll(orderedList);
                    }
                }

                return res;
            }

        } catch (Exception e) {
            throw new ServerException(e);
        }
    }

    public static void printStageDump(List<Stage> stages) {
        log.info("Dumping stages...");
        for (Stage stage : stages) {
            List<String> taskNames =
                    stage.getTasks().stream().map(Task::getName).toList();
            log.info("[{}] : [{}]", stage.getName(), String.join(",", taskNames));
        }
    }
}
