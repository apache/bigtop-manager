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

import org.apache.bigtop.manager.common.enums.JobState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "\"job\"",
        indexes = {@Index(name = "idx_job_cluster_id", columnList = "cluster_id")})
@TableGenerator(name = "job_generator", table = "sequence", pkColumnName = "seq_name", valueColumnName = "seq_count")
public class JobPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "job_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "state")
    private JobState state;

    @Column(name = "name")
    private String name;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "\"context\"", length = 16777216)
    private String context;

    @ManyToOne
    @JoinColumn(name = "cluster_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ClusterPO clusterPO;

    @ToString.Exclude
    @OneToMany(mappedBy = "jobPO")
    private List<StagePO> stagePOList;
}
