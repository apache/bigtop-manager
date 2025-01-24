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
package org.apache.bigtop.manager.ai.core.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageTypeTest {

    @Test
    public void testGetSenders() {
        List<String> senders = MessageType.getSenders();
        assertEquals(3, senders.size());
        assertTrue(senders.contains("user"));
        assertTrue(senders.contains("ai"));
        assertTrue(senders.contains("system"));
    }

    @Test
    public void testGetMessageSender() {
        assertEquals(MessageType.USER, MessageType.getMessageSender("user"));
        assertEquals(MessageType.AI, MessageType.getMessageSender("ai"));
        assertEquals(MessageType.SYSTEM, MessageType.getMessageSender("system"));
        assertNull(MessageType.getMessageSender(""));
        assertNull(MessageType.getMessageSender(null));
        assertNull(MessageType.getMessageSender("unknown"));
    }
}
