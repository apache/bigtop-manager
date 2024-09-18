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
package org.apache.bigtop.manager.dao.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "task")
public class TaskPO extends BasePO implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "context", nullable = false)
    private String context;

    @Column(name = "state")
    private String state;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "command")
    private String command;

    @Column(name = "custom_command")
    private String customCommand;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "stack_name")
    private String stackName;

    @Column(name = "stack_version")
    private String stackVersion;

    @Column(name = "service_user")
    private String serviceUser;

    @Column(name = "content")
    private String content;

    @Column(name = "stage_id")
    private Long stageId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "cluster_id")
    private Long clusterId;
}
