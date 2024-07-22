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
package org.apache.bigtop.manager.common.utils.os;

import org.apache.bigtop.manager.common.shell.ShellExecutor;
import org.apache.bigtop.manager.common.shell.ShellResult;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TimeSyncDetection {

    public static ShellResult checkTimeSync() {
        List<String> params = new ArrayList<>();
        params.add("systemctl");
        params.add("status");
        params.add("chronyd");
        ShellResult shellResult;
        try {
            log.info("Checking service chronyd status");
            shellResult = ShellExecutor.execCommand(params);
            if (shellResult.getExitCode() == 0) {
                log.info("Service chronyd is enabled");
                return shellResult;
            }

            log.info("Service chronyd is not enabled, checking ntpd status");
            params.remove(params.size() - 1);
            params.add("ntpd");
            shellResult = ShellExecutor.execCommand(params);
            if (shellResult.getExitCode() == 0) {
                log.info("Service ntpd is enabled");
                return shellResult;
            }

            log.info("Service ntpd is not enabled");
        } catch (IOException e) {
            shellResult = new ShellResult();
            shellResult.setExitCode(-1);
            shellResult.setErrMsg("Neither chronyd nor ntpd check failed");
        }

        return shellResult;
    }
}
