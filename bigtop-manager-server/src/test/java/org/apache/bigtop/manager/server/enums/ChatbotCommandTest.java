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
package org.apache.bigtop.manager.server.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatbotCommandTest {

    @Test
    public void testGetAllCommands() {
        // Test getting all commands
        List<String> allCommands = ChatbotCommand.getAllCommands();
        assertEquals(3, allCommands.size());
        assertTrue(allCommands.contains("info"));
        assertTrue(allCommands.contains("search"));
        assertTrue(allCommands.contains("help"));
    }

    @Test
    public void testGetCommandWithValidInput() {
        // Test getting valid command
        assertEquals(ChatbotCommand.INFO, ChatbotCommand.getCommand("info"));
        assertEquals(ChatbotCommand.SEARCH, ChatbotCommand.getCommand("search"));
        assertEquals(ChatbotCommand.HELP, ChatbotCommand.getCommand("help"));
    }

    @Test
    public void testGetCommandWithInvalidInput() {
        // Test getting invalid command
        assertNull(ChatbotCommand.getCommand("unknown"));
        assertNull(ChatbotCommand.getCommand(""));
        assertNull(ChatbotCommand.getCommand(null));
    }

    @Test
    public void testGetCommandFromMessageWithValidInput() {
        // Test getting valid command from message
        assertEquals(ChatbotCommand.INFO, ChatbotCommand.getCommandFromMessage("/info"));
        assertEquals(ChatbotCommand.SEARCH, ChatbotCommand.getCommandFromMessage("/search some text"));
        assertEquals(ChatbotCommand.HELP, ChatbotCommand.getCommandFromMessage("/help"));
    }

    @Test
    public void testGetCommandFromMessageWithInvalidInput() {
        // Test getting invalid command from message
        assertNull(ChatbotCommand.getCommandFromMessage("info"));
        assertNull(ChatbotCommand.getCommandFromMessage("/unknown"));
        assertNull(ChatbotCommand.getCommandFromMessage(""));
        assertThrows(NullPointerException.class, () -> ChatbotCommand.getCommandFromMessage(null));
    }

    @Test
    public void testGetCommandFromMessageWithNoSlash() {
        // Test message with no slash
        assertNull(ChatbotCommand.getCommandFromMessage("info"));
    }

    @Test
    public void testGetCommandFromMessageWithOnlySlash() {
        // Test message with only slash
        assertNull(ChatbotCommand.getCommandFromMessage("/"));
    }

    @Test
    public void testGetCommandFromMessageWithMultipleSlashes() {
        // Test message with multiple slashes
        assertNull(ChatbotCommand.getCommandFromMessage("//info"));
    }

    @Test
    public void testGetCommandFromMessageWithTrailingSpace() {
        // Test message with trailing space after command
        assertEquals(ChatbotCommand.INFO, ChatbotCommand.getCommandFromMessage("/info "));
    }

    @Test
    public void testGetCommandFromMessageWithCaseInsensitive() {
        // Test message with case insensitive command
        assertNull(ChatbotCommand.getCommandFromMessage("/INFO"));
        assertNull(ChatbotCommand.getCommandFromMessage("/SEARCH"));
        assertNull(ChatbotCommand.getCommandFromMessage("/HELP"));
    }
}
