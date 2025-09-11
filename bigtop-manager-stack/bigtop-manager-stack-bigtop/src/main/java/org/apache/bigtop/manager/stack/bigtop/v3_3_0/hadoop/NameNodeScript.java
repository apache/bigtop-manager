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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hadoop;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@AutoService(Script.class)
public class NameNodeScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.add(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);

        return HadoopSetup.configure(params, getComponentName());
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        HadoopParams hadoopParams = (HadoopParams) params;
        String hostname = hadoopParams.hostname();
        List<String> namenodeList = LocalSettings.componentHosts("namenode");
        try {
            if (namenodeList != null && !namenodeList.isEmpty() && hostname.equals(namenodeList.get(0))) {
                HadoopSetup.formatNameNode(hadoopParams);
                String startCmd = MessageFormat.format("{0}/hdfs --daemon start namenode", hadoopParams.binDir());
                ShellResult result = LinuxOSUtils.sudoExecCmd(startCmd, hadoopParams.user());
                if (result.getExitCode() != 0) {
                    throw new StackException("Failed to start primary NameNode: " + result.getErrMsg());
                }
                return result;
            }
            else if (namenodeList != null && namenodeList.size() >= 2 && hostname.equals(namenodeList.get(1))) {
                boolean isPrimaryReady = waitForNameNodeReady(namenodeList.get(0), hadoopParams);
                if (!isPrimaryReady) {
                    throw new StackException("Primary NameNode is not ready, cannot bootstrap standby");
                }
                String bootstrapCmd = MessageFormat.format(
                        "{0}/hdfs namenode -bootstrapStandby -nonInteractive",
                        hadoopParams.binDir()
                );
                ShellResult bootstrapResult = LinuxOSUtils.sudoExecCmd(bootstrapCmd, hadoopParams.user());
                if (bootstrapResult.getExitCode() != 0) {
                    throw new StackException("Failed to bootstrap standby NameNode: " + bootstrapResult.getErrMsg());
                }

                String startCmd = MessageFormat.format("{0}/hdfs --daemon start namenode", hadoopParams.binDir());
                ShellResult startResult = LinuxOSUtils.sudoExecCmd(startCmd, hadoopParams.user());
                if (startResult.getExitCode() != 0) {
                    throw new StackException("Failed to start standby NameNode: " + startResult.getErrMsg());
                }
                return startResult;
            } else {
                throw new StackException("Current host is not in NameNode HA list: " + hostname);
            }
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    private boolean waitForNameNodeReady(String namenodeHost, HadoopParams hadoopParams) {
        String httpPort = hadoopParams.getDfsHttpPort();
        long timeout = 5 * 60 * 1000;
        long interval = 3000;
        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline) {
            try {
                URL url = new URL("http://" + namenodeHost + ":" + httpPort + "/jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        String response = reader.lines().collect(Collectors.joining());
                        log.warn("response: " + response);
                        if (response.contains("active")) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Waiting for NameNode to be ready: " + e.getMessage());
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    @Override
    public ShellResult stop(Params params) {
        HadoopParams hadoopParams = (HadoopParams) params;
        String cmd = MessageFormat.format("{0}/hdfs --daemon stop namenode", hadoopParams.binDir());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hadoopParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        HadoopParams hadoopParams = (HadoopParams) params;
        return LinuxOSUtils.checkProcess(hadoopParams.getNameNodePidFile());
    }

    public ShellResult rebalanceHdfs(Params params) {
        HadoopParams hadoopParams = (HadoopParams) params;
        String cmd = MessageFormat.format("{0}/hdfs balancer", hadoopParams.binDir());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hadoopParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    public ShellResult printTopology(Params params) {
        HadoopParams hadoopParams = (HadoopParams) params;
        String cmd = MessageFormat.format("{0}/hdfs dfsadmin -printTopology", hadoopParams.binDir());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hadoopParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public String getComponentName() {
        return "namenode";
    }
}
