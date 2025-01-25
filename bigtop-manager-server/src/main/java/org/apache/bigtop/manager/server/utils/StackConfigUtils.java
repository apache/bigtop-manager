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

import dev.langchain4j.agent.tool.P;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.server.model.dto.AttrsDTO;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.stack.model.AttrsModel;
import org.apache.bigtop.manager.server.stack.model.PropertyModel;
import org.apache.bigtop.manager.server.stack.xml.ConfigurationXml;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

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

    public static List<ServiceConfigDTO> mergeServiceConfigs(List<ServiceConfigDTO> oriConfigs, List<ServiceConfigDTO> overrideConfigs) {
        // To avoid to change the original configs, we use cloned object
        List<ServiceConfigDTO> mergedConfigs = new ArrayList<>();
        for (ServiceConfigDTO oriConfig : oriConfigs) {
            ServiceConfigDTO mergedConfig = new ServiceConfigDTO();
            BeanUtils.copyProperties(oriConfig, mergedConfig);
            mergedConfigs.add(mergedConfig);
        }

        if (CollectionUtils.isEmpty(overrideConfigs)) {
            return mergedConfigs;
        }

        Map<String, Map<String, String>> overrideConfigsMap = serviceConfig2Map(overrideConfigs);
        for (ServiceConfigDTO mergedConfig : mergedConfigs) {
            String configName = mergedConfig.getName();
            if (!overrideConfigsMap.containsKey(configName)) {
                continue;
            }

            // Override existing properties
            Map<String, String> overridePropertiesMap = overrideConfigsMap.get(configName);
            for (PropertyDTO property : mergedConfig.getProperties()) {
                String propertyName = property.getName();
                String value = overridePropertiesMap.remove(propertyName);
                if (value != null) {
                    property.setValue(value);
                }
            }

            // We still have some properties, maybe added by user manually
            if (MapUtils.isNotEmpty(overridePropertiesMap)) {
                for (Map.Entry<String, String> entry : overridePropertiesMap.entrySet()) {
                    PropertyDTO property = new PropertyDTO();
                    property.setName(entry.getKey());
                    property.setValue(entry.getValue());
                    mergedConfig.getProperties().add(property);
                }
            }
        }

        return mergedConfigs;
    }

    /**
     * extract config from List<ServiceConfigDTO> to Map<String, Map<String, String>>
     *
     * @param configs List<ServiceConfigDTO>
     * @return Map<String, Map<String, String>>
     */
    private static Map<String, Map<String, String>> serviceConfig2Map(List<ServiceConfigDTO> configs) {
        Map<String, Map<String, String>> outerMap = new HashMap<>();
        if (CollectionUtils.isEmpty(configs)) {
            return outerMap;
        }

        for (ServiceConfigDTO config : configs) {
            Map<String, String> innerMap = new HashMap<>();
            for (PropertyDTO property : config.getProperties()) {
                innerMap.put(property.getName(), property.getValue());
            }

            outerMap.put(config.getName(), innerMap);
        }

        return outerMap;
    }
}
