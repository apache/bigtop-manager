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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.spark;

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
public class SparkThriftServerScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.add(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);

        return SparkSetup.configure(params);
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        SparkParams sparkParams = (SparkParams) params;
        String cmd = MessageFormat.format("{0}/sbin/start-thriftserver.sh", sparkParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, sparkParams.user());
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        SparkParams sparkParams = (SparkParams) params;
        String cmd = MessageFormat.format("{0}/sbin/stop-thriftserver.sh", sparkParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, sparkParams.user());
        } catch (IOException e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        SparkParams sparkParams = (SparkParams) params;
        return LinuxOSUtils.checkProcess(sparkParams.getSparkThriftServerPidFile());
    }

    @Override
    public String getComponentName() {
        return "spark_thriftserver";
    }
}
