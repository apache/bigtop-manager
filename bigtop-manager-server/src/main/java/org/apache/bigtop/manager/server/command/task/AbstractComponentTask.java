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
import org.apache.bigtop.manager.common.message.entity.pojo.PackageInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.PackageSpecificInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ScriptInfo;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.repository.HostComponentDao;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CustomCommandDTO;
import org.apache.bigtop.manager.server.model.dto.PackageDTO;
import org.apache.bigtop.manager.server.model.dto.PackageSpecificDTO;
import org.apache.bigtop.manager.server.model.dto.ScriptDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractComponentTask extends AbstractTask {

    protected HostComponentDao hostComponentDao;

    public AbstractComponentTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.hostComponentDao = SpringContextHolder.getBean(HostComponentDao.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected CommandRequest getCommandRequest() {
        CommandPayload commandPayload = new CommandPayload();
        commandPayload.setServiceName(taskContext.getServiceName());
        commandPayload.setCommand(getCommand());
        commandPayload.setServiceUser(taskContext.getServiceUser());
        commandPayload.setComponentName(taskContext.getComponentName());
        commandPayload.setRootDir(taskContext.getRootDir());

        Map<String, Object> properties = taskContext.getProperties();

        commandPayload.setCustomCommands(
                convertCustomCommandInfo((List<CustomCommandDTO>) properties.get("customCommands")));
        commandPayload.setPackageSpecifics(
                convertPackageSpecificInfo((List<PackageSpecificDTO>) properties.get("packageSpecifics")));
        commandPayload.setCommandScript(convertScriptInfo((ScriptDTO) properties.get("commandScript")));

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.COMPONENT);
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

    private List<PackageSpecificInfo> convertPackageSpecificInfo(List<PackageSpecificDTO> packageSpecificDTOList) {
        if (packageSpecificDTOList == null) {
            return new ArrayList<>();
        }

        List<PackageSpecificInfo> packageSpecificInfos = new ArrayList<>();
        for (PackageSpecificDTO packageSpecificDTO : packageSpecificDTOList) {
            PackageSpecificInfo packageSpecificInfo = new PackageSpecificInfo();
            packageSpecificInfo.setOs(packageSpecificDTO.getOs());
            packageSpecificInfo.setArch(packageSpecificDTO.getArch());
            List<PackageInfo> packageInfoList = new ArrayList<>();
            for (PackageDTO packageDTO : packageSpecificDTO.getPackages()) {
                PackageInfo packageInfo = new PackageInfo();
                packageInfo.setName(packageDTO.getName());
                packageInfo.setChecksum(packageDTO.getChecksum());
                packageInfoList.add(packageInfo);
            }
            packageSpecificInfo.setPackages(packageInfoList);
            packageSpecificInfos.add(packageSpecificInfo);
        }

        return packageSpecificInfos;
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
