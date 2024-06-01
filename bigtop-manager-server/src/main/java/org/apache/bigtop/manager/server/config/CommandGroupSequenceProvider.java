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
package org.apache.bigtop.manager.server.config;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.model.req.CommandReq;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

public class CommandGroupSequenceProvider implements DefaultGroupSequenceProvider<CommandReq> {

    @Override
    public List<Class<?>> getValidationGroups(CommandReq bean) {
        List<Class<?>> defaultGroupSequence = new ArrayList<>();
        defaultGroupSequence.add(CommandReq.class); // 这一步不能省,否则Default分组都不会执行了，会抛错的

        if (bean != null) { // 这块判空请务必要做
            CommandLevel commandLevel = bean.getCommandLevel();

            switch (commandLevel) {
                case SERVICE:
                    if (bean.getCommand() == Command.INSTALL) {
                        defaultGroupSequence.add(ServiceInstallCommandGroup.class);
                    } else {
                        defaultGroupSequence.add(ServiceCommandGroup.class);
                    }
                    break;
                case HOST:
                    defaultGroupSequence.add(HostCommandGroup.class);
                    break;
                case COMPONENT:
                    defaultGroupSequence.add(ComponentCommandGroup.class);
                    break;
                case CLUSTER:
                    defaultGroupSequence.add(ClusterCommandGroup.class);
                    break;
            }

        }
        return defaultGroupSequence;
    }

    public interface ServiceCommandGroup {
    }

    public interface HostCommandGroup {
    }

    public interface ComponentCommandGroup {
    }

    public interface ServiceInstallCommandGroup {
    }

    public interface ClusterCommandGroup {
    }
}
