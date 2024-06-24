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
package org.apache.bigtop.manager.server.ws;

import org.apache.bigtop.manager.common.message.entity.BaseMessage;
import org.apache.bigtop.manager.common.message.entity.BaseRequestMessage;
import org.apache.bigtop.manager.common.message.entity.HeartbeatMessage;
import org.apache.bigtop.manager.common.message.entity.command.CommandResponseMessage;
import org.apache.bigtop.manager.common.message.entity.pojo.HostInfo;
import org.apache.bigtop.manager.common.ws.AbstractBinaryWebSocketHandler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Endpoint for agent.
 */
@Slf4j
@Component
public class ServerWebSocketHandler extends AbstractBinaryWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public static final Map<String, HeartbeatMessage> HEARTBEAT_MESSAGE_MAP = new ConcurrentHashMap<>();

    public void sendMessage(String hostname, BaseMessage message) {
        WebSocketSession session = sessions.get(hostname);
        if (session == null) {
            log.warn("host: {}, is not connected, can't send message: {}", hostname, message);
            return;
        }

        try {
            super.sendMessage(session, message);
        } catch (Exception e) {
            log.error("Error sending message {} to host: {}", message, hostname, e);
        }
    }

    public CommandResponseMessage sendRequestMessage(String hostname, BaseRequestMessage message) {
        WebSocketSession session = sessions.get(hostname);
        if (session == null) {
            return null;
        } else {
            return (CommandResponseMessage) super.sendRequestMessage(session, message);
        }
    }

    @Override
    protected void handleBinaryMessage(@Nonnull WebSocketSession session, BinaryMessage message) throws Exception {
        BaseMessage baseMessage = deserializer.deserialize(message.getPayload().array());

        handleMessage(session, baseMessage);

        log.debug(baseMessage.toString());
    }

    private void handleMessage(WebSocketSession session, BaseMessage baseMessage) {
        log.info("Received message type: {}", baseMessage.getClass().getSimpleName());
        if (baseMessage instanceof HeartbeatMessage heartbeatMessage) {
            handleHeartbeatMessage(session, heartbeatMessage);
        } else if (baseMessage instanceof CommandResponseMessage commandResponseMessage) {
            super.handleResponseMessage(commandResponseMessage);
        } else {
            log.error("Unrecognized message type: {}", baseMessage.getClass().getSimpleName());
        }
    }

    private void handleHeartbeatMessage(WebSocketSession session, HeartbeatMessage heartbeatMessage) {
        HostInfo hostInfo = heartbeatMessage.getHostInfo();
        sessions.putIfAbsent(hostInfo.getHostname(), session);
        HEARTBEAT_MESSAGE_MAP.put(hostInfo.getHostname(), heartbeatMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @Nonnull CloseStatus status) throws Exception {
        log.error("session closed: {}, remove it!!!", session.getId());
        sessions.values().removeIf(value -> value.getId().equals(session.getId()));
        HEARTBEAT_MESSAGE_MAP.clear();
        log.info("latest ServerWebSocketSessionManager.sessions: {}", sessions);
    }

    public List<String> getConnectedHosts() {
        return new ArrayList<>(sessions.keySet());
    }
}
