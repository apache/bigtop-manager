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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.enums.JobState;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "\"task\"",
        indexes = {
            @Index(name = "idx_task_cluster_id", columnList = "cluster_id"),
            @Index(name = "idx_task_job_id", columnList = "job_id"),
            @Index(name = "idx_task_stage_id", columnList = "stage_id")
        })
@TableGenerator(name = "task_generator", table = "sequence", pkColumnName = "seq_name", valueColumnName = "seq_count")
public class TaskPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "task_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "context", length = 16777216)
    private String context;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "state")
    private JobState state;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "command")
    private Command command;

    @Column(name = "custom_command")
    private String customCommand;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "custom_commands", length = 16777216)
    private String customCommands;

    @Column(name = "command_script")
    private String commandScript;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "stack_name")
    private String stackName;

    @Column(name = "stack_version")
    private String stackVersion;

    @Column(name = "service_user")
    private String serviceUser;

    @Column(name = "service_group")
    private String serviceGroup;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", length = 16777216)
    private String content;

    @ManyToOne
    @JoinColumn(name = "job_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private JobPO jobPO;

    @ManyToOne
    @JoinColumn(name = "stage_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private StagePO stagePO;

    @ManyToOne
    @JoinColumn(name = "cluster_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ClusterPO clusterPO;
}
