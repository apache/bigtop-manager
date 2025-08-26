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
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;

import java.text.MessageFormat;
import java.util.Properties;

@AutoService(Script.class)
public class PrometheusServerScript extends AbstractServerScript {

    @Override
    public ShellResult add(Params params) {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_SKIP_LEVELS, "1");

        return super.add(params, properties);
    }

    @Override
    public ShellResult configure(Params params) {
        super.configure(params);

        return PrometheusSetup.config(params);
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        PrometheusParams prometheusParams = (PrometheusParams) params;
        String cmd = MessageFormat.format(
                "nohup {0}/prometheus --config.file={1}/prometheus.yml --web.listen-address={2} --storage.tsdb.path={0}/data > {0}/nohup.out 2>&1 &",
                prometheusParams.serviceHome(), prometheusParams.serviceHome(), prometheusParams.listenAddress());
        try {
            ShellResult shellResult = LinuxOSUtils.sudoExecCmd(cmd, prometheusParams.user());
            if (shellResult.getExitCode() != 0) {
                throw new StackException("Failed to start Prometheus: {0}", shellResult.getErrMsg());
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
        PrometheusParams prometheusParams = (PrometheusParams) params;
        String cmd = MessageFormat.format("pkill -f {0}/prometheus", prometheusParams.serviceHome());
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, prometheusParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        PrometheusParams prometheusParams = (PrometheusParams) params;
        String cmd = MessageFormat.format("pgrep -f {0}/prometheus", prometheusParams.serviceHome());
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

    @Override
    public String getComponentName() {
        return "prometheus_server";
    }
}
