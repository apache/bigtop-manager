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

import org.apache.bigtop.manager.server.enums.PropertyAction;
import org.apache.bigtop.manager.server.model.dto.AttrsDTO;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.stack.model.AttrsModel;
import org.apache.bigtop.manager.server.stack.model.PropertyModel;
import org.apache.bigtop.manager.server.stack.xml.ConfigurationXml;

import org.apache.commons.collections4.CollectionUtils;
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
            attrsDTO.setRequired(attrsModel.getRequired());
            attrsDTO.setType(attrsModel.getType());
            propertyDTO.setAttrs(attrsDTO);
        }

        return propertyDTO;
    }

    public static List<ServiceConfigDTO> mergeServiceConfigs(
            List<ServiceConfigDTO> oriConfigs, List<ServiceConfigDTO> overrideConfigs) {
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

        Map<String, Map<String, PropertyDTO>> mergedConfigsMap = serviceConfig2Map(mergedConfigs);
        for (ServiceConfigDTO overrideConfig : overrideConfigs) {
            String configName = overrideConfig.getName();

            // Merge properties from override config to existing config
            Map<String, PropertyDTO> mergedPropertiesMap = mergedConfigsMap.get(configName);
            for (PropertyDTO property : overrideConfig.getProperties()) {
                String propertyName = property.getName();
                PropertyAction action = property.getAction();
                if (action == null) {
                    // null means property does not come from request body, but from config xml or database.
                    // the logic the same as `UPDATE` action.
                    action = PropertyAction.UPDATE;
                }
                switch (action) {
                    case ADD, UPDATE -> {
                        PropertyDTO propertyDTO = mergedPropertiesMap.get(propertyName);
                        if (propertyDTO == null) {
                            propertyDTO = new PropertyDTO();
                            propertyDTO.setName(propertyName);
                        }

                        propertyDTO.setValue(property.getValue());
                        mergedPropertiesMap.put(propertyName, propertyDTO);
                    }
                    case DELETE -> mergedPropertiesMap.remove(propertyName);
                }
            }
        }

        mergedConfigs = map2ServiceConfig(mergedConfigsMap);

        // Assign id for each service config
        Map<String, Long> configIdMap = new HashMap<>();
        for (ServiceConfigDTO config : oriConfigs) {
            String name = config.getName();
            Long id = config.getId();
            if (id != null) {
                configIdMap.put(name, id);
            }
        }

        for (ServiceConfigDTO config : overrideConfigs) {
            String name = config.getName();
            Long id = config.getId();
            if (id != null) {
                configIdMap.put(name, id);
            }
        }

        for (ServiceConfigDTO config : mergedConfigs) {
            config.setId(configIdMap.get(config.getName()));
        }

        return mergedConfigs;
    }

    /**
     * extract config from List<ServiceConfigDTO> to Map<String, Map<String, String>>
     *
     * @param configs List<ServiceConfigDTO>
     * @return Map<String, Map<String, String>>
     */
    private static Map<String, Map<String, PropertyDTO>> serviceConfig2Map(List<ServiceConfigDTO> configs) {
        Map<String, Map<String, PropertyDTO>> outerMap = new HashMap<>();
        if (CollectionUtils.isEmpty(configs)) {
            return outerMap;
        }

        for (ServiceConfigDTO config : configs) {
            Map<String, PropertyDTO> innerMap = new HashMap<>();
            for (PropertyDTO property : config.getProperties()) {
                innerMap.put(property.getName(), property);
            }

            outerMap.put(config.getName(), innerMap);
        }

        return outerMap;
    }

    private static List<ServiceConfigDTO> map2ServiceConfig(Map<String, Map<String, PropertyDTO>> configMap) {
        List<ServiceConfigDTO> configs = new ArrayList<>();
        for (Map.Entry<String, Map<String, PropertyDTO>> entry : configMap.entrySet()) {
            ServiceConfigDTO serviceConfig = new ServiceConfigDTO();
            String name = entry.getKey();
            List<PropertyDTO> properties = new ArrayList<>(entry.getValue().values());

            serviceConfig.setName(name);
            serviceConfig.setProperties(properties);
            configs.add(serviceConfig);
        }

        return configs;
    }
}
