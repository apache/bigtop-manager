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
package org.apache.bigtop.manager.server.model.mapper;

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.vo.PropertyVO;

import org.mapstruct.Named;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class TypeConvert {

    @Named("obj2Json")
    public <T> String obj2Json(T obj) {
        return JsonUtils.writeAsString(obj);
    }

    @Named("json2List")
    public List<String> json2List(String json) {
        return JsonUtils.readFromString(json, new TypeReference<>() {});
    }

    @Named("json2PropertyDTOList")
    public List<PropertyDTO> json2PropertyDTOList(String json) {
        return JsonUtils.readFromString(json, new TypeReference<>() {});
    }

    @Named("json2PropertyVOList")
    public List<PropertyVO> json2PropertyVOList(String json) {
        return JsonUtils.readFromString(json, new TypeReference<>() {});
    }
}
