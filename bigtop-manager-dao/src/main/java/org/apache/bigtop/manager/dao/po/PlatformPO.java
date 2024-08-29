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
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "llm_platform")
public class PlatformPO extends BasePO {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "credential", columnDefinition = "json", nullable = false)
    @Convert(converter = JsonToMapConverter.class)
    private Map<String, String> credential;

    @Column(name = "support_models", length = 255)
    private String supportModels;
}
