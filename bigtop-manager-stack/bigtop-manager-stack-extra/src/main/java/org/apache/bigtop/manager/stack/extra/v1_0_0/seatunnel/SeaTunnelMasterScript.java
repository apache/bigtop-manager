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

import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
@AutoService(Script.class)
public class SeaTunnelMasterScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.add(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        return SeaTunnelSetup.config(params);
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        SeaTunnelParams seatunnelParams = (SeaTunnelParams) params;
        String cmd = MessageFormat.format("{0}/bin/seatunnel-cluster.sh -d -r master", seatunnelParams.serviceHome());
        try {
            ShellResult shellResult = LinuxOSUtils.sudoExecCmd(cmd, seatunnelParams.user());
            if (shellResult.getExitCode() != 0) {
                throw new StackException("Failed to start seatunnel master: {0}", shellResult.getErrMsg());
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
        SeaTunnelParams seatunnelParams = (SeaTunnelParams) params;
        String cmd = MessageFormat.format(
                "ps -ef | grep -v grep | grep {0} | grep master | cut -d' ' -f2 | xargs kill -9",
                seatunnelParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, seatunnelParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        SeaTunnelParams seatunnelParams = (SeaTunnelParams) params;
        String cmd =
                MessageFormat.format("ps -ef | grep -v grep | grep {0} | grep master", seatunnelParams.serviceHome());
        try {
            List<String> builderParameters = Arrays.asList("sh", "-c", cmd);
            ShellResult result = ShellExecutor.execCommand(builderParameters);
            if (result.getExitCode() == 0) {
                return ShellResult.success();
            } else {
                return new ShellResult(-1, "", "SeaTunnel master is not running");
            }
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public String getComponentName() {
        return "seatunnel_master";
    }
}
