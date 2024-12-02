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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.flink;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.exception.StackException;
import org.apache.bigtop.manager.stack.core.param.Params;
import org.apache.bigtop.manager.stack.core.spi.script.AbstractServerScript;
import org.apache.bigtop.manager.stack.core.spi.script.Script;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import com.google.auto.service.AutoService;

@AutoService(Script.class)
public class FlinkHistoryServerScript extends AbstractServerScript {

    @Override
    public ShellResult configure(Params params) {
        return FlinkSetup.config(params);
    }

    @Override
    public ShellResult start(Params params) {
        configure(params);
        FlinkParams flinkParams = (FlinkParams) params;
        String hadoopClasspath = flinkParams.hadoopHome() + "/bin/hadoop classpath";
        String cmd = "export HADOOP_CLASSPATH=`" + hadoopClasspath + "`;" + flinkParams.serviceHome()
                + "/bin/historyserver.sh start";
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, flinkParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult stop(Params params) {
        FlinkParams flinkParams = (FlinkParams) params;
        String cmd = flinkParams.serviceHome() + "/bin/historyserver.sh stop";
        try {
            return LinuxOSUtils.sudoExecCmd(cmd, flinkParams.user());
        } catch (Exception e) {
            throw new StackException(e);
        }
    }

    @Override
    public ShellResult status(Params params) {
        FlinkParams flinkParams = (FlinkParams) params;
        return LinuxOSUtils.checkProcess(flinkParams.getHistoryServerPidFile());
    }
}
