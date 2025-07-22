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

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;

import static org.apache.bigtop.manager.common.constants.Constants.PERMISSION_755;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DorisSetup {

    public static ShellResult config(Params params, String type) throws IOException {
        DorisParams dorisParams = (DorisParams) params;
        String user = dorisParams.user();
        String group = dorisParams.group();

        LinuxFileUtils.toFileByTemplate(
                dorisParams.dorisConf(),
                MessageFormat.format("{0}/doris.conf", dorisParams.getLimitsConfDir()),
                Constants.ROOT_USER,
                Constants.ROOT_GROUP,
                Constants.PERMISSION_644,
                dorisParams.getGlobalParamsMap());

        LinuxFileUtils.toFileByTemplate(
                dorisParams.dorisSysctlConf(),
                MessageFormat.format("{0}/doris-sysctl.conf", dorisParams.getSysctlConfDir()),
                Constants.ROOT_USER,
                Constants.ROOT_GROUP,
                Constants.PERMISSION_644,
                dorisParams.getGlobalParamsMap());

        ShellResult sr1 = LinuxOSUtils.sudoExecCmd("sysctl -p /etc/sysctl.d/doris-sysctl.conf");
        if (sr1.getExitCode() != MessageConstants.SUCCESS_CODE) {
            String errMsg = sr1.formatMessage("Failed to apply sysctl settings");
            log.error(errMsg);
            throw new StackException(errMsg);
        }

        ShellResult sr2 = LinuxOSUtils.sudoExecCmd("swapoff -a");
        if (sr2.getExitCode() != MessageConstants.SUCCESS_CODE) {
            String errMsg = sr2.formatMessage("Failed to run swapoff command");
            log.error(errMsg);
            throw new StackException(errMsg);
        }

        if (type.equals("doris_fe")) {
            LinuxFileUtils.createDirectories(dorisParams.dorisFeConfDir(), user, group, PERMISSION_755, false);
            LinuxFileUtils.createDirectories(dorisParams.dorisFePidDir(), user, group, PERMISSION_755, false);
            LinuxFileUtils.createDirectories(dorisParams.dorisFeLogDir(), user, group, PERMISSION_755, false);
            LinuxFileUtils.createDirectories(dorisParams.dorisFeMetaDir(), user, group, PERMISSION_755, false);

            LinuxFileUtils.toFileByTemplate(
                    dorisParams.dorisFeConfContent(),
                    MessageFormat.format("{0}/fe.conf", dorisParams.dorisFeConfDir()),
                    user,
                    group,
                    Constants.PERMISSION_644,
                    dorisParams.getGlobalParamsMap(),
                    dorisParams.getGlobalParamsMap());

        } else if (type.equals("doris_be")) {
            LinuxFileUtils.createDirectories(dorisParams.dorisBeConfDir(), user, group, PERMISSION_755, false);
            LinuxFileUtils.createDirectories(dorisParams.dorisBePidDir(), user, group, PERMISSION_755, false);
            LinuxFileUtils.createDirectories(dorisParams.dorisBeLogDir(), user, group, PERMISSION_755, false);
            LinuxFileUtils.createDirectories(dorisParams.dorisBeStorage(), user, group, PERMISSION_755, false);

            LinuxFileUtils.toFileByTemplate(
                    dorisParams.dorisBeConfContent(),
                    MessageFormat.format("{0}/be.conf", dorisParams.dorisBeConfDir()),
                    user,
                    group,
                    Constants.PERMISSION_644,
                    dorisParams.getGlobalParamsMap(),
                    dorisParams.getGlobalParamsMap());
        }

        return ShellResult.success("Doris Configure success!");
    }
}
