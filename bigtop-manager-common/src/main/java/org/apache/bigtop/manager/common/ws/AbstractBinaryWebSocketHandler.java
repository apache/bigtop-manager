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
package org.apache.bigtop.manager.common.ws;

import org.apache.bigtop.manager.common.message.entity.BaseMessage;
import org.apache.bigtop.manager.common.message.entity.BaseRequestMessage;
import org.apache.bigtop.manager.common.message.entity.BaseResponseMessage;
import org.apache.bigtop.manager.common.message.serializer.MessageDeserializer;
import org.apache.bigtop.manager.common.message.serializer.MessageSerializer;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AbstractBinaryWebSocketHandler extends BinaryWebSocketHandler {

    @Resource
    protected MessageSerializer serializer;

    @Resource
    protected MessageDeserializer deserializer;

    private final ConcurrentHashMap<String, BaseRequestMessage> requests = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, BaseResponseMessage> responses = new ConcurrentHashMap<>();

    protected void sendMessage(WebSocketSession session, BaseMessage message) {
        try {
            sendMessageWithRetry(session, message);
        } catch (Exception e) {
            log.error("Error sending message: {}", message, e);
        }
    }

    protected BaseResponseMessage sendRequestMessage(WebSocketSession session, BaseRequestMessage request) {
        requests.put(request.getMessageId(), request);

        try {
            sendMessageWithRetry(session, request);
        } catch (Exception e) {
            log.error("Error sending message: {}", request, e);
        }

        for (int i = 0; i <= 300; i++) {
            BaseResponseMessage response = responses.get(request.getMessageId());
            if (response == null) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    log.error("Error waiting for message response, messageId: {}", request.getMessageId(), e);
                }
            } else {
                requests.remove(request.getMessageId());
                responses.remove(request.getMessageId());
                return response;
            }
        }

        requests.remove(request.getMessageId());
        return null;
    }

    protected void handleResponseMessage(BaseResponseMessage response) {
        if (requests.containsKey(response.getMessageId())) {
            responses.put(response.getMessageId(), response);
        } else {
            log.warn("Message is timed out or unexpected, drop it: {}", response);
        }
    }

    private void sendMessageWithRetry(WebSocketSession session, BaseMessage message) throws Exception {
        int retryCount = 3;
        int retryInterval = 5000;
        for (int i = 0; i < retryCount; i++) {
            try {
                session.sendMessage(new BinaryMessage(serializer.serialize(message)));
                break;
            } catch (Exception e) {
                log.error("Error sending message: {}, retry count: {}", message, i + 1, e);
                if (i + 1 == retryCount) {
                    throw e;
                } else {
                    Thread.sleep(retryInterval);
                }
            }
        }
    }
}
