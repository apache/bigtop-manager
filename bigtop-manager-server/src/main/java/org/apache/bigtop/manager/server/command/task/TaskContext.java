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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.model.dto.HostDTO;

import lombok.Data;

import java.util.Map;

@Data
public class TaskContext {

    private Long clusterId;

    private String clusterName;

    private HostDTO hostDTO;

    private String serviceName;

    private String serviceUser;

    private String componentName;

    // This is for display purpose for task info(eg. task name) only
    private String componentDisplayName;

    private String userGroup;

    private String rootDir;

    // Extra properties for specific tasks
    protected Map<String, Object> properties;
}
