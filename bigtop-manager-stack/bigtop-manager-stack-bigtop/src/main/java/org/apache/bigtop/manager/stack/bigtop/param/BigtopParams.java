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
package org.apache.bigtop.manager.stack.bigtop.param;

import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.stack.core.param.BaseParams;

import java.text.MessageFormat;

public abstract class BigtopParams extends BaseParams {

    protected BigtopParams(CommandPayload commandPayload) {
        super(commandPayload);
    }

    public String stackBinDir() {
        String stackName = this.commandPayload.getStackName();
        String stackVersion = this.commandPayload.getStackVersion();
        String root = this.commandPayload.getRootDir();
        return MessageFormat.format("{0}/{1}/{2}/usr/bin", root, stackName.toLowerCase(), stackVersion);
    }

    public String stackLibDir() {
        String stackName = this.commandPayload.getStackName();
        String stackVersion = this.commandPayload.getStackVersion();
        String root = this.commandPayload.getRootDir();
        return MessageFormat.format("{0}/{1}/{2}/usr/lib", root, stackName.toLowerCase(), stackVersion);
    }

    /**
     * service home dir
     */
    @Override
    public String serviceHome() {
        String service = this.commandPayload.getServiceName();
        return stackLibDir() + "/" + service.toLowerCase();
    }
}
