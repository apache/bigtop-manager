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
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Properties;

@Slf4j
@AutoService(Script.class)
public class ZkfcScript extends AbstractServerScript {
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
    public ShellResult init(Params params) {
        return formatZk(params);
    }

    public ShellResult formatZk(Params params) {
        configure(params);
        HadoopParams hadoopParams = (HadoopParams) params;

        String formatCmd = MessageFormat.format(
                "{0}/hdfs --config {1} zkfc -formatZK", hadoopParams.binDir(), hadoopParams.confDir());
        try {
            return LinuxOSUtils.sudoExecCmd(formatCmd, hadoopParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        HadoopParams hadoopParams = (HadoopParams) params;

        // Ensure ZKFC is formatted before starting.
        // If the parent znode does not exist, zkfc will fail with:
        // "Parent znode does not exist. Run with -formatZK flag to initialize ZooKeeper."
        // Make this step idempotent: try start, if it indicates not formatted then format and retry.
        String startCmd = MessageFormat.format("{0}/hdfs --daemon start zkfc", hadoopParams.binDir());
        try {
            ShellResult startRes = LinuxOSUtils.sudoExecCmd(startCmd, hadoopParams.user());
            if (startRes.getExitCode() == 0) {
                return startRes;
            }

            String err = (startRes.getErrMsg() == null ? "" : startRes.getErrMsg());
            String out = (startRes.getOutput() == null ? "" : startRes.getOutput());
            String combined = out + "\n" + err;
            if (combined.contains("Parent znode does not exist") || combined.contains("-formatZK")) {
                log.warn("ZKFC not formatted, attempting to formatZK and retry start. stdout/stderr: {}", combined);
                ShellResult fmt = formatZk(params);
                if (fmt.getExitCode() != 0) {
                    return fmt;
                }
                return LinuxOSUtils.sudoExecCmd(startCmd, hadoopParams.user());
            }

            return startRes;
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        HadoopParams hadoopParams = (HadoopParams) params;
        String cmd = MessageFormat.format("{0}/hdfs --daemon stop zkfc", hadoopParams.binDir());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, hadoopParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        HadoopParams hadoopParams = (HadoopParams) params;
        return LinuxOSUtils.checkProcess(hadoopParams.getZkfcPidFile());
    }

    @Override
    public String getComponentName() {
        return "zkfc";
    }
}
