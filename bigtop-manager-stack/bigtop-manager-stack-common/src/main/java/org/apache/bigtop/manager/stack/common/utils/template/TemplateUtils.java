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
package org.apache.bigtop.manager.stack.common.utils.template;

import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.stack.common.enums.ConfigType;
import org.apache.bigtop.manager.stack.common.log.TaskLogWriter;

import java.util.HashMap;
import java.util.Map;

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
                String paramTemplate = BaseTemplate.writeTemplateAsString(modelMap, configType.name());
                BaseTemplate.writeCustomTemplate(fileName, paramMap, paramTemplate);
            }
        } catch (Exception e) {
            TaskLogWriter.error("writeProperties error: " + e.getMessage());
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
                String paramTemplate = BaseTemplate.writeCustomTemplateAsString(configMap, template);
                BaseTemplate.writeCustomTemplate(fileName, paramMap, paramTemplate);
            }
        } catch (Exception e) {
            TaskLogWriter.error("writeProperties error: " + e.getMessage());
        }
    }

}
