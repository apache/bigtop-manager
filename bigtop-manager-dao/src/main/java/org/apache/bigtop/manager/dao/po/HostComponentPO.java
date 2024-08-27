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
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "host_component",
        indexes = {
            @Index(name = "idx_hc_component_id", columnList = "component_id"),
            @Index(name = "idx_hc_host_id", columnList = "host_id")
        })
@TableGenerator(
        name = "host_component_generator",
        table = "sequence",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count")
public class HostComponentPO extends BasePO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "host_component_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "state")
    private String state;

    @Column(name = "host_id")
    private Long hostId;

    @Column(name = "component_id")
    private Long componentId;

    @Transient
    @Column(name = "stack_name")
    private String stackName;

    @Transient
    @Column(name = "stack_version")
    private String stackVersion;

    @Transient
    @Column(name = "service_id")
    private Long serviceId;

    @Transient
    @Column(name = "cluster_name")
    private String clusterName;

    @Transient
    @Column(name = "root")
    private String root;

    @Transient
    @Column(name = "service_name")
    private String serviceName;

    @Transient
    @Column(name = "service_user")
    private String serviceUser;

    @Transient
    @Column(name = "component_name")
    private String componentName;

    @Transient
    @Column(name = "display_name")
    private String displayName;

    @Transient
    @Column(name = "category")
    private String category;

    @Transient
    @Column(name = "command_script")
    private String commandScript;

    @Transient
    @Column(name = "hostname")
    private String hostname;
}
