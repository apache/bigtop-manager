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
        // The first namenode in the list is the one that formats the cluster.
        if (namenodeList != null && !namenodeList.isEmpty() && hostname.equals(namenodeList.get(0))) {
            // Only format if not already formatted.
            HadoopSetup.formatNameNode(hadoopParams);
        }

        // For both active and standby, the start command is the same.
        // The role is determined by ZKFC at runtime.
        String startCmd = MessageFormat.format("{0}/hdfs --daemon start namenode", hadoopParams.binDir());
        try {
            return LinuxOSUtils.sudoExecCmd(startCmd, hadoopParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    /**
     * 在启用 HA 流程中由 server 侧通过 custom command 调用：初始化 shared edits
     */
    public ShellResult initializeSharedEdits(Params params) {
        configure(params);
        HadoopParams hadoopParams = (HadoopParams) params;
        try {
            boolean allJnReachable = HadoopSetup.checkAllJournalNodesPortReachable(hadoopParams);
            if (!allJnReachable) {
                throw new StackException("Cannot initializeSharedEdits: Some JournalNodes are unreachable.");
            }
        } catch (Exception e) {
            throw new StackException(e);
        }

        String cmd = MessageFormat.format(
                "{0}/hdfs --config {1} namenode -initializeSharedEdits -nonInteractive",
                hadoopParams.binDir(),
                hadoopParams.confDir());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hadoopParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    /**
     * 在启用 HA 流程中由 server 侧通过 custom command 调用：bootstrap standby
     */
    public ShellResult bootstrapStandby(Params params) {
        configure(params);
        HadoopParams hadoopParams = (HadoopParams) params;
        try {
            return bootstrapStandby(hadoopParams);
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    private ShellResult bootstrapStandby(HadoopParams hadoopParams) throws Exception {
        String cmd = MessageFormat.format("{0}/hdfs namenode -bootstrapStandby -nonInteractive", hadoopParams.binDir());
        return LinuxOSUtils.sudoExecCmd(cmd, hadoopParams.user());
    }

    private boolean waitForNameNodeReady(String namenodeHost, HadoopParams hadoopParams) {
        String httpPort = hadoopParams.getDfsHttpPort();
        long timeout = 5 * 60 * 1000;
        long interval = 3000;
        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline) {
            try {
                URL url = new URL("http://" + namenodeHost + ":" + httpPort
                        + "/jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String response = reader.lines().collect(Collectors.joining());
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
