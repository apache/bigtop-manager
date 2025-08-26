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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.kafka;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.enums.ConfigType;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.bigtop.manager.common.constants.Constants.PERMISSION_644;
import static org.apache.bigtop.manager.common.constants.Constants.PERMISSION_755;
import static org.apache.bigtop.manager.common.constants.Constants.ROOT_USER;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaSetup {

    public static ShellResult configure(Params params) {
        log.info("Configuring Kafka");
        KafkaParams kafkaParams = (KafkaParams) params;

        String confDir = kafkaParams.confDir();
        String kafkaUser = kafkaParams.user();
        String kafkaGroup = kafkaParams.group();

        LinuxFileUtils.createDirectories(kafkaParams.getKafkaDataDir(), kafkaUser, kafkaGroup, PERMISSION_755, true);
        LinuxFileUtils.createDirectories(kafkaParams.getKafkaLogDir(), kafkaUser, kafkaGroup, PERMISSION_755, true);
        LinuxFileUtils.createDirectories(kafkaParams.getKafkaPidDir(), kafkaUser, kafkaGroup, PERMISSION_755, true);

        List<String> zookeeperServerHosts = LocalSettings.componentHosts("zookeeper_server");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("zk_server_list", zookeeperServerHosts);
        paramMap.put("host", kafkaParams.hostname());
        LinuxFileUtils.toFile(
                ConfigType.PROPERTIES,
                MessageFormat.format("{0}/server.properties", confDir),
                kafkaUser,
                kafkaGroup,
                PERMISSION_644,
                kafkaParams.kafkaBroker(),
                paramMap);

        LinuxFileUtils.toFileByTemplate(
                kafkaParams.getKafkaEnvContent(),
                MessageFormat.format("{0}/kafka-env.sh", confDir),
                kafkaUser,
                kafkaGroup,
                PERMISSION_644,
                kafkaParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                kafkaParams.getKafkaLog4jContent(),
                MessageFormat.format("{0}/log4j.properties", confDir),
                kafkaUser,
                kafkaGroup,
                PERMISSION_644,
                kafkaParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                kafkaParams.kafkaLimits(),
                MessageFormat.format("{0}/kafka.conf", KafkaParams.LIMITS_CONF_DIR),
                ROOT_USER,
                ROOT_USER,
                PERMISSION_644,
                kafkaParams.getGlobalParamsMap());

        log.info("Successfully configured Kafka");
        return ShellResult.success();
    }
}
