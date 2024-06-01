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
package org.apache.bigtop.manager.common.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YamlUtils {

    private static final Yaml YAML;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        Representer representer = new Representer(dumperOptions);
        representer.getPropertyUtils().setSkipMissingProperties(true);

        YAML = new Yaml(representer, dumperOptions);
    }

    /**
     * Read yaml file
     *
     * @param path  source yaml file path
     * @param clazz class
     * @return object
     */
    public static <T> T readYaml(String path, Class<T> clazz) {
        FileReader reader = null;
        try {
            reader = new FileReader(path);
        } catch (FileNotFoundException e) {
            log.error(path + " File Not Found, ", e);
            return null;
        }

        BufferedReader buffer = new BufferedReader(reader);
        return YAML.loadAs(buffer, clazz);
    }

    /**
     * Write data to yaml file
     *
     * @param path out yaml file path
     * @param data yaml content, maybe Map, json or java bean
     */
    public static void writeYaml(String path, Object data) {
        try (FileWriter fileWriter = new FileWriter(path, false)) {
            YAML.dump(data, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
