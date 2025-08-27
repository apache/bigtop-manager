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
package org.apache.bigtop.manager.stack.extra.v1_0_0.seatunnel;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeaTunnelSetup {

    public static ShellResult config(Params params) {
        SeaTunnelParams seatunnelParams = (SeaTunnelParams) params;
        String user = seatunnelParams.user();
        String group = seatunnelParams.group();

        List<String> masterHostPortList = hostPort("seatunnel_master", seatunnelParams.getSeatunnelMasterPort(), 10);
        List<String> workerHostPortList = hostPort("seatunnel_worker", seatunnelParams.getSeatunnelWorkerPort(), 10);
        List<String> clientHostPortList = hostPort("seatunnel_master", seatunnelParams.getSeatunnelMasterPort(), 6);

        List<String> masterWorkerHostPortList = Stream.concat(masterHostPortList.stream(), workerHostPortList.stream())
                .toList();
        String clusterMasterWorkerHostPort = String.join("\n", masterWorkerHostPortList);
        String clusterMasterHostPort = String.join("\n", clientHostPortList);

        Map<String, Object> globalParamsMap = seatunnelParams.getGlobalParamsMap();
        globalParamsMap.put("cluster_master_worker_host_port", clusterMasterWorkerHostPort);
        globalParamsMap.put("cluster_master_host_port", clusterMasterHostPort);

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getSeatunnelContent(),
                MessageFormat.format("{0}/seatunnel.yaml", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                seatunnelParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getSeatunnelEnvContent(),
                MessageFormat.format("{0}/seatunnel-env.sh", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                seatunnelParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getSeatunnelMasterContent(),
                MessageFormat.format("{0}/hazelcast-master.yaml", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                globalParamsMap);

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getSeatunnelWorkerContent(),
                MessageFormat.format("{0}/hazelcast-worker.yaml", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                globalParamsMap);

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getSeatunnelClientContent(),
                MessageFormat.format("{0}/hazelcast-client.yaml", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                globalParamsMap);

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getJvmMasterOptionsContent(),
                MessageFormat.format("{0}/jvm_master_options", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                seatunnelParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getJvmWorkerOptionsContent(),
                MessageFormat.format("{0}/jvm_worker_options", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                seatunnelParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getJvmClientOptionsContent(),
                MessageFormat.format("{0}/jvm_client_options", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                seatunnelParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getLog4j2Content(),
                MessageFormat.format("{0}/log4j2.properties", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                seatunnelParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                seatunnelParams.getLog4j2ClientContent(),
                MessageFormat.format("{0}/log4j2_client.properties", seatunnelParams.confDir()),
                user,
                group,
                Constants.PERMISSION_644,
                seatunnelParams.getGlobalParamsMap());

        return ShellResult.success("SeaTunnel Configure success!");
    }

    private static List<String> hostPort(String componentName, String port, int spacesNum) {
        String spaces = " ".repeat(spacesNum);
        List<String> hostList = LocalSettings.componentHosts(componentName);
        hostList.sort(String::compareToIgnoreCase);
        List<String> hostPortList = new ArrayList<>();
        for (String host : hostList) {
            String format = MessageFormat.format("{0}- {1}:{2}", spaces, host, port);
            hostPortList.add(format);
        }
        return hostPortList;
    }
}
