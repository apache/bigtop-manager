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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "type_config")
@TableGenerator(
        name = "type_config_generator",
        table = "sequence",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count")
public class TypeConfigPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "type_config_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "type_name")
    private String typeName;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "properties_json", length = 16777216)
    private String propertiesJson;

    @ManyToOne
    @JoinColumn(name = "service_config_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ServiceConfigPO serviceConfigPO;
}
