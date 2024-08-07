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
import org.apache.bigtop.manager.spi.stack.Params;
import org.apache.bigtop.manager.spi.stack.Script;
import org.apache.bigtop.manager.stack.common.exception.StackException;
import org.apache.bigtop.manager.stack.common.utils.PackageUtils;
import org.apache.bigtop.manager.stack.common.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;

@Slf4j
@AutoService(Script.class)
public class KafkaBrokerScript implements Script {

    @Override
    public ShellResult install(Params params) {
        return PackageUtils.install(params.getPackageList());
    }

    @Override
    public ShellResult configure(Params params) {
        return KafkaSetup.config(params);
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        KafkaParams kafkaParams = (KafkaParams) params;

        String cmd = MessageFormat.format(
                "sh {0}/bin/kafka-server-start.sh {0}/config/server.properties > /dev/null 2>&1 & echo -n $!>{1}",
                kafkaParams.serviceHome(), kafkaParams.getKafkaPidFile());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, kafkaParams.user());
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        KafkaParams kafkaParams = (KafkaParams) params;
        String cmd = MessageFormat.format("sh {0}/bin/kafka-server-stop.sh", kafkaParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, kafkaParams.user());
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        KafkaParams kafkaParams = (KafkaParams) params;
        return LinuxOSUtils.checkProcess(kafkaParams.getKafkaPidFile());
    }

    public ShellResult test(Params params) {
        KafkaParams kafkaParams = (KafkaParams) params;
        try {
            return LinuxOSUtils.sudoExecCmd("date", kafkaParams.user());
        } catch (IOException e) {
            throw new StackException(e);
        }
    }
}
