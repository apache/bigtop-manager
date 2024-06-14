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
package org.apache.bigtop.manager.spi.stack;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.spi.plugin.PrioritySPI;

import org.apache.commons.lang3.StringUtils;

public interface Script extends PrioritySPI {

    ShellResult install(Params params);

    ShellResult configure(Params params);

    ShellResult start(Params params);

    ShellResult stop(Params params);

    default ShellResult restart(Params params) {
        ShellResult shellResult = stop(params);
        if (shellResult.getExitCode() != 0) {
            return shellResult;
        }
        ShellResult shellResult1 = start(params);
        if (shellResult1.getExitCode() != 0) {
            return shellResult1;
        }

        return new ShellResult(
                0,
                StringUtils.join(shellResult.getOutput(), shellResult1.getOutput()),
                StringUtils.join(shellResult.getErrMsg(), shellResult1.getErrMsg()));
    }

    ShellResult status(Params params);

    default ShellResult check(Params params) {
        return ShellResult.success();
    }
}
