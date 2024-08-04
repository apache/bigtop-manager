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

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.message.entity.pojo.CustomCommandInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.OSSpecificInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ScriptInfo;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CustomCommandDTO;
import org.apache.bigtop.manager.server.model.dto.OSSpecificDTO;
import org.apache.bigtop.manager.server.model.dto.ScriptDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractComponentTask extends AbstractTask {

    protected HostComponentRepository hostComponentRepository;

    public AbstractComponentTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.hostComponentRepository = SpringContextHolder.getBean(HostComponentRepository.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected CommandRequest getCommandRequest() {
        CommandPayload commandPayload = new CommandPayload();
        commandPayload.setServiceName(taskContext.getServiceName());
        commandPayload.setCommand(getCommand());
        commandPayload.setServiceUser(taskContext.getServiceUser());
        commandPayload.setServiceGroup(taskContext.getServiceGroup());
        commandPayload.setStackName(taskContext.getStackName());
        commandPayload.setStackVersion(taskContext.getStackVersion());
        commandPayload.setComponentName(taskContext.getComponentName());
        commandPayload.setRoot(taskContext.getRoot());
        commandPayload.setHostname(taskContext.getHostname());

        Map<String, Object> properties = taskContext.getProperties();

        commandPayload.setCustomCommands(
                convertCustomCommandInfo((List<CustomCommandDTO>) properties.get("customCommands")));
        commandPayload.setOsSpecifics(convertOSSpecificInfo((List<OSSpecificDTO>) properties.get("osSpecifics")));
        commandPayload.setCommandScript(convertScriptInfo((ScriptDTO) properties.get("commandScript")));

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.COMPONENT);
        builder.setHostname(taskContext.getHostname());
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
