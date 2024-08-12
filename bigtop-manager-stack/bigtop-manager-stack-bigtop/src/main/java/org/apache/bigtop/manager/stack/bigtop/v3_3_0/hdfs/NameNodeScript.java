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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hdfs;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.PackageUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import java.text.MessageFormat;

@Slf4j
@AutoService(Script.class)
public class NameNodeScript extends AbstractServerScript {

    @Override
    public ShellResult install(Params params) {
        return PackageUtils.install(params.getPackageList());
    }

    @Override
    public ShellResult configure(Params params) {
        return HdfsSetup.config(params, "namenode");
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        HdfsParams hdfsParams = (HdfsParams) params;

        HdfsSetup.formatNameNode(hdfsParams);

        String cmd = MessageFormat.format("{0} --daemon start namenode", hdfsParams.hdfsExec());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hdfsParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        HdfsParams hdfsParams = (HdfsParams) params;
        String cmd = MessageFormat.format("{0} --daemon stop namenode", hdfsParams.hdfsExec());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hdfsParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        HdfsParams hdfsParams = (HdfsParams) params;
        return LinuxOSUtils.checkProcess(hdfsParams.getNameNodePidFile());
    }

    public ShellResult rebalanceHdfs(Params params) {
        HdfsParams hdfsParams = (HdfsParams) params;
        String cmd = MessageFormat.format("{0} balancer", hdfsParams.hdfsExec());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hdfsParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    public ShellResult printTopology(Params params) {
        HdfsParams hdfsParams = (HdfsParams) params;
        String cmd = MessageFormat.format("{0} dfsadmin -printTopology", hdfsParams.hdfsExec());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hdfsParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }
}
