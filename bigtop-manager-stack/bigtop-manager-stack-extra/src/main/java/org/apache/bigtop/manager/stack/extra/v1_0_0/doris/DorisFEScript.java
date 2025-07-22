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
package org.apache.bigtop.manager.stack.extra.v1_0_0.doris;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import org.apache.commons.lang3.tuple.Pair;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

@Slf4j
@AutoService(Script.class)
public class DorisFEScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.add(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);

        try {
            return DorisSetup.config(params, "doris_fe");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        DorisParams dorisParams = (DorisParams) params;
        String hostname = dorisParams.hostname();
        String ip = dorisParams.ip();
        LinuxFileUtils.removeDirectories(dorisParams.dorisFePidFile());

        // Check if the FE is already registered
        Pair<String, List<String>> masterAndFeList = DorisService.getMasterAndFeList(dorisParams);
        List<String> feList = masterAndFeList.getRight();
        String feMaster = masterAndFeList.getLeft();

        try {
            if (feMaster.equals(hostname) || feMaster.equals(ip) || feList.contains(hostname)) {
                String cmd = MessageFormat.format("{0}/start_fe.sh --daemon", dorisParams.dorisFeBinDir());
                return LinuxOSUtils.sudoExecCmd(cmd, dorisParams.user());
            } else {
                String cmd = MessageFormat.format(
                        "{0}/start_fe.sh --helper {1}:{2,number,#} --daemon",
                        dorisParams.dorisFeBinDir(), feMaster, dorisParams.dorisFeEditLogPort());

                return LinuxOSUtils.sudoExecCmd(cmd, dorisParams.user());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult prepare(Params params) {
        DorisParams dorisParams = (DorisParams) params;
        // Register FE in Doris service
        for (String dorisFeHost : dorisParams.dorisFeHosts()) {
            DorisService.registerFollower(dorisParams, dorisFeHost);
        }
        return ShellResult.success();
    }

    @Override
    public ShellResult stop(Params params) {
        DorisParams dorisParams = (DorisParams) params;
        String cmd = MessageFormat.format("{0}/stop_fe.sh", dorisParams.dorisFeBinDir());
        try {
            ShellResult shellResult = LinuxOSUtils.sudoExecCmd(cmd, dorisParams.user());
            LinuxFileUtils.removeDirectories(dorisParams.dorisFePidDir());
            return shellResult;
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        DorisParams dorisParams = (DorisParams) params;
        return LinuxOSUtils.checkProcess(dorisParams.dorisFePidFile());
    }

    @Override
    public String getComponentName() {
        return "doris_fe";
    }
}
