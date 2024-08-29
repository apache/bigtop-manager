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

import org.apache.bigtop.manager.dao.converter.JsonToMapConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "llm_platform_authorized")
public class PlatformAuthorizedPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "llm_platform_authorized_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "credentials", columnDefinition = "json", nullable = false)
    @Convert(converter = JsonToMapConverter.class)
    private Map<String, String> credentials;

    @Column(name = "platform_id")
    private Long platformId;
}
