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
package org.apache.bigtop.manager.server.command.stage.factory.host;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.HostCheckPayload;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.Task;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.grpc.utils.ProtobufUtil;
import org.apache.bigtop.manager.server.command.stage.factory.AbstractStageFactory;
import org.apache.bigtop.manager.server.command.stage.factory.StageType;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HostCheckStageFactory extends AbstractStageFactory {

    @Resource
    private ClusterRepository clusterRepository;

    public StageType getStageType() {
        return StageType.HOST_CHECK;
    }

    @Override
    public void doCreateStage() {
        if (context.getClusterId() != null) {
            ClusterPO clusterPO = clusterRepository.getReferenceById(context.getClusterId());

            context.setStackName(clusterPO.getStackPO().getStackName());
            context.setStackVersion(clusterPO.getStackPO().getStackVersion());
        }

        // Create stages
        stage.setName("Check Hosts");

        List<Task> tasks = new ArrayList<>();
        for (String hostname : context.getHostnames()) {
            Task task = new Task();
            task.setName(stage.getName() + " on " + hostname);
            task.setStackName(context.getStackName());
            task.setStackVersion(context.getStackVersion());
            task.setHostname(hostname);
            task.setServiceName("cluster");
            task.setServiceUser("root");
            task.setServiceGroup("root");
            task.setComponentName("bigtop-manager-agent");
            task.setCommand(Command.CUSTOM);
            task.setCustomCommand("check_host");

            CommandRequest request = createMessage(hostname);
            task.setContent(ProtobufUtil.toJson(request));

            tasks.add(task);
        }

        stage.setTasks(tasks);
    }

    private CommandRequest createMessage(String hostname) {
        HostCheckPayload messagePayload = new HostCheckPayload();
        messagePayload.setHostname(hostname);

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.HOST_CHECK);
        builder.setHostname(hostname);
        builder.setPayload(JsonUtils.writeAsString(messagePayload));

        return builder.build();
    }
}
