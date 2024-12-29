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
package org.apache.bigtop.manager.ai.assistant.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class GeneralAssistantConfigTest {

    private GeneralAssistantConfig.Builder builder;
    private String model;
    private String language;
    private Map<String, String> credentials;
    private Map<String, String> configs;

    @BeforeEach
    public void setUp() {
        builder = GeneralAssistantConfig.builder();
        model = "test-model";
        language = "en-US";
        credentials = new HashMap<>();
        credentials.put("api key", "123456");

        configs = new HashMap<>();
        configs.put("threadId", "123456");
    }

    @Test
    public void testBuilderSetsValuesCorrectly() {
        GeneralAssistantConfig config = builder.setModel(model)
                .setLanguage(language)
                .addCredentials(credentials)
                .addConfigs(configs)
                .build();

        assertNotNull(config);
        assertEquals(model, config.getModel());
        assertEquals(language, config.getLanguage());
        assertEquals(credentials, config.getCredentials());
        assertEquals(configs, config.getConfigs());
    }

    @Test
    public void testBuilderAddsSingleCredential() {
        GeneralAssistantConfig config = builder.setModel(model)
                .setLanguage(language)
                .addCredential("client_id", "abcd1234")
                .build();

        assertNotNull(config);
        assertEquals("abcd1234", config.getCredentials().get("client_id"));
    }

    @Test
    public void testBuilderAddsSingleConfig() {
        GeneralAssistantConfig config = builder.setModel(model)
                .setLanguage(language)
                .addConfig("threadId", "123")
                .build();

        assertNotNull(config);
        assertEquals("123", config.getConfigs().get("threadId"));
    }

    @Test
    public void testEmptyBuilder() {
        GeneralAssistantConfig config = builder.build();

        assertNotNull(config);
        assertNull(config.getModel());
        assertNull(config.getLanguage());
        assertEquals(0, config.getCredentials().size());
        assertEquals(0, config.getConfigs().size());
    }

    @Test
    public void testMultipleCredentialsAndConfigs() {
        Map<String, String> extraCredentials = new HashMap<>();
        extraCredentials.put("client_id", "abcd1234");
        extraCredentials.put("secret_key", "secret");

        Map<String, String> extraConfigs = new HashMap<>();
        extraConfigs.put("retry", "3");

        GeneralAssistantConfig config = builder.setModel(model)
                .setLanguage(language)
                .addCredentials(extraCredentials)
                .addConfigs(extraConfigs)
                .build();

        assertEquals("abcd1234", config.getCredentials().get("client_id"));
        assertEquals("secret", config.getCredentials().get("secret_key"));
        assertEquals("3", config.getConfigs().get("retry"));
    }
}
