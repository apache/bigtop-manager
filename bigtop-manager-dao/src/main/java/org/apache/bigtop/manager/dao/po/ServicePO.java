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
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "service",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_service_name",
                    columnNames = {"service_name", "cluster_id"})
        },
        indexes = {@Index(name = "idx_service_cluster_id", columnList = "cluster_id")})
@TableGenerator(
        name = "service_generator",
        table = "sequence",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count")
public class ServicePO extends BasePO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "service_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "service_desc", length = 1024)
    private String serviceDesc;

    @Column(name = "service_version")
    private String serviceVersion;

    @Column(name = "package_specifics", length = 1024)
    private String packageSpecifics;

    @Column(name = "service_user")
    private String serviceUser;

    @Column(name = "required_services")
    private String requiredServices;

    @Column(name = "cluster_id")
    private Long clusterId;

    @ManyToOne
    @JoinColumn(name = "cluster_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ClusterPO clusterPO;
}
