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

import org.apache.bigtop.manager.ai.assistant.GeneralAssistantFactory;
import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.assistant.store.PersistentChatMemoryStore;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantFactory;
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

    private AIAssistantFactory aiAssistantFactory;

    public AIAssistantFactory getAIAssistantFactory() {
        if (aiAssistantFactory == null) {
            aiAssistantFactory =
                    new GeneralAssistantFactory(new PersistentChatMemoryStore(chatThreadDao, chatMessageDao));
        }
        return aiAssistantFactory;
    }

    private AIAssistantConfig getAIAssistantConfig(
            String model, Map<String, String> credentials, Map<String, String> configs) {
        return AIAssistantConfig.builder()
                .setModel(model)
                .setLanguage(LocaleContextHolder.getLocale().toString())
                .addCredentials(credentials)
                .addConfigs(configs)
                .build();
    }

    private PlatformType getPlatformType(String platformName) {
        return PlatformType.getPlatformType(platformName.toLowerCase());
    }

    private Boolean testAuthorization(String platformName, String model, Map<String, String> credentials) {
        AIAssistantConfig aiAssistantConfig = AIAssistantConfig.builder()
                .setModel(model)
                .setLanguage(LocaleContextHolder.getLocale().toString())
                .addCredentials(credentials)
                .build();
        AIAssistant aiAssistant = getAIAssistantFactory().create(getPlatformType(platformName), aiAssistantConfig);
        try {
            return aiAssistant.test();
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT, e.getMessage());
        }
    }

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
        PlatformPO platformPO = platformDao.findById(authPlatformDTO.getPlatformId());
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }

        Map<String, String> credentialSet =
                getStringMap(authPlatformDTO, PlatformConverter.INSTANCE.fromPO2DTO(platformPO));

        authPlatformDTO.setAuthCredentials(credentialSet);
        AuthPlatformPO authPlatformPO = AuthPlatformConverter.INSTANCE.fromDTO2PO(authPlatformDTO);
        if (authPlatformDTO.getIsTested()) {
            authPlatformPO.setStatus(AuthPlatformStatus.NORMAL.getCode());
        } else {
            authPlatformPO.setStatus(AuthPlatformStatus.UNAVAILABLE.getCode());
        }

        authPlatformDao.save(authPlatformPO);
        return AuthPlatformConverter.INSTANCE.fromPO2VO(authPlatformPO, platformPO);
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

    @Override
    public boolean deleteAuthorizedPlatform(Long authId) {
        AuthPlatformPO authPlatformPO = authPlatformDao.findById(authId);
        if (authPlatformPO == null || authPlatformPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }

        authPlatformPO.setIsDeleted(true);
        authPlatformDao.partialUpdateById(authPlatformPO);

        List<ChatThreadPO> chatThreadPOS = chatThreadDao.findAllByAuthId(authPlatformPO.getId());
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            chatThreadPO.setIsDeleted(true);
            chatThreadDao.partialUpdateById(chatThreadPO);
            List<ChatMessagePO> chatMessagePOS = chatMessageDao.findAllByThreadId(chatThreadPO.getId());
            for (ChatMessagePO chatMessagePO : chatMessagePOS) {
                chatMessagePO.setIsDeleted(true);
                chatMessageDao.partialUpdateById(chatMessagePO);
            }
        }

        return true;
    }

    @Override
    public boolean testAuthorizedPlatform(AuthPlatformDTO authPlatformDTO) {
        if (authPlatformDTO.getId() != null) {
            authPlatformDTO =
                    AuthPlatformConverter.INSTANCE.fromPO2DTO(authPlatformDao.findById(authPlatformDTO.getId()));
        }

        PlatformPO platformPO = platformDao.findById(authPlatformDTO.getPlatformId());
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        List<String> supportModels = List.of(platformPO.getSupportModels().split(","));
        if (supportModels.isEmpty() || !supportModels.contains(authPlatformDTO.getModel())) {
            throw new ApiException(ApiExceptionEnum.MODEL_NOT_SUPPORTED);
        }

        if (authPlatformDTO.getId() != null) {
            AuthPlatformPO authPlatformPO = authPlatformDao.findById(authPlatformDTO.getId());
            if (authPlatformPO == null || authPlatformPO.getIsDeleted()) {
                throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
            }
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
            authPlatformPO.setStatus(AuthPlatformStatus.NORMAL.getCode());
            authPlatformDao.partialUpdateById(authPlatformPO);
        }

        return true;
    }

    @Override
    public AuthPlatformVO updateAuthorizedPlatform(AuthPlatformDTO authPlatformDTO) {
        AuthPlatformPO authPlatformPO = authPlatformDao.findById(authPlatformDTO.getId());
        if (authPlatformPO == null || authPlatformPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }

        authPlatformPO.setName(authPlatformDTO.getName());
        authPlatformPO.setNotes(authPlatformDTO.getNotes());
        if (!authPlatformPO.getModel().equals(authPlatformDTO.getModel())) {
            authPlatformPO.setStatus(AuthPlatformStatus.UNAVAILABLE.getCode());
        }
        authPlatformPO.setModel(authPlatformDTO.getModel());
        authPlatformDao.partialUpdateById(authPlatformPO);

        return AuthPlatformConverter.INSTANCE.fromPO2VO(
                authPlatformPO, platformDao.findById(authPlatformPO.getPlatformId()));
    }

    @Override
    public AuthPlatformVO switchAuthPlatform(AuthPlatformDTO authPlatformDTO) {
        AuthPlatformPO authPlatformPO = authPlatformDao.findById(authPlatformDTO.getId());
        if (authPlatformPO == null || authPlatformPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }

        if (authPlatformDTO.getIsActive() && AuthPlatformStatus.isAvailable(authPlatformPO.getStatus())) {
            switchActivePlatform(authPlatformPO.getId());
        } else if (!authPlatformDTO.getIsActive() && AuthPlatformStatus.isActive(authPlatformPO.getStatus())) {
            authPlatformPO.setStatus(AuthPlatformStatus.NORMAL.getCode());
            authPlatformDao.partialUpdateById(authPlatformPO);
        }
        return AuthPlatformConverter.INSTANCE.fromPO2VO(
                authPlatformDao.findById(authPlatformDTO.getId()),
                platformDao.findById(authPlatformPO.getPlatformId()));
    }

    private void switchActivePlatform(Long id) {
        List<AuthPlatformPO> authPlatformPOS = authPlatformDao.findAll();
        for (AuthPlatformPO authPlatformPO : authPlatformPOS) {
            if (!AuthPlatformStatus.isAvailable(authPlatformPO.getStatus())) {
                continue;
            }
            if (authPlatformPO.getId().equals(id)) {
                authPlatformPO.setStatus(AuthPlatformStatus.ACTIVE.getCode());
            } else {
                authPlatformPO.setStatus(AuthPlatformStatus.NORMAL.getCode());
            }
        }
        authPlatformDao.partialUpdateByIds(authPlatformPOS);
    }
}
