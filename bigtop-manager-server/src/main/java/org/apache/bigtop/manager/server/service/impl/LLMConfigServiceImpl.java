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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.ai.assistant.config.GeneralAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantFactory;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.AuthPlatformPO;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.po.PlatformPO;
import org.apache.bigtop.manager.dao.repository.AuthPlatformDao;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;
import org.apache.bigtop.manager.dao.repository.PlatformDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.AuthPlatformStatus;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.AuthPlatformConverter;
import org.apache.bigtop.manager.server.model.converter.PlatformConverter;
import org.apache.bigtop.manager.server.model.dto.AuthPlatformDTO;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.vo.AuthPlatformVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.LLMConfigService;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LLMConfigServiceImpl implements LLMConfigService {
    @Resource
    private PlatformDao platformDao;

    @Resource
    private AuthPlatformDao authPlatformDao;

    @Resource
    private ChatThreadDao chatThreadDao;

    @Resource
    private ChatMessageDao chatMessageDao;

    @Resource
    private AIAssistantFactory aiAssistantFactory;

    private static final String TEST_FLAG = "ZmxhZw==";
    private static final String TEST_KEY = "bm";

    @Override
    public List<PlatformVO> platforms() {
        List<PlatformPO> platformPOs = platformDao.findAll();
        return PlatformConverter.INSTANCE.fromPO2VO(platformPOs);
    }

    @Override
    public List<PlatformAuthCredentialVO> platformsAuthCredentials(Long platformId) {
        PlatformPO platformPO = platformDao.findById(platformId);
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        List<PlatformAuthCredentialVO> platformAuthCredentialVOs = new ArrayList<>();
        PlatformDTO platformDTO = PlatformConverter.INSTANCE.fromPO2DTO(platformPO);
        for (String key : platformDTO.getAuthCredentials().keySet()) {
            PlatformAuthCredentialVO platformAuthCredentialVO = new PlatformAuthCredentialVO(
                    key, platformDTO.getAuthCredentials().get(key));
            platformAuthCredentialVOs.add(platformAuthCredentialVO);
        }
        return platformAuthCredentialVOs;
    }

    @Override
    public List<AuthPlatformVO> authorizedPlatforms() {
        List<AuthPlatformVO> authorizedPlatforms = new ArrayList<>();
        List<AuthPlatformPO> authPlatformPOList = authPlatformDao.findAll();
        for (AuthPlatformPO authPlatformPO : authPlatformPOList) {
            if (authPlatformPO.getIsDeleted()) {
                continue;
            }

            PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());
            authorizedPlatforms.add(AuthPlatformConverter.INSTANCE.fromPO2VO(authPlatformPO, platformPO));
        }

        return authorizedPlatforms;
    }

    @Override
    public AuthPlatformVO addAuthorizedPlatform(AuthPlatformDTO authPlatformDTO) {
        PlatformPO platformPO = validateAndGetPlatform(authPlatformDTO.getPlatformId());

        Map<String, String> credentialSet =
                getStringMap(authPlatformDTO, PlatformConverter.INSTANCE.fromPO2DTO(platformPO));

        authPlatformDTO.setAuthCredentials(credentialSet);
        AuthPlatformPO authPlatformPO = AuthPlatformConverter.INSTANCE.fromDTO2PO(authPlatformDTO);
        if (authPlatformDTO.getTestPassed()) {
            authPlatformPO.setStatus(AuthPlatformStatus.AVAILABLE.getCode());
        } else {
            authPlatformPO.setStatus(AuthPlatformStatus.UNAVAILABLE.getCode());
        }

        authPlatformDao.save(authPlatformPO);
        return AuthPlatformConverter.INSTANCE.fromPO2VO(authPlatformPO, platformPO);
    }

    @Override
    public boolean deleteAuthorizedPlatform(Long authId) {
        AuthPlatformPO authPlatformPO = validateAndGetAuthPlatform(authId);

        if (AuthPlatformStatus.isActive(authPlatformPO.getStatus())) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_IS_ACTIVE);
        }

        authPlatformPO.setIsDeleted(true);
        authPlatformDao.partialUpdateById(authPlatformPO);

        List<ChatThreadPO> chatThreadPOS = chatThreadDao.findAllByAuthId(authPlatformPO.getId());
        softDeleteChatThreads(chatThreadPOS);

        return true;
    }

    @Override
    public boolean testAuthorizedPlatform(AuthPlatformDTO authPlatformDTO) {
        if (authPlatformDTO.getId() != null) {
            authPlatformDTO =
                    AuthPlatformConverter.INSTANCE.fromPO2DTO(authPlatformDao.findById(authPlatformDTO.getId()));
        }

        PlatformPO platformPO = validateAndGetPlatform(authPlatformDTO.getPlatformId());

        List<String> supportModels = List.of(platformPO.getSupportModels().split(","));
        if (supportModels.isEmpty() || !supportModels.contains(authPlatformDTO.getModel())) {
            throw new ApiException(ApiExceptionEnum.MODEL_NOT_SUPPORTED);
        }

        if (authPlatformDTO.getId() != null) {
            AuthPlatformPO authPlatformPO = validateAndGetAuthPlatform(authPlatformDTO.getId());

            AuthPlatformDTO existAuthPlatformDTO = AuthPlatformConverter.INSTANCE.fromPO2DTO(authPlatformPO);
            authPlatformDTO.setAuthCredentials(existAuthPlatformDTO.getAuthCredentials());
            authPlatformDTO.setModel(existAuthPlatformDTO.getModel());
        }

        Map<String, String> credentialSet =
                getStringMap(authPlatformDTO, PlatformConverter.INSTANCE.fromPO2DTO(platformPO));

        if (!testAuthorization(platformPO.getName(), authPlatformDTO.getModel(), credentialSet)) {
            throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT);
        }

        if (authPlatformDTO.getId() != null) {
            AuthPlatformPO authPlatformPO = AuthPlatformConverter.INSTANCE.fromDTO2PO(authPlatformDTO);
            authPlatformPO.setStatus(AuthPlatformStatus.AVAILABLE.getCode());
            authPlatformDao.partialUpdateById(authPlatformPO);
        }

        return true;
    }

    @Override
    public AuthPlatformVO updateAuthorizedPlatform(AuthPlatformDTO authPlatformDTO) {
        AuthPlatformPO authPlatformPO = validateAndGetAuthPlatform(authPlatformDTO.getId());

        String newModel = authPlatformDTO.getModel();
        if (newModel != null) {
            if (AuthPlatformStatus.isActive(authPlatformPO.getStatus())) {
                throw new ApiException(ApiExceptionEnum.PLATFORM_IS_ACTIVE);
            }

            authPlatformPO.setModel(newModel);

            if (authPlatformDTO.getTestPassed()) {
                authPlatformPO.setStatus(AuthPlatformStatus.AVAILABLE.getCode());
            } else {
                authPlatformPO.setStatus(AuthPlatformStatus.UNAVAILABLE.getCode());
            }
        }

        authPlatformPO.setName(authPlatformDTO.getName());
        authPlatformPO.setDesc(authPlatformDTO.getDesc());

        authPlatformDao.partialUpdateById(authPlatformPO);

        return AuthPlatformConverter.INSTANCE.fromPO2VO(
                authPlatformPO, platformDao.findById(authPlatformPO.getPlatformId()));
    }

    @Override
    public boolean activateAuthorizedPlatform(Long authId) {
        AuthPlatformPO authPlatformPO = validateAndGetAuthPlatform(authId);

        if (!AuthPlatformStatus.available(authPlatformPO.getStatus())) {
            return false;
        }

        if (AuthPlatformStatus.isActive(authPlatformPO.getStatus())) {
            return true;
        }
        switchActivePlatform(authPlatformPO.getId());
        return true;
    }

    @Override
    public boolean deactivateAuthorizedPlatform(Long authId) {
        AuthPlatformPO authPlatformPO = validateAndGetAuthPlatform(authId);

        AuthPlatformStatus authPlatformStatus = AuthPlatformStatus.fromCode(authPlatformPO.getStatus());
        if (authPlatformStatus.equals(AuthPlatformStatus.ACTIVE)) {
            authPlatformPO.setStatus(AuthPlatformStatus.AVAILABLE.getCode());
            authPlatformDao.partialUpdateById(authPlatformPO);
            return true;
        }
        return true;
    }

    @Override
    public AuthPlatformVO getAuthorizedPlatform(Long authId) {
        AuthPlatformPO authPlatformPO = validateAndGetAuthPlatform(authId);

        return AuthPlatformConverter.INSTANCE.fromPO2VO(
                authPlatformPO, platformDao.findById(authPlatformPO.getPlatformId()));
    }

    @Override
    public PlatformVO getPlatform(Long id) {
        PlatformPO platformPO = validateAndGetPlatform(id);

        return PlatformConverter.INSTANCE.fromPO2VO(platformPO);
    }

    public PlatformPO validateAndGetPlatform(Long platformId) {
        if (platformId == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }

        PlatformPO platformPO = platformDao.findById(platformId);
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        return platformPO;
    }

    public AuthPlatformPO validateAndGetAuthPlatform(Long authId) {
        if (authId == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }
        AuthPlatformPO authPlatformPO = authPlatformDao.findById(authId);
        if (authPlatformPO == null || authPlatformPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }
        return authPlatformPO;
    }

    private GeneralAssistantConfig getAIAssistantConfig(
            String platformName, String model, Map<String, String> credentials) {
        return GeneralAssistantConfig.builder()
                .setPlatformType(getPlatformType(platformName))
                .setModel(model)
                .setLanguage(LocaleContextHolder.getLocale().toString())
                .addCredentials(credentials)
                .build();
    }

    private PlatformType getPlatformType(String platformName) {
        return PlatformType.getPlatformType(platformName.toLowerCase());
    }

    private Boolean testAuthorization(String platformName, String model, Map<String, String> credentials) {
        Boolean result = testFuncCalling(platformName, model, credentials);
        log.info("Test func calling result: {}", result);
        GeneralAssistantConfig generalAssistantConfig = getAIAssistantConfig(platformName, model, credentials);
        AIAssistant aiAssistant = aiAssistantFactory.createForTest(generalAssistantConfig, null);
        try {
            return aiAssistant.test();
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT, e.getMessage());
        }
    }

    private Boolean testFuncCalling(String platformName, String model, Map<String, String> credentials) {
        ToolProvider toolProvider = (toolProviderRequest) -> {
            ToolSpecification toolSpecification = ToolSpecification.builder()
                    .name("getFlag")
                    .description("Get flag based on key")
                    .parameters(JsonObjectSchema.builder()
                            .addStringProperty("key")
                            .description("Lowercase key to get flag")
                            .build())
                    .build();
            ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
                Map<String, Object> arguments = JsonUtils.readFromString(toolExecutionRequest.arguments());
                String key = arguments.get("key").toString();
                if (key.equals(TEST_KEY)) {
                    return TEST_FLAG;
                }
                return null;
            };

            return ToolProviderResult.builder()
                    .add(toolSpecification, toolExecutor)
                    .build();
        };

        GeneralAssistantConfig generalAssistantConfig = getAIAssistantConfig(platformName, model, credentials);
        AIAssistant aiAssistant = aiAssistantFactory.createForTest(generalAssistantConfig, toolProvider);
        try {
            return aiAssistant.ask("What is the flag of " + TEST_KEY).contains(TEST_FLAG);
        } catch (Exception e) {
            log.error("Test function calling failed", e);
            return false;
        }
    }

    private void switchActivePlatform(Long id) {
        List<AuthPlatformPO> authPlatformPOS = authPlatformDao.findAll();
        for (AuthPlatformPO authPlatformPO : authPlatformPOS) {
            if (!AuthPlatformStatus.available(authPlatformPO.getStatus())) {
                continue;
            }
            if (authPlatformPO.getId().equals(id)) {
                authPlatformPO.setStatus(AuthPlatformStatus.ACTIVE.getCode());
            } else {
                authPlatformPO.setStatus(AuthPlatformStatus.AVAILABLE.getCode());
            }
        }
        authPlatformDao.partialUpdateByIds(authPlatformPOS);
    }

    private void softDeleteChatMessages(List<ChatMessagePO> chatMessagePOS) {
        for (ChatMessagePO chatMessagePO : chatMessagePOS) {
            chatMessagePO.setIsDeleted(true);
            chatMessageDao.partialUpdateById(chatMessagePO);
        }
    }

    private void softDeleteChatThreads(List<ChatThreadPO> chatThreadPOS) {
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            chatThreadPO.setIsDeleted(true);
            chatThreadDao.partialUpdateById(chatThreadPO);
            List<ChatMessagePO> chatMessagePOS = chatMessageDao.findAllByThreadId(chatThreadPO.getId());
            softDeleteChatMessages(chatMessagePOS);
        }
    }

    private static @NotNull Map<String, String> getStringMap(AuthPlatformDTO authPlatformDTO, PlatformDTO platformDTO) {
        if (platformDTO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        Map<String, String> credentialNeed = platformDTO.getAuthCredentials();
        Map<String, String> credentialGet = authPlatformDTO.getAuthCredentials();
        Map<String, String> credentialSet = new HashMap<>();
        for (String key : credentialNeed.keySet()) {
            if (!credentialGet.containsKey(key)) {
                throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT);
            }
            credentialSet.put(key, credentialGet.get(key));
        }
        return credentialSet;
    }
}
