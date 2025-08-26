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
package org.apache.bigtop.manager.server.mcp.tool;

import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.mcp.converter.JsonToolCallResultConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

@Component
public class ServiceMcpTool implements McpTool {

    @Resource
    private ServiceDao serviceDao;

    @Tool(
            name = "ListServices",
            description = "List created Services",
            resultConverter = JsonToolCallResultConverter.class)
    public List<ServiceVO> listServices(Long clusterId) {
        List<ServicePO> servicePOList = serviceDao.findByClusterId(clusterId);
        return ServiceConverter.INSTANCE.fromPO2VO(servicePOList);
    }
}
