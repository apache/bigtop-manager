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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hive;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.tarball.FileDownloader;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxFileUtils;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Slf4j
@AutoService(Script.class)
public class HiveMetastoreScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.add(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);

        return HiveSetup.configure(params);
    }

    @Override
    public ShellResult init(Params params) {
        downloadMySQLJdbcDriver(params);
        initSchema(params);
        return ShellResult.success();
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        HiveParams hiveParams = (HiveParams) params;
        try {
            initSchema(params);
            String cmd = MessageFormat.format(
                    "{0}/hive-service.sh metastore " + hiveParams.getHiveMetastorePidFile(),
                    hiveParams.serviceHome() + "/bin");
            ShellResult shellResult = LinuxOSUtils.sudoExecCmd(cmd, hiveParams.user());
            if (shellResult.getExitCode() != 0) {
                throw new StackException("Failed to start HiveMetastore: {0}", shellResult.getErrMsg());
            }
            long startTime = System.currentTimeMillis();
            long maxWaitTime = 5000;
            long pollInterval = 500;

            while (System.currentTimeMillis() - startTime < maxWaitTime) {
                ShellResult statusResult = status(params);
                if (statusResult.getExitCode() == 0) {
                    return statusResult;
                }
                Thread.sleep(pollInterval);
            }
            return status(params);
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        HiveParams hiveParams = (HiveParams) params;
        int pid = Integer.parseInt(
                LinuxFileUtils.readFile(hiveParams.getHiveMetastorePidFile()).replaceAll("\r|\n", ""));
        String cmd = "kill -9 " + pid;
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hiveParams.user());
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        HiveParams hiveParams = (HiveParams) params;
        return LinuxOSUtils.checkProcess(hiveParams.getHiveMetastorePidFile());
    }

    private void downloadMySQLJdbcDriver(Params params) {
        RepoInfo repoInfo = LocalSettings.repo("mysql-connector-j");
        FileDownloader.download(params.stackHome(), repoInfo);
        LinuxFileUtils.moveFile(params.stackHome() + "/mysql-connector-j-8.0.33.jar", params.serviceHome() + "/lib/");
        LinuxFileUtils.updateOwner(params.serviceHome() + "/lib", params.user(), params.group(), true);
        LinuxFileUtils.updatePermissions(params.serviceHome() + "/lib", Constants.PERMISSION_755, true);
    }

    private void initSchema(Params params) {
        try {
            HiveParams hiveParams = (HiveParams) params;
            String cmd = hiveParams.serviceHome() + "/bin/schematool -validate -dbType mysql";
            ShellResult shellResult = LinuxOSUtils.sudoExecCmd(cmd, hiveParams.user());
            String clusterName = LocalSettings.cluster().getName();
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE
                    && shellResult.getErrMsg().contains("Table '" + clusterName + "_hive.VERSION' doesn't exist")) {
                // init schema
                cmd = hiveParams.serviceHome() + "/bin/schematool -initSchema -dbType mysql";
                // TODO previous cmd will kill agent application, not sure why
                // temp solve by running with nohup and wait for 5 seconds to let it finish
                cmd = "nohup " + cmd + " > /dev/null 2>&1 &";
                shellResult = LinuxOSUtils.sudoExecCmd(cmd, hiveParams.user());
                Thread.sleep(5000);
                if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                    throw new StackException(shellResult.getErrMsg());
                }
            }
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public String getComponentName() {
        return "hive_metastore";
    }
}
