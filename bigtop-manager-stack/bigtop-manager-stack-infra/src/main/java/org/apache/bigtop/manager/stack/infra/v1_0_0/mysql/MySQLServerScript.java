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
package org.apache.bigtop.manager.stack.infra.v1_0_0.mysql;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Slf4j
@AutoService(Script.class)
public class MySQLServerScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.add(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);

        return MySQLSetup.configure(params);
    }

    @Override
    public ShellResult init(Params params) {
        String user = params.user();
        String binDir = params.serviceHome() + "/bin";
        String confPath = params.serviceHome() + "/my.cnf";
        runCommand(binDir + "/mysqld --defaults-file=" + confPath + " --initialize-insecure", user);
        return ShellResult.success();
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        MySQLParams mysqlParams = (MySQLParams) params;

        String cmd = getStartCommand(params);
        try {
            ShellResult shellResult = LinuxOSUtils.sudoExecCmd(cmd, mysqlParams.user());
            if (shellResult.getExitCode() != 0) {
                throw new StackException("Failed to start MySQL: {0}", shellResult.getErrMsg());
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
    public ShellResult prepare(Params params) {
        MySQLParams mysqlParams = (MySQLParams) params;
        String user = params.user();
        String binDir = params.serviceHome() + "/bin";
        String confPath = params.serviceHome() + "/my.cnf";
        String password = mysqlParams.getRootPassword();
        runCommand(
                MessageFormat.format(
                        "{0}/mysql --defaults-file={1} -u root -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY ''{2}'';\"",
                        binDir, confPath, password),
                user);
        runCommand(
                MessageFormat.format(
                        "{0}/mysql --defaults-file={1} -u root -p''{2}'' -e \"CREATE USER ''root''@''%'' IDENTIFIED BY ''{2}'';\"",
                        binDir, confPath, password),
                user);
        runCommand(
                MessageFormat.format(
                        "{0}/mysql --defaults-file={1} -u root -p''{2}'' -e \"GRANT ALL PRIVILEGES ON *.* TO ''root''@''%'' WITH GRANT OPTION;\"",
                        binDir, confPath, password),
                user);
        runCommand(
                MessageFormat.format(
                        "{0}/mysql --defaults-file={1} -u root -p''{2}'' -e \"FLUSH PRIVILEGES;\"",
                        binDir, confPath, password),
                user);
        return ShellResult.success();
    }

    @Override
    public ShellResult stop(Params params) {
        MySQLParams mysqlParams = (MySQLParams) params;
        String cmd = getStopCommand(params);
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, mysqlParams.user());
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        MySQLParams mysqlParams = (MySQLParams) params;
        return LinuxOSUtils.checkProcess(mysqlParams.getMysqlPidDir() + "/mysqld.pid");
    }

    private String getStartCommand(Params params) {
        MySQLParams mysqlParams = (MySQLParams) params;
        return MessageFormat.format("nohup {0}/bin/mysqld_safe > /dev/null 2>&1 &", mysqlParams.serviceHome());
    }

    private String getStopCommand(Params params) {
        MySQLParams mysqlParams = (MySQLParams) params;
        return MessageFormat.format(
                "{0}/bin/mysqladmin -u root -p''{1}'' shutdown",
                mysqlParams.serviceHome(), mysqlParams.getRootPassword());
    }

    private void runCommand(String cmd, String user) {
        try {
            ShellResult shellResult = LinuxOSUtils.sudoExecCmd(cmd, user);
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                throw new StackException(shellResult.getErrMsg());
            }
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public String getComponentName() {
        return "mysql_server";
    }
}
