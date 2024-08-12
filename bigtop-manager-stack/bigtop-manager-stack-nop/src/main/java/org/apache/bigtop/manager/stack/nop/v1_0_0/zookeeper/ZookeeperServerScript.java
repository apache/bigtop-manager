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
package org.apache.bigtop.manager.stack.nop.v1_0_0.zookeeper;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.Params;
import org.apache.bigtop.manager.stack.core.spi.Script;

@Slf4j
@AutoService(Script.class)
public class ZookeeperServerScript implements Script {

    @Override
    public ShellResult install(Params params) {
        log.info("Default to success on dev mode");
        return ShellResult.success();
    }

    @Override
    public ShellResult configure(Params params) {
        log.info("Default to success on dev mode");
        return ShellResult.success();
    }

    @Override
    public ShellResult start(Params params) {
        log.info("Default to success on dev mode");
        return ShellResult.success();
    }

    @Override
    public ShellResult stop(Params params) {
        log.info("Default to success on dev mode");
        return ShellResult.success();
    }

    @Override
    public ShellResult status(Params params) {
        log.info("Default to success on dev mode");
        return ShellResult.success();
    }
}
