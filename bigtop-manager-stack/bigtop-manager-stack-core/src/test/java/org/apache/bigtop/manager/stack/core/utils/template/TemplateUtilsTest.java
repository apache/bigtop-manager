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
package org.apache.bigtop.manager.stack.core.utils.template;

import org.apache.bigtop.manager.stack.core.enums.ConfigType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TemplateUtilsTest {

    private MockedStatic<BaseTemplate> baseTemplateMockedStatic;

    @BeforeEach
    public void setUp() {
        baseTemplateMockedStatic = mockStatic(BaseTemplate.class);
        baseTemplateMockedStatic
                .when(() -> BaseTemplate.writeTemplate(anyString(), any(), anyString()))
                .thenAnswer(invocation -> null);
        baseTemplateMockedStatic
                .when(() -> BaseTemplate.writeTemplateAsString(any(), anyString()))
                .thenAnswer(invocation -> null);
        baseTemplateMockedStatic
                .when(() -> BaseTemplate.writeCustomTemplate(anyString(), any(), anyString()))
                .thenAnswer(invocation -> null);
    }

    @AfterEach
    public void tearDown() {
        baseTemplateMockedStatic.close();
    }

    @Test
    public void testMap2TemplateWithoutParamMap() {
        ConfigType configType = ConfigType.UNKNOWN;
        String fileName = "fileName";
        Map<String, Object> configMap = new HashMap<>();

        TemplateUtils.map2Template(configType, fileName, configMap, null);
        baseTemplateMockedStatic.verify(() -> BaseTemplate.writeTemplate(eq(fileName), any(), anyString()), times(1));
    }

    @Test
    public void testMap2TemplateWithParamMap() {
        ConfigType configType = ConfigType.UNKNOWN;
        String fileName = "fileName";
        Map<String, Object> configMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();

        TemplateUtils.map2Template(configType, fileName, configMap, paramMap);
        baseTemplateMockedStatic.verify(() -> BaseTemplate.writeTemplateAsString(any(), anyString()), times(1));
        baseTemplateMockedStatic.verify(() -> BaseTemplate.writeCustomTemplate(eq(fileName), any(), any()), times(1));
    }

    @Test
    public void testMap2CustomTemplateWithoutParamMap() {
        String template = "template";
        String fileName = "fileName";
        Map<String, Object> configMap = new HashMap<>();

        TemplateUtils.map2CustomTemplate(template, fileName, configMap, null);
        baseTemplateMockedStatic.verify(
                () -> BaseTemplate.writeCustomTemplate(eq(fileName), eq(configMap), any()), times(1));
    }

    @Test
    public void testMap2CustomTemplateWithParamMap() {
        String template = "template";
        String fileName = "fileName";
        Map<String, Object> configMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();

        TemplateUtils.map2CustomTemplate(template, fileName, configMap, paramMap);
        baseTemplateMockedStatic.verify(
                () -> BaseTemplate.writeCustomTemplateAsString(eq(configMap), eq(template)), times(1));
        baseTemplateMockedStatic.verify(
                () -> BaseTemplate.writeCustomTemplate(eq(fileName), eq(paramMap), any()), times(1));
    }
}
