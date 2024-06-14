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
package org.apache.bigtop.manager.agent.ws;

import org.apache.bigtop.manager.agent.holder.SpringContextHolder;
import org.apache.bigtop.manager.agent.scheduler.CommandScheduler;
import org.apache.bigtop.manager.common.config.ApplicationConfig;
import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.message.entity.BaseMessage;
import org.apache.bigtop.manager.common.message.entity.HeartbeatMessage;
import org.apache.bigtop.manager.common.message.entity.command.CommandRequestMessage;
import org.apache.bigtop.manager.common.message.entity.pojo.HostInfo;
import org.apache.bigtop.manager.common.message.serializer.MessageDeserializer;
import org.apache.bigtop.manager.common.utils.os.OSDetection;
import org.apache.bigtop.manager.common.ws.AbstractBinaryWebSocketHandler;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.bigtop.manager.common.constants.Constants.WS_BINARY_MESSAGE_SIZE_LIMIT;

@Slf4j
@Component
public class AgentWebSocketHandler extends AbstractBinaryWebSocketHandler
        implements ApplicationListener<ApplicationStartedEvent> {

    @Resource
    private ApplicationConfig applicationConfig;

    @Resource
    private MessageDeserializer deserializer;

    @Resource
    private CommandScheduler commandScheduler;

    @Getter
    @Setter
    private WebSocketSession session;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private HostInfo hostInfo;

    public void sendMessage(BaseMessage message) {
        log.info("send message to server: {}", message);
        if (session == null) {
            log.warn("session is null, can't send message to server");
            return;
        }

        try {
            super.sendMessage(session, message);
        } catch (Exception e) {
            log.error("Error sending message to server: {}", message, e);
        }
    }

    @Override
    protected void handleBinaryMessage(@Nonnull WebSocketSession session, BinaryMessage message) {
        BaseMessage baseMessage = deserializer.deserialize(message.getPayload().array());

        handleMessage(session, baseMessage);
    }

    private void handleMessage(WebSocketSession session, BaseMessage baseMessage) {
        log.info(
                "Received message type: {}, session: {}", baseMessage.getClass().getSimpleName(), session);

        if (baseMessage instanceof CommandRequestMessage commandRequestMessage) {
            commandScheduler.submit(commandRequestMessage);
        } else {
            log.error("Unrecognized message type: {}", baseMessage.getClass().getSimpleName());
        }
    }

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
        this.setSession(session);
        executor.scheduleAtFixedRate(
                () -> {
                    try {
                        HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
                        heartbeatMessage.setHostInfo(hostInfo);
                        WebSocketSession innerSession =
                                SpringContextHolder.getAgentWebSocket().getSession();
                        if (null == innerSession || !innerSession.isOpen()) {
                            connectToServer();
                        }
                        innerSession = SpringContextHolder.getAgentWebSocket().getSession();
                        if (null != innerSession && innerSession.isOpen()) {
                            super.sendMessage(innerSession, heartbeatMessage);
                        }
                    } catch (Exception e) {
                        log.error(MessageFormat.format("Error sending heartbeat to server: {0}", e.getMessage()));
                    }
                },
                3,
                10,
                TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) {
        log.info("WebSocket connection closed unexpectedly, reconnecting...");

        this.session = null;
        connectToServer();
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationStartedEvent event) {
        executor.scheduleAtFixedRate(this::readHostInfo, 0, 30, TimeUnit.SECONDS);
        log.info("Bootstrap successfully, connecting to server websocket endpoint...");
        executor.scheduleAtFixedRate(this::connectToServer, 0, 30, TimeUnit.SECONDS);
    }

    private void readHostInfo() {
        hostInfo = new HostInfo();

        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostInfo.setHostname(addr.getHostName());
            hostInfo.setIpv4(addr.getHostAddress());

            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            hostInfo.setOs(OSDetection.getOS());
            hostInfo.setVersion(OSDetection.getVersion());
            hostInfo.setArch(OSDetection.getArch());
            hostInfo.setAvailableProcessors(osmxb.getAvailableProcessors());
            hostInfo.setProcessCpuTime(osmxb.getProcessCpuTime());
            hostInfo.setTotalMemorySize(osmxb.getTotalMemorySize());
            hostInfo.setFreeMemorySize(osmxb.getFreeMemorySize());
            hostInfo.setTotalSwapSpaceSize(osmxb.getTotalSwapSpaceSize());
            hostInfo.setFreeSwapSpaceSize(osmxb.getFreeSwapSpaceSize());
            hostInfo.setCommittedVirtualMemorySize(osmxb.getCommittedVirtualMemorySize());

            hostInfo.setCpuLoad(new BigDecimal(String.valueOf(osmxb.getCpuLoad())));
            hostInfo.setProcessCpuLoad(new BigDecimal(String.valueOf(osmxb.getProcessCpuLoad())));
            hostInfo.setSystemLoadAverage(new BigDecimal(String.valueOf(osmxb.getSystemLoadAverage())));

            hostInfo.setFreeDisk(OSDetection.freeDisk());
            hostInfo.setTotalDisk(OSDetection.totalDisk());
        } catch (Exception e) {
            log.error("Error getting host info", e);
            throw new RuntimeException(e);
        }
    }

    private void connectToServer() {
        String host = applicationConfig.getServer().getHost();
        Integer port = applicationConfig.getServer().getPort();
        String uri = MessageFormat.format("ws://{0}:{1,number,#}/ws/server", host, port);
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        int retryTime = 0;
        while (true) {
            try {
                if (retryTime >= 3) break;
                AgentWebSocketHandler agentWebSocket = SpringContextHolder.getAgentWebSocket();
                WebSocketSession contextSession = agentWebSocket.getSession();
                if (null == contextSession || !contextSession.isOpen()) {
                    WebSocketSession webSocketSession =
                            webSocketClient.execute(this, uri).get(5, TimeUnit.SECONDS);
                    webSocketSession.setBinaryMessageSizeLimit(WS_BINARY_MESSAGE_SIZE_LIMIT);
                    agentWebSocket.setSession(webSocketSession);
                    log.info(MessageFormat.format("Connect to server: {0} successfully", uri));
                }
                ++retryTime;
                break;
            } catch (Exception e) {
                log.error(MessageFormat.format(
                        "Error connecting to server: {0}, retry time: {1}", e.getMessage(), ++retryTime));
                // retry after 5 seconds
                try {
                    TimeUnit.MILLISECONDS.sleep(Constants.REGISTRY_SESSION_TIMEOUT);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
