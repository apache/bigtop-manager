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
package org.apache.bigtop.manager.server.utils;

import org.apache.bigtop.manager.server.model.dto.AttrsDTO;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.stack.model.AttrsModel;
import org.apache.bigtop.manager.server.stack.model.PropertyModel;
import org.apache.bigtop.manager.server.stack.xml.ConfigurationXml;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackConfigUtils {

    /**
     * load config from yaml file to Map<String,Object>
     *
     * @param fileName yaml config file name
     * @return List<PropertyDTO>
     */
    public static List<PropertyDTO> loadConfig(String fileName) {
        ConfigurationXml configurationXml = JaxbUtils.readFromPath(fileName, ConfigurationXml.class);
        List<PropertyModel> propertyModels = configurationXml.getPropertyModels();

        List<PropertyDTO> propertyDTOList = new ArrayList<>();
        for (PropertyModel propertyModel : propertyModels) {
            PropertyDTO propertyDTO = getPropertyDTO(propertyModel);
            propertyDTOList.add(propertyDTO);
        }

        return propertyDTOList;
    }

    private static PropertyDTO getPropertyDTO(PropertyModel propertyModel) {
        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setDisplayName(propertyModel.getDisplayName());
        propertyDTO.setDesc(StringUtils.strip(propertyModel.getDesc()));
        propertyDTO.setName(propertyModel.getName());
        propertyDTO.setValue(StringUtils.strip(propertyModel.getValue()));
        if (propertyModel.getAttrs() != null) {
            AttrsModel attrsModel = propertyModel.getAttrs();
            AttrsDTO attrsDTO = new AttrsDTO();
            attrsDTO.setType(attrsModel.getType());
            propertyDTO.setAttrs(attrsDTO);
        }

        return propertyDTO;
    }

    /**
     * extract config from List<Map<String,Object>> to Map<String,Object>
     *
     * @param list List<PropertyModel>
     * @return Map<String, Object>
     */
    public static Map<String, Object> extractConfigMap(List<PropertyDTO> list) {
        if (list == null) {
            return null;
        }

        Map<String, Object> hashMap = new HashMap<>();
        for (PropertyDTO property : list) {
            String key = property.getName();
            Object value = property.getValue();
            hashMap.put(key, value);
        }
        return hashMap;
    }
}
