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

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bigtop.manager.dao.converter.JsonToMapConverter;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "\"platform_authorized\"")
@TableGenerator(name = "user_generator", table = "sequence", pkColumnName = "seq_name", valueColumnName = "seq_count")
public class PlatformAuthorizedPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "platform_authorized_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "platform_id", nullable = false)
    private Long platformId;

    @Column(name = "credentials", columnDefinition = "json", nullable = false)
    @Convert(converter = JsonToMapConverter.class)
    private Map<String, String> credentials;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserPO userPO;
}
