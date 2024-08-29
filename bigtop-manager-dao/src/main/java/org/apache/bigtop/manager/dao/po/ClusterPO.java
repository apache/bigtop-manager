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
import jakarta.persistence.Transient;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "cluster")
public class ClusterPO extends BasePO implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "cluster_type")
    private Integer clusterType;

    @Column(name = "root")
    private String root;

    @Column(name = "user_group")
    private String userGroup;

    @Column(name = "packages")
    private String packages;

    @Column(name = "repo_template")
    private String repoTemplate;

    @Column(name = "state")
    private String state;

    @Column(name = "selected")
    private Boolean selected;

    @Column(name = "stack_id")
    private Long stackId;

    @Transient
    @Column(name = "stack_name")
    private String stackName;

    @Transient
    @Column(name = "stack_version")
    private String stackVersion;
}
