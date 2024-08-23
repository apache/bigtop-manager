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
package org.apache.bigtop.manager.stack.infra.v1_0_0.prometheus;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;

import java.text.MessageFormat;
import java.util.Properties;

@AutoService(Script.class)
public class PrometheusServerScript extends AbstractServerScript {

    @Override
    public ShellResult install(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.install(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        return PrometheusSetup.config(params);
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        PrometheusParams prometheusParams = (PrometheusParams) params;
        String cmd = MessageFormat.format(
                "nohup {0}/prometheus --config.file={0}/prometheus.yml --storage.tsdb.path={0}/data > {0}/nohup.out 2>&1 &",
                prometheusParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, prometheusParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        PrometheusParams prometheusParams = (PrometheusParams) params;
        String cmd = "pkill -f prometheus";
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, prometheusParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        String cmd = "pgrep -f prometheus";
        try {
            ShellResult result = LinuxOSUtils.execCmd(cmd);
            if (result.getExitCode() == 0) {
                return ShellResult.success();
            } else {
                return new ShellResult(-1, "", "Prometheus is not running");
            }
        } catch (Exception e) {
            throw new StackException(e);
        }
    }
}
