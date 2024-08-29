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
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "service")
public class ServicePO extends BasePO implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "service_desc")
    private String serviceDesc;

    @Column(name = "service_version")
    private String serviceVersion;

    @Column(name = "package_specifics")
    private String packageSpecifics;

    @Column(name = "service_user")
    private String serviceUser;

    @Column(name = "required_services")
    private String requiredServices;

    @Column(name = "cluster_id")
    private Long clusterId;

    @Transient
    @Column(name = "cluster_name")
    private String clusterName;

    @Transient
    @Column(name = "user_group")
    private String userGroup;

    @ToString.Exclude
    private List<ComponentPO> components;
}
