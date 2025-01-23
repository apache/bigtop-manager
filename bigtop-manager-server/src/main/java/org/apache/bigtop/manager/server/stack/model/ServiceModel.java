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
public class ServiceModel {

    private String name;

    @XmlElement(name = "display-name")
    private String displayName;

    private String desc;

    private String version;

    private String user;

    private String license;

    @XmlElementWrapper(name = "package-specifics")
    @XmlElements(@XmlElement(name = "package-specific"))
    private List<PackageSpecificModel> packageSpecifics;

    @XmlElementWrapper(name = "components")
    @XmlElements(@XmlElement(name = "component"))
    private List<ComponentModel> components;

    @XmlElementWrapper(name = "templates")
    @XmlElements(@XmlElement(name = "template"))
    private List<TemplateModel> templates;

    @XmlElementWrapper(name = "required-services")
    @XmlElements(@XmlElement(name = "service"))
    private List<String> requiredServices;
}
