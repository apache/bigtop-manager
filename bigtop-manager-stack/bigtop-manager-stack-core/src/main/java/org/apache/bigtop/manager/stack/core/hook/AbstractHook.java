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
package org.apache.bigtop.manager.stack.core.hook;

import org.apache.bigtop.manager.spi.stack.Hook;
import org.apache.bigtop.manager.stack.common.utils.LocalSettings;
import org.apache.bigtop.manager.stack.common.utils.linux.LinuxAccountUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class AbstractHook implements Hook {

    @Override
    public void before() {
        addUserAndGroup();

        doBefore();
    }

    @Override
    public void after() {
        doAfter();
    }

    protected abstract void doBefore();

    protected abstract void doAfter();

    private void addUserAndGroup() {
        Map<String, Set<String>> users = LocalSettings.users();
        String userGroup = LocalSettings.cluster().getUserGroup();

        for (Map.Entry<String, Set<String>> user : users.entrySet()) {
            Set<String> groups = user.getValue();
            for (String group : groups) {
                log.info("Adding group: {}", group);
                LinuxAccountUtils.groupAdd(group);
            }

            log.info(
                    "Adding user: {} to group: {} and groups: [{}]",
                    user.getKey(),
                    userGroup,
                    String.join(",", groups));
            LinuxAccountUtils.userAdd(user.getKey(), userGroup, groups);
        }
    }
}
