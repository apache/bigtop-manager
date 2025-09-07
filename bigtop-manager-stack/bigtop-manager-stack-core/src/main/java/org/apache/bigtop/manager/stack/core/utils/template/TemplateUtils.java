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
import org.apache.bigtop.manager.stack.core.spi.param.BaseParams;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class TemplateUtils {

    /**
     * writeProperties to file
     *
     * @param fileName  fileName
     * @param configMap configMap
     * @param paramMap paramMap parameters for template
     */
    public static void map2Template(ConfigType configType, String fileName, Object configMap, Object paramMap) {
        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("model", configMap);
        try {
            if (paramMap == null) {
                BaseTemplate.writeTemplate(fileName, modelMap, configType.name());
            } else {
                processTemplateWithOrderedResolution(
                        () -> BaseTemplate.writeTemplateAsString(modelMap, configType.name()), fileName, paramMap);
            }
        } catch (Exception e) {
            log.error("writeProperties error", e);
        }
    }

    /**
     * write custom template
     */
    public static void map2CustomTemplate(String template, String fileName, Object configMap, Object paramMap) {
        try {
            if (paramMap == null) {
                BaseTemplate.writeCustomTemplate(fileName, configMap, template);
            } else {
                processTemplateWithOrderedResolution(
                        () -> BaseTemplate.writeCustomTemplateAsString(configMap, template), fileName, paramMap);
            }
        } catch (Exception e) {
            log.error("writeProperties error", e);
        }
    }

    /**
     * Process template with ordered resolution: FreeMarker first, then BaseParams.
     *
     * This ensures that FreeMarker syntax (e.g., &lt;#if host??&gt;) is processed before
     * BaseParams parameter resolution (e.g., ${host}), preventing false warnings about
     * missing parameters.
     *
     * @param freeMarkerProcessor Supplier that processes FreeMarker template and returns processed string
     * @param fileName Target file name for the template
     * @param paramMap Parameters for BaseParams resolution
     */
    private static void processTemplateWithOrderedResolution(
            Supplier<String> freeMarkerProcessor, String fileName, Object paramMap) {

        // Step 1: Process FreeMarker template syntax (<#if>, <#list>, etc.)
        String freeMarkerProcessed = freeMarkerProcessor.get();

        // Step 2: Resolve BaseParams parameters for remaining ${...} placeholders
        if (paramMap instanceof BaseParams baseParams) {
            baseParams.resolveGlobalParamsIfNeeded();
        }

        // Step 3: Write final template with resolved parameters
        BaseTemplate.writeCustomTemplate(fileName, paramMap, freeMarkerProcessed);
    }
}
