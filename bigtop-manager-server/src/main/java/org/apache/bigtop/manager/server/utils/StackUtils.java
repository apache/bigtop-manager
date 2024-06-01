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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.dto.TypeConfigDTO;
import org.apache.bigtop.manager.server.model.mapper.ServiceMapper;
import org.apache.bigtop.manager.server.model.mapper.StackMapper;
import org.apache.bigtop.manager.server.stack.dag.ComponentCommandWrapper;
import org.apache.bigtop.manager.server.stack.dag.DAG;
import org.apache.bigtop.manager.server.stack.dag.DagGraphEdge;
import org.apache.bigtop.manager.server.stack.pojo.ServiceModel;
import org.apache.bigtop.manager.server.stack.pojo.StackModel;
import org.apache.bigtop.manager.server.stack.xml.ServiceMetainfoXml;
import org.apache.bigtop.manager.server.stack.xml.StackMetainfoXml;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StackUtils {

    private static final String ROLE_COMMAND_SPLIT = "-";

    private static final String BIGTOP_MANAGER_STACK_PATH = "bigtop.manager.stack.path";

    private static final String META_FILE = "metainfo.xml";

    private static final String STACKS_FOLDER_NAME = "stacks";

    private static final String SERVICES_FOLDER_NAME = "services";

    private static final String CONFIGURATION_FOLDER = "configuration";

    private static final String CONFIGURATION_FILE_EXTENSION = "xml";

    private static final String DEPENDENCY_FILE_NAME = "order.json";

    private static final String NOP_STACK = "nop";

    private static final Map<String, Map<String, List<String>>> STACK_DEPENDENCY_MAP =
            new HashMap<>();

    private static final Map<String, Map<String, List<TypeConfigDTO>>> STACK_CONFIG_MAP =
            new HashMap<>();

    private static final Map<String, ImmutablePair<StackDTO, List<ServiceDTO>>> STACK_KEY_MAP =
            new HashMap<>();

    private static final Map<String, DAG<String, ComponentCommandWrapper, DagGraphEdge>> STACK_DAG_MAP =
            new HashMap<>();

    public static Map<String, Map<String, List<String>>> getStackDependencyMap() {
        return Collections.unmodifiableMap(STACK_DEPENDENCY_MAP);
    }

    public static Map<String, Map<String, List<TypeConfigDTO>>> getStackConfigMap() {
        return Collections.unmodifiableMap(STACK_CONFIG_MAP);
    }

    public static Map<String, ImmutablePair<StackDTO, List<ServiceDTO>>> getStackKeyMap() {
        return Collections.unmodifiableMap(STACK_KEY_MAP);
    }

    public static Map<String, DAG<String, ComponentCommandWrapper, DagGraphEdge>> getStackDagMap() {
        return Collections.unmodifiableMap(STACK_DAG_MAP);
    }

    /**
     * Parse stack file to generate stack model
     *
     * @return stack model {@link StackModel}
     */
    public static StackDTO parseStack(File versionFolder) {
        StackMetainfoXml stackMetainfoXml = JaxbUtils.readFromPath(
                versionFolder.getAbsolutePath() + File.separator + META_FILE,
                StackMetainfoXml.class);
        return StackMapper.INSTANCE.fromModel2DTO(stackMetainfoXml.getStack());
    }

    /**
     * Parse service file to generate service model
     *
     * @param fullStackName full stack name
     * @return service model {@link ServiceModel}
     */
    public static List<ServiceDTO> parseService(File versionFolder, String fullStackName) {
        Map<String, List<TypeConfigDTO>> mergedConfigMap = new HashMap<>();
        File[] files = new File(versionFolder.getAbsolutePath(), SERVICES_FOLDER_NAME).listFiles();
        List<ServiceDTO> services = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                log.info("service dir: {}", file);

                // metainfo.xml
                ServiceMetainfoXml serviceMetainfoXml =
                        JaxbUtils.readFromPath(file.getAbsolutePath() + "/" + META_FILE,
                                ServiceMetainfoXml.class);
                ServiceModel serviceModel = serviceMetainfoXml.getService();
                ServiceDTO serviceDTO = ServiceMapper.INSTANCE.fromModel2DTO(serviceModel);
                services.add(serviceDTO);

                // configurations
                List<TypeConfigDTO> serviceConfigList = new ArrayList<>();
                File configFolder = new File(file.getAbsolutePath(), CONFIGURATION_FOLDER);
                if (configFolder.exists()) {
                    for (File configFile : Optional.ofNullable(configFolder.listFiles())
                            .orElse(new File[0])) {
                        String configPath = configFile.getAbsolutePath();
                        String fileExtension =
                                configPath.substring(configPath.lastIndexOf(".") + 1);
                        if (fileExtension.equals(CONFIGURATION_FILE_EXTENSION)) {
                            String typeName =
                                    configPath.substring(configPath.lastIndexOf(File.separator) + 1,
                                            configPath.lastIndexOf("."));

                            List<PropertyDTO> properties = StackConfigUtils.loadConfig(configPath);
                            TypeConfigDTO typeConfigDTO = new TypeConfigDTO();
                            typeConfigDTO.setTypeName(typeName);
                            typeConfigDTO.setProperties(properties);
                            serviceConfigList.add(typeConfigDTO);
                        }
                    }
                }

                mergedConfigMap.put(serviceDTO.getServiceName(), serviceConfigList);

                // order.json
                File dependencyFile = new File(file.getAbsolutePath(), DEPENDENCY_FILE_NAME);
                if (dependencyFile.exists()) {
                    Map<String, List<String>> dependencyMap =
                            STACK_DEPENDENCY_MAP.computeIfAbsent(fullStackName, k -> new HashMap<>());

                    Map<String, List<String>> dependencyMapByFile =
                            JsonUtils.readFromFile(dependencyFile);
                    for (Map.Entry<String, List<String>> entry : dependencyMapByFile.entrySet()) {
                        String blocked = entry.getKey();
                        String fixedBlocked = blocked.split(ROLE_COMMAND_SPLIT)[0].toLowerCase() +
                                ROLE_COMMAND_SPLIT + blocked.split(ROLE_COMMAND_SPLIT)[1];
                        List<String> blockers = entry.getValue();
                        List<String> fixedBlockers = blockers.stream().map(
                                x -> x.split(ROLE_COMMAND_SPLIT)[0].toLowerCase() + ROLE_COMMAND_SPLIT +
                                        x.split(ROLE_COMMAND_SPLIT)[1])
                                .toList();

                        dependencyMap.put(fixedBlocked, fixedBlockers);
                    }
                }
            }

            STACK_CONFIG_MAP.put(fullStackName, mergedConfigMap);
        }

        // log.info("Stack config map: {}", STACK_CONFIG_MAP);
        log.info("Stack dependency map: {}", STACK_DEPENDENCY_MAP);
        return services;
    }

    /**
     * @return stack list map
     */
    public static Map<StackDTO, List<ServiceDTO>> stackList() {
        File stacksFolder = loadStacksFolder();
        File[] stackFolders = Optional.ofNullable(stacksFolder.listFiles()).orElse(new File[0]);
        Map<StackDTO, List<ServiceDTO>> stackMap = new HashMap<>();

        for (File stackFolder : stackFolders) {
            String stackName = stackFolder.getName();

            // If in dev mode, only parse nop stack
            // If not in dev mode, skip nop stack
            if (Environments.isDevMode() != stackName.equals(NOP_STACK)) {
                continue;
            }

            File[] versionFolders =
                    Optional.ofNullable(stackFolder.listFiles()).orElse(new File[0]);

            for (File versionFolder : versionFolders) {
                String stackVersion = versionFolder.getName();
                String fullStackName = fullStackName(stackName, stackVersion);
                log.info("Parsing stack: {}", fullStackName);

                checkStack(versionFolder);
                StackDTO stackDTO = parseStack(versionFolder);
                List<ServiceDTO> services = parseService(versionFolder, fullStackName);

                stackMap.put(stackDTO, services);

                STACK_KEY_MAP.put(fullStackName, new ImmutablePair<>(stackDTO, services));
            }
        }

        initializeDag();
        return stackMap;
    }

    /**
     * Initialize the DAG for each stack
     */
    private static void initializeDag() {
        for (Map.Entry<String, Map<String, List<String>>> mapEntry : StackUtils.getStackDependencyMap()
                .entrySet()) {
            String fullStackName = mapEntry.getKey();
            DAG<String, ComponentCommandWrapper, DagGraphEdge> dag = new DAG<>();

            for (Map.Entry<String, List<String>> entry : mapEntry.getValue().entrySet()) {
                String blocked = entry.getKey();
                List<String> blockers = entry.getValue();
                addToDagNode(dag, blocked);

                for (String blocker : blockers) {
                    addToDagNode(dag, blocker);
                    dag.addEdge(blocker, blocked, new DagGraphEdge(blocker, blocked), false);
                }
            }

            STACK_DAG_MAP.put(fullStackName, dag);
        }
    }

    private static void addToDagNode(DAG<String, ComponentCommandWrapper, DagGraphEdge> dag,
                                     String roleCommand) {
        String[] split = roleCommand.split(ROLE_COMMAND_SPLIT);
        String role = split[0];
        String command = split[1];

        if (!EnumUtils.isValidEnum(Command.class, command)) {
            throw new ServerException("Unsupported command: " + command);
        }

        ComponentCommandWrapper commandWrapper =
                new ComponentCommandWrapper(role, Command.valueOf(command));
        dag.addNodeIfAbsent(roleCommand, commandWrapper);
    }

    /**
     * Load stack folder as file
     */
    private static File loadStacksFolder() throws ApiException {
        String stackPath = System.getProperty(BIGTOP_MANAGER_STACK_PATH);
        stackPath = stackPath == null ? "" : stackPath;

        File file = new File(stackPath);
        if (!file.exists() || !file.isDirectory()) {
            URL url = StackUtils.class.getClassLoader().getResource(STACKS_FOLDER_NAME);
            if (url == null) {
                throw new ServerException("Can't find stack folder");
            }

            stackPath = url.getPath();
            file = new File(stackPath);
            if (!file.exists()) {
                throw new ServerException("Can't find stack folder");
            }
        }

        log.info("stack file: {}", file);
        return file;
    }

    /**
     * Check stack file
     *
     * @param versionFolder stack version folder
     */
    private static void checkStack(File versionFolder) {
        String[] list = versionFolder.list();
        if (list == null || !Arrays.asList(list).contains(META_FILE)) {
            throw new ServerException(
                    "Missing metainfo.xml in stack version folder: " + versionFolder.getAbsolutePath());
        }
    }

    /**
     * Generate full stack name
     *
     * @param stackName    bigtop
     * @param stackVersion 3.3.0
     * @return {stackName}-{stackVersion} eg. bigtop-3.3.0
     */
    public static String fullStackName(String stackName, String stackVersion) {
        return stackName + "-" + stackVersion;
    }

    public static List<ServiceDTO> getServiceDTOList(String stackName, String stackVersion) {
        Map<String, ImmutablePair<StackDTO, List<ServiceDTO>>> stackKeyMap =
                StackUtils.getStackKeyMap();
        ImmutablePair<StackDTO, List<ServiceDTO>> immutablePair =
                stackKeyMap.get(StackUtils.fullStackName(stackName, stackVersion));
        return immutablePair.getRight()
                .stream()
                .toList();
    }

    public static ServiceDTO getServiceDTO(String stackName, String stackVersion,
                                           String serviceName) {
        Map<String, ServiceDTO> serviceNameToDTO = getServiceDTOList(stackName, stackVersion)
                .stream()
                .collect(Collectors.toMap(ServiceDTO::getServiceName, Function.identity()));
        return serviceNameToDTO.get(serviceName);
    }

    public static List<ComponentDTO> getComponentDTOList(String stackName, String stackVersion) {
        Map<String, ImmutablePair<StackDTO, List<ServiceDTO>>> stackKeyMap =
                StackUtils.getStackKeyMap();
        ImmutablePair<StackDTO, List<ServiceDTO>> immutablePair =
                stackKeyMap.get(StackUtils.fullStackName(stackName, stackVersion));
        return immutablePair.getRight()
                .stream()
                .flatMap(serviceDTO -> serviceDTO.getComponents().stream())
                .toList();
    }

    public static ComponentDTO getComponentDTO(String stackName, String stackVersion,
                                               String componentName) {
        Map<String, ComponentDTO> componentNameToDTO = getComponentDTOList(stackName, stackVersion)
                .stream()
                .collect(Collectors.toMap(ComponentDTO::getComponentName, Function.identity()));
        return componentNameToDTO.get(componentName);
    }
}
