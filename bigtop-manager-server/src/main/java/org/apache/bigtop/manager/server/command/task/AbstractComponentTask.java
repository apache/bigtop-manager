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

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandReply;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandServiceGrpc;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.grpc.pojo.PackageInfo;
import org.apache.bigtop.manager.grpc.pojo.PackageSpecificInfo;
import org.apache.bigtop.manager.grpc.pojo.TemplateInfo;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.PackageDTO;
import org.apache.bigtop.manager.server.model.dto.PackageSpecificDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.dto.TemplateDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractComponentTask extends AbstractTask {

    protected ComponentDao componentDao;

    public AbstractComponentTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.componentDao = SpringContextHolder.getBean(ComponentDao.class);
    }

    @Override
    protected Boolean doRun(String hostname, Integer grpcPort) {
        ComponentCommandRequest request = getComponentCommandRequest();
        ComponentCommandServiceGrpc.ComponentCommandServiceBlockingStub stub = GrpcClient.getBlockingStub(
                hostname, grpcPort, ComponentCommandServiceGrpc.ComponentCommandServiceBlockingStub.class);
        ComponentCommandReply reply = stub.exec(request);

        return reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;
    }

    @SuppressWarnings("unchecked")
    protected ComponentCommandRequest getComponentCommandRequest() {
        StackDTO stackDTO = StackUtils.getServiceStack(taskContext.getServiceName());
        ServiceDTO serviceDTO = StackUtils.getServiceDTO(taskContext.getServiceName());

        ComponentCommandPayload payload = new ComponentCommandPayload();
        payload.setServiceName(taskContext.getServiceName());
        payload.setComponentName(taskContext.getComponentName());
        payload.setServiceUser(taskContext.getServiceUser());
        payload.setStackName(stackDTO.getStackName());
        payload.setStackVersion(stackDTO.getStackVersion());
        payload.setCommand(getCommand().getCode());
        payload.setCustomCommand(getCustomCommand());

        payload.setTemplates(convertTemplateInfo(serviceDTO.getName(), serviceDTO.getTemplates()));
        payload.setPackageSpecifics(convertPackageSpecificInfo(serviceDTO.getPackageSpecifics()));

        ComponentCommandRequest.Builder requestBuilder = ComponentCommandRequest.newBuilder();
        requestBuilder.setPayload(JsonUtils.writeAsString(payload));
        requestBuilder.setTaskId(getTaskPO().getId());

        return requestBuilder.build();
    }

    private List<TemplateInfo> convertTemplateInfo(String serviceName, List<TemplateDTO> templateDTOList) {
        if (templateDTOList == null) {
            return new ArrayList<>();
        }

        List<TemplateInfo> templateInfos = new ArrayList<>();
        for (TemplateDTO templateDTO : templateDTOList) {
            String content = StackUtils.SERVICE_TEMPLATE_MAP.get(serviceName).get(templateDTO.getSrc());

            TemplateInfo templateInfo = new TemplateInfo();
            templateInfo.setSrc(templateDTO.getSrc());
            templateInfo.setDest(templateDTO.getDest());
            templateInfo.setContent(content);
            templateInfos.add(templateInfo);
        }

        return templateInfos;
    }

    private List<PackageSpecificInfo> convertPackageSpecificInfo(List<PackageSpecificDTO> packageSpecificDTOList) {
        if (packageSpecificDTOList == null) {
            return new ArrayList<>();
        }

        List<PackageSpecificInfo> packageSpecificInfos = new ArrayList<>();
        for (PackageSpecificDTO packageSpecificDTO : packageSpecificDTOList) {
            PackageSpecificInfo packageSpecificInfo = new PackageSpecificInfo();
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
}
