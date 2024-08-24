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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "component",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_component_name",
                    columnNames = {"component_name", "cluster_id"})
        },
        indexes = {
            @Index(name = "idx_component_cluster_id", columnList = "cluster_id"),
            @Index(name = "idx_component_service_id", columnList = "service_id")
        })
@TableGenerator(
        name = "component_generator",
        table = "sequence",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count")
public class ComponentPO extends BasePO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "component_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "command_script")
    private String commandScript;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "custom_commands", length = 16777216)
    private String customCommands;

    @Column(name = "category")
    private String category;

    @Column(name = "quick_link")
    private String quickLink;

    @Column(name = "cardinality")
    private String cardinality;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "cluster_id")
    private Long clusterId;

    @Transient
    @Column(name = "service_name")
    private String serviceName;

    @Transient
    @Column(name = "cluster_name")
    private String clusterName;

    @ManyToOne
    @JoinColumn(name = "service_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ServicePO servicePO;

    @ManyToOne
    @JoinColumn(name = "cluster_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ClusterPO clusterPO;
}
