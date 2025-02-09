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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandGroupSequenceProviderTest {

    private CommandGroupSequenceProvider provider;
    private CommandReq req;

    @BeforeEach
    void setUp() {
        provider = new CommandGroupSequenceProvider();
        req = new CommandReq();
    }

    @Test
    void testGetValidationGroups_ServiceLevel_AddCommand() {
        req.setCommandLevel(CommandLevel.SERVICE);
        req.setCommand(Command.ADD);
        List<Class<?>> groups = provider.getValidationGroups(req);
        assertEquals(2, groups.size());
        assertTrue(groups.contains(CommandReq.class));
        assertTrue(groups.contains(CommandGroupSequenceProvider.ServiceInstallCommandGroup.class));
    }

    @Test
    void testGetValidationGroups_ServiceLevel_OtherCommand() {
        req.setCommandLevel(CommandLevel.SERVICE);
        req.setCommand(Command.START);
        List<Class<?>> groups = provider.getValidationGroups(req);
        assertEquals(2, groups.size());
        assertTrue(groups.contains(CommandReq.class));
        assertTrue(groups.contains(CommandGroupSequenceProvider.ServiceCommandGroup.class));
    }

    @Test
    void testGetValidationGroups_NullCommandReq() {
        List<Class<?>> groups = provider.getValidationGroups(null);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(CommandReq.class));
    }

    @Test
    void testGetValidationGroups_UnsetCommand() {
        req.setCommandLevel(CommandLevel.SERVICE);
        List<Class<?>> groups = provider.getValidationGroups(req);
        assertEquals(2, groups.size());
        assertTrue(groups.contains(CommandReq.class));
        assertTrue(groups.contains(CommandGroupSequenceProvider.ServiceCommandGroup.class));
    }
}
