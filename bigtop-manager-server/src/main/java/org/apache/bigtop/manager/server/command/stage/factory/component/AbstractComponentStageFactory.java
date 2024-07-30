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
package org.apache.bigtop.manager.server.command.stage.factory.component;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.message.entity.pojo.CustomCommandInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.OSSpecificInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ScriptInfo;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.grpc.utils.ProtobufUtil;
import org.apache.bigtop.manager.server.command.stage.factory.AbstractStageFactory;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.CustomCommandDTO;
import org.apache.bigtop.manager.server.model.dto.OSSpecificDTO;
import org.apache.bigtop.manager.server.model.dto.ScriptDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractComponentStageFactory extends AbstractStageFactory {

    @Resource
    private ClusterRepository clusterRepository;

    protected ClusterPO clusterPO;

    @Override
    public void doCreateStage() {
        clusterPO = clusterRepository.getReferenceById(context.getClusterId());

        Command command = getCommand();
        ServiceDTO serviceDTO = context.getServiceDTO();
        ComponentDTO componentDTO = context.getComponentDTO();

        stage.setName(command.toCamelCase() + " " + componentDTO.getDisplayName());
        stage.setServiceName(serviceDTO.getServiceName());
        stage.setComponentName(componentDTO.getComponentName());

        List<TaskPO> taskPOList = new ArrayList<>();
        for (String hostname : context.getHostnames()) {
            TaskPO taskPO = new TaskPO();

            // Required fields
            taskPO.setName(stage.getName() + " on " + hostname);
            taskPO.setHostname(hostname);
            taskPO.setCommand(command);
            taskPO.setServiceName(serviceDTO.getServiceName());
            taskPO.setStackName(context.getStackName());
            taskPO.setStackVersion(context.getStackVersion());

            // Context fields
            taskPO.setComponentName(componentDTO.getComponentName());
            taskPO.setServiceUser(serviceDTO.getServiceUser());
            taskPO.setServiceGroup(serviceDTO.getServiceGroup());
            taskPO.setCustomCommands(JsonUtils.writeAsString(componentDTO.getCustomCommands()));
            taskPO.setCommandScript(JsonUtils.writeAsString(componentDTO.getCommandScript()));

            CommandRequest request = getMessage(serviceDTO, componentDTO, hostname, command);
            taskPO.setContent(ProtobufUtil.toJson(request));
            taskPOList.add(taskPO);
        }

        stage.setTaskPOList(taskPOList);
    }

    protected abstract Command getCommand();

    private CommandRequest getMessage(
            ServiceDTO serviceDTO, ComponentDTO componentDTO, String hostname, Command command) {
        CommandPayload commandPayload = new CommandPayload();
        commandPayload.setServiceName(serviceDTO.getServiceName());
        commandPayload.setCommand(command);
        commandPayload.setServiceUser(serviceDTO.getServiceUser());
        commandPayload.setServiceGroup(serviceDTO.getServiceGroup());
        commandPayload.setStackName(context.getStackName());
        commandPayload.setStackVersion(context.getStackVersion());
        commandPayload.setComponentName(componentDTO.getComponentName());
        commandPayload.setRoot(clusterPO.getRoot());
        commandPayload.setHostname(hostname);

        commandPayload.setCustomCommands(convertCustomCommandInfo(componentDTO.getCustomCommands()));
        commandPayload.setOsSpecifics(convertOSSpecificInfo(serviceDTO.getOsSpecifics()));
        commandPayload.setCommandScript(convertScriptInfo(componentDTO.getCommandScript()));

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.COMPONENT);
        builder.setHostname(hostname);
        builder.setPayload(JsonUtils.writeAsString(commandPayload));

        return builder.build();
    }

    private ScriptInfo convertScriptInfo(ScriptDTO scriptDTO) {
        if (scriptDTO == null) {
            return null;
        }

        ScriptInfo scriptInfo = new ScriptInfo();
        scriptInfo.setScriptId(scriptDTO.getScriptId());
        scriptInfo.setScriptType(scriptDTO.getScriptType());
        scriptInfo.setTimeout(scriptDTO.getTimeout());
        return scriptInfo;
    }

    private List<OSSpecificInfo> convertOSSpecificInfo(List<OSSpecificDTO> osSpecificDTOs) {
        if (osSpecificDTOs == null) {
            return new ArrayList<>();
        }

        List<OSSpecificInfo> osSpecificInfos = new ArrayList<>();
        for (OSSpecificDTO osSpecificDTO : osSpecificDTOs) {
            OSSpecificInfo osSpecificInfo = new OSSpecificInfo();
            osSpecificInfo.setOs(osSpecificDTO.getOs());
            osSpecificInfo.setArch(osSpecificDTO.getArch());
            osSpecificInfo.setPackages(osSpecificDTO.getPackages());
            osSpecificInfos.add(osSpecificInfo);
        }

        return osSpecificInfos;
    }

    private List<CustomCommandInfo> convertCustomCommandInfo(List<CustomCommandDTO> customCommandDTOs) {
        if (customCommandDTOs == null) {
            return new ArrayList<>();
        }

        List<CustomCommandInfo> customCommandInfos = new ArrayList<>();
        for (CustomCommandDTO customCommandDTO : customCommandDTOs) {
            CustomCommandInfo customCommandInfo = new CustomCommandInfo();
            customCommandInfo.setName(customCommandDTO.getName());
            customCommandInfo.setCommandScript(convertScriptInfo(customCommandDTO.getCommandScript()));
            customCommandInfos.add(customCommandInfo);
        }

        return customCommandInfos;
    }
}
