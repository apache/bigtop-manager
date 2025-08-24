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

import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.server.mcp.converter.JsonToolCallResultConverter;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.vo.HostVO;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

@Component
public class HostMcpTool implements McpTool {

    @Resource
    private HostDao hostDao;

    @Tool(name = "ListHosts", description = "List created Hosts", resultConverter = JsonToolCallResultConverter.class)
    public List<HostVO> listHosts(Long clusterId) {
        List<HostPO> hostPOList = hostDao.findAllByClusterId(clusterId);
        return HostConverter.INSTANCE.fromPO2VO(hostPOList);
    }
}
