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
package org.apache.bigtop.manager.server.stack.model;

import lombok.Data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class StackModel {

    @XmlElement(name = "stack-name")
    private String stackName;

    @XmlElement(name = "stack-version")
    private String stackVersion;

    @XmlElement(name = "user-group")
    private String userGroup;

    /**
     * The scope of the stack, should be one of 'global' or 'cluster'.
     */
    private String scope;

    private String root;

    private String packages;

    @XmlElement(name = "repo-template")
    private String repoTemplate;

    @XmlElementWrapper(name = "repos")
    @XmlElements(@XmlElement(name = "repo"))
    private List<RepoModel> repos;
}
