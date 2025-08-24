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

import org.apache.bigtop.manager.server.mcp.converter.JsonToolCallResultConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.converter.StackConverter;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StackMcpTool implements McpTool {

    @Tool(
            name = "ListStacks",
            description = "List supported stacks",
            resultConverter = JsonToolCallResultConverter.class)
    public List<StackVO> listStacks() {
        List<StackVO> stackVOList = new ArrayList<>();

        for (Map.Entry<StackDTO, List<ServiceDTO>> entry : StackUtils.STACK_SERVICE_MAP.entrySet()) {
            StackDTO stackDTO = entry.getKey();
            List<ServiceDTO> serviceDTOList = entry.getValue();

            StackVO stackVO = StackConverter.INSTANCE.fromDTO2VO(stackDTO);
            stackVO.setServices(ServiceConverter.INSTANCE.fromDTO2VO(serviceDTOList));
            stackVOList.add(stackVO);
        }

        return stackVOList;
    }
}
