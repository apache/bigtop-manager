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

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.utils.CaseUtils;
import org.apache.bigtop.manager.common.utils.FileUtils;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.stack.dag.ComponentCommandWrapper;
import org.apache.bigtop.manager.server.stack.dag.DAG;
import org.apache.bigtop.manager.server.stack.dag.DagGraphEdge;
import org.apache.bigtop.manager.server.stack.model.ServiceModel;
import org.apache.bigtop.manager.server.stack.xml.ServiceMetainfoXml;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StackUtils {

    private static final String ROLE_COMMAND_SPLIT = "-";

    private static final String META_FILE = "metainfo.xml";

    private static final String SERVICES_FOLDER_NAME = "services";

    private static final String CONFIGURATION_FOLDER = "configuration";

    private static final String TEMPLATE_FOLDER = "template";

    private static final String CONFIGURATION_FILE_EXTENSION = "xml";

    private static final String DEPENDENCY_FILE_NAME = "order.json";

    public static final Map<String, List<ServiceConfigDTO>> SERVICE_CONFIG_MAP = new HashMap<>();

    public static final Map<String, Map<String, String>> SERVICE_TEMPLATE_MAP = new HashMap<>();

    public static final Map<StackDTO, List<ServiceDTO>> STACK_SERVICE_MAP = new HashMap<>();

    public static final DAG<String, ComponentCommandWrapper, DagGraphEdge> DAG = new DAG<>();

    private static boolean parsed = false;

    public static synchronized void parseStack() {
        if (parsed) {
            return;
        }

        File stacksFolder = loadStacksFolder();
        File[] stackFolders = Optional.ofNullable(stacksFolder.listFiles()).orElse(new File[0]);

        for (File stackFolder : stackFolders) {
            String stackName = stackFolder.getName();
            File[] versionFolders = Optional.ofNullable(stackFolder.listFiles()).orElse(new File[0]);

            for (File versionFolder : versionFolders) {
                String stackVersion = versionFolder.getName();
                parseService(new StackDTO(stackName, stackVersion), versionFolder);
            }
        }

        parsed = true;
    }

    /**
     * Parse service file to generate service model
     *
     * @param stackDTO stackDTO
     * @return service model {@link ServiceModel}
     */
    public static void parseService(StackDTO stackDTO, File versionFolder) {
        File[] files = new File(versionFolder.getAbsolutePath(), SERVICES_FOLDER_NAME).listFiles();
        List<ServiceDTO> services = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                log.info("parsing service: {}", file.getName());

                ServiceDTO serviceDTO = parseServiceMetaInfo(file);
                services.add(serviceDTO);

                parseServiceConfigurations(file, serviceDTO.getName());
                parseServiceTemplates(file, serviceDTO.getName());

                parseDag(file);
            }
        }

        STACK_SERVICE_MAP.put(stackDTO, services);
    }

    private static ServiceDTO parseServiceMetaInfo(File file) {
        ServiceMetainfoXml serviceMetainfoXml =
                JaxbUtils.readFromPath(file.getAbsolutePath() + "/" + META_FILE, ServiceMetainfoXml.class);
        ServiceModel serviceModel = serviceMetainfoXml.getService();
        ServiceDTO serviceDTO = ServiceConverter.INSTANCE.fromModel2DTO(serviceModel);
        serviceDTO.setDesc(StringUtils.strip(serviceDTO.getDesc()));
        return serviceDTO;
    }

    private static void parseServiceConfigurations(File file, String serviceName) {
        List<ServiceConfigDTO> configs = new ArrayList<>();
        File configFolder = new File(file.getAbsolutePath(), CONFIGURATION_FOLDER);
        if (configFolder.exists()) {
            for (File configFile : Optional.ofNullable(configFolder.listFiles()).orElse(new File[0])) {
                String configPath = configFile.getAbsolutePath();
                String fileExtension = configPath.substring(configPath.lastIndexOf(".") + 1);
                if (fileExtension.equals(CONFIGURATION_FILE_EXTENSION)) {
                    String configName = configPath.substring(
                            configPath.lastIndexOf(File.separator) + 1, configPath.lastIndexOf("."));

                    List<PropertyDTO> properties = StackConfigUtils.loadConfig(configPath);

                    ServiceConfigDTO serviceConfigDTO = new ServiceConfigDTO();
                    serviceConfigDTO.setName(configName);
                    serviceConfigDTO.setProperties(properties);
                    configs.add(serviceConfigDTO);
                }
            }
        }

        SERVICE_CONFIG_MAP.put(serviceName, configs);
    }

    private static void parseServiceTemplates(File file, String serviceName) {
        File templateFolder = new File(file.getAbsolutePath(), TEMPLATE_FOLDER);
        if (templateFolder.exists()) {
            Map<String, String> map = SERVICE_TEMPLATE_MAP.computeIfAbsent(serviceName, k -> new HashMap<>());
            parseTemplateFiles(templateFolder, templateFolder, map);
        }
    }

    private static void parseTemplateFiles(File templateRoot, File currentFolder, Map<String, String> templateMap) {
        for (File file : Optional.ofNullable(currentFolder.listFiles()).orElse(new File[0])) {
            if (file.isDirectory()) {
                parseTemplateFiles(templateRoot, file, templateMap);
            } else {
                String relativePath =
                        templateRoot.toURI().relativize(file.toURI()).getPath();
                String content = FileUtils.readFile2Str(file);
                templateMap.put(relativePath, content);
            }
        }
    }

    private static void parseDag(File file) {
        File dependencyFile = new File(file.getAbsolutePath(), DEPENDENCY_FILE_NAME);
        if (dependencyFile.exists()) {
            Map<String, List<String>> dependencyMapByFile = JsonUtils.readFromFile(dependencyFile);
            for (Map.Entry<String, List<String>> entry : dependencyMapByFile.entrySet()) {
                String blocked = entry.getKey().split(ROLE_COMMAND_SPLIT)[0].toLowerCase()
                        + ROLE_COMMAND_SPLIT
                        + entry.getKey().split(ROLE_COMMAND_SPLIT)[1];
                List<String> blockers = entry.getValue().stream()
                        .map(x -> x.split(ROLE_COMMAND_SPLIT)[0].toLowerCase()
                                + ROLE_COMMAND_SPLIT
                                + x.split(ROLE_COMMAND_SPLIT)[1])
                        .toList();

                DAG.addNodeIfAbsent(blocked, getCommandWrapper(blocked));
                for (String blocker : blockers) {
                    DAG.addNodeIfAbsent(blocker, getCommandWrapper(blocker));
                    DAG.addEdge(blocker, blocked, new DagGraphEdge(blocker, blocked), false);
                }
            }
        }
    }

    private static ComponentCommandWrapper getCommandWrapper(String roleCommand) {
        String[] split = roleCommand.split(ROLE_COMMAND_SPLIT);
        String role = split[0];
        String command = split[1];

        if (!EnumUtils.isValidEnum(Command.class, command)) {
            throw new ServerException("Unsupported command: " + command);
        }

        return new ComponentCommandWrapper(role, Command.valueOf(command));
    }

    /**
     * Load stack folder as file
     */
    private static File loadStacksFolder() throws ApiException {
        String stackPath = ProjectPathUtils.getServerStackPath();
        File file = new File(stackPath);
        if (!file.exists()) {
            throw new ServerException("Can't find stack folder");
        }

        log.info("stack file: {}", file);
        return file;
    }

    public static String getFullStackName(StackDTO stackDTO) {
        return CaseUtils.toCamelCase(stackDTO.getStackName()) + "-" + stackDTO.getStackVersion();
    }

    public static List<StackDTO> getAllStacks() {
        return new ArrayList<>(STACK_SERVICE_MAP.keySet());
    }

    public static StackDTO getServiceStack(String serviceName) {
        for (Map.Entry<StackDTO, List<ServiceDTO>> entry : STACK_SERVICE_MAP.entrySet()) {
            for (ServiceDTO serviceDTO : entry.getValue()) {
                if (serviceDTO.getName().equals(serviceName)) {
                    return entry.getKey();
                }
            }
        }

        throw new ServerException("Service not found: " + serviceName);
    }

    public static List<ServiceDTO> getServiceDTOList(StackDTO stackDTO) {
        List<ServiceDTO> serviceDTOList = STACK_SERVICE_MAP.get(stackDTO);
        if (serviceDTOList == null) {
            throw new ServerException("Stack not found: " + stackDTO);
        }

        return serviceDTOList;
    }

    public static ServiceDTO getServiceDTO(String serviceName) {
        for (Map.Entry<StackDTO, List<ServiceDTO>> entry : STACK_SERVICE_MAP.entrySet()) {
            for (ServiceDTO serviceDTO : entry.getValue()) {
                if (serviceDTO.getName().equals(serviceName)) {
                    return serviceDTO;
                }
            }
        }

        throw new ServerException("Service not found: " + serviceName);
    }

    public static ComponentDTO getComponentDTO(String componentName) {
        for (Map.Entry<StackDTO, List<ServiceDTO>> entry : STACK_SERVICE_MAP.entrySet()) {
            for (ServiceDTO serviceDTO : entry.getValue()) {
                for (ComponentDTO componentDTO : serviceDTO.getComponents()) {
                    if (componentDTO.getName().equals(componentName)) {
                        return componentDTO;
                    }
                }
            }
        }

        throw new ServerException("Component not found: " + componentName);
    }

    public static ServiceDTO getServiceDTOByComponentName(String componentName) {
        for (Map.Entry<StackDTO, List<ServiceDTO>> entry : STACK_SERVICE_MAP.entrySet()) {
            for (ServiceDTO serviceDTO : entry.getValue()) {
                for (ComponentDTO componentDTO : serviceDTO.getComponents()) {
                    if (componentDTO.getName().equals(componentName)) {
                        return serviceDTO;
                    }
                }
            }
        }

        throw new ServerException("Service not found by component name: " + componentName);
    }

    public static Boolean isServerComponent(String componentName) {
        ComponentDTO componentDTO = getComponentDTO(componentName);
        return componentDTO.getCategory().equalsIgnoreCase(ComponentCategories.SERVER);
    }

    public static Boolean isClientComponent(String componentName) {
        ComponentDTO componentDTO = getComponentDTO(componentName);
        return componentDTO.getCategory().equalsIgnoreCase(ComponentCategories.CLIENT);
    }
}
