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
package org.apache.bigtop.manager.server.grpc;

import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractAsyncStub;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractFutureStub;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcClient {

    private static final Map<String, ManagedChannel> CHANNELS = new ConcurrentHashMap<>();

    // The key of outer map is hostname, inner map is stub class name
    private static final Map<String, Map<String, AbstractBlockingStub<?>>> BLOCKING_STUBS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, AbstractAsyncStub<?>>> ASYNC_STUBS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, AbstractFutureStub<?>>> FUTURE_STUBS = new ConcurrentHashMap<>();

    public static Boolean isChannelAlive(String host) {
        ManagedChannel channel = CHANNELS.get(host);
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractBlockingStub<T>> T getBlockingStub(String host, Integer grpcPort, Class<T> clazz) {
        Map<String, AbstractBlockingStub<?>> innerMap =
                BLOCKING_STUBS.computeIfAbsent(host, k -> new ConcurrentHashMap<>());
        return (T) innerMap.computeIfAbsent(clazz.getName(), k -> {
            T instance = T.newStub(getFactory(clazz), getChannel(host, grpcPort));
            log.info("Instance: {} created.", k);
            return instance;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractAsyncStub<T>> T getAsyncStub(String host, Integer grpcPort, Class<T> clazz) {
        Map<String, AbstractAsyncStub<?>> innerMap = ASYNC_STUBS.computeIfAbsent(host, k -> new ConcurrentHashMap<>());
        return (T) innerMap.computeIfAbsent(clazz.getName(), k -> {
            T instance = T.newStub(getFactory(clazz), getChannel(host, grpcPort));
            log.info("Instance: {} created.", k);
            return instance;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractFutureStub<T>> T getFutureStub(String host, Integer grpcPort, Class<T> clazz) {
        Map<String, AbstractFutureStub<?>> innerMap =
                FUTURE_STUBS.computeIfAbsent(host, k -> new ConcurrentHashMap<>());
        return (T) innerMap.computeIfAbsent(clazz.getName(), k -> {
            T instance = T.newStub(getFactory(clazz), getChannel(host, grpcPort));
            log.info("Instance: {} created.", k);
            return instance;
        });
    }

    private static ManagedChannel createChannel(String host, Integer port) {
        String ipv4;
        try {
            InetAddress address = InetAddress.getByName(host);
            ipv4 = address.getHostAddress();
        } catch (Exception e) {
            log.error("Unable to resolve host: {}", host);
            throw new ApiException(ApiExceptionEnum.HOST_UNABLE_TO_RESOLVE, host);
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress(ipv4, port)
                .usePlaintext()
                .keepAliveTime(60, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();

        ConnectivityState state = channel.getState(true);
        while (state == ConnectivityState.IDLE || state == ConnectivityState.CONNECTING) {
            try {
                Thread.sleep(1000);
                state = channel.getState(true);
            } catch (Exception e) {
                log.warn("Error ignored when creating channel", e);
            }
        }

        if (state != ConnectivityState.READY) {
            channel.shutdown();
            log.error("Unable to connect to host: {}", host);
            throw new ApiException(ApiExceptionEnum.HOST_UNABLE_TO_CONNECT, host);
        } else {
            CHANNELS.put(host, channel);
            return channel;
        }
    }

    private static ManagedChannel getChannel(String host, Integer grpcPort) {
        if (isChannelAlive(host)) {
            return CHANNELS.get(host);
        } else {
            return createChannel(host, grpcPort);
        }
    }

    public static void removeChannel(String host) {
        ManagedChannel channel = CHANNELS.remove(host);
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Channel shutdown interrupted", e);
            }

            BLOCKING_STUBS.remove(host);
            ASYNC_STUBS.remove(host);
            FUTURE_STUBS.remove(host);
            log.info("Channel to host: {} removed.", host);
        }
    }

    private static <T extends AbstractStub<T>> AbstractStub.StubFactory<T> getFactory(Class<T> clazz) {
        return (channel, callOptions) -> {
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor(Channel.class, CallOptions.class);
                constructor.setAccessible(true);
                return constructor.newInstance(channel, callOptions);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
