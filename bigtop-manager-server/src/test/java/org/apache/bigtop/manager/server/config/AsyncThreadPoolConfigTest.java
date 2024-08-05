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

import org.apache.bigtop.manager.server.holder.SessionUserHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AsyncThreadPoolConfigTest {

    private AsyncThreadPoolConfig asyncThreadPoolConfig;

    @BeforeEach
    void setUp() {
        asyncThreadPoolConfig = new AsyncThreadPoolConfig();
    }

    @AfterEach
    void tearDown() {
        asyncThreadPoolConfig = null;
        SessionUserHolder.setUserId(null);
    }

    @Test
    void getAsyncExecutor_ReturnsConfiguredExecutor() {
        Executor executor = asyncThreadPoolConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(5, taskExecutor.getCorePoolSize());
        assertEquals(10, taskExecutor.getMaxPoolSize());
        assertEquals(300, taskExecutor.getQueueCapacity());
        assertInstanceOf(
                ThreadPoolExecutor.CallerRunsPolicy.class,
                taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler());
    }

    @Test
    void getAsyncUncaughtExceptionHandler_ReturnsSimpleAsyncUncaughtExceptionHandler() {
        assertInstanceOf(
                SimpleAsyncUncaughtExceptionHandler.class, asyncThreadPoolConfig.getAsyncUncaughtExceptionHandler());
    }

    @Test
    void contextCopyingDecorator_CopiesContextAndClearsAfterExecutionInSubThread() throws InterruptedException {
        SessionUserHolder.setUserId(1L);
        Runnable task = mock(Runnable.class);
        AsyncThreadPoolConfig.ContextCopyingDecorator decorator = new AsyncThreadPoolConfig.ContextCopyingDecorator();

        Runnable decoratedTask = decorator.decorate(() -> {
            assertEquals(1L, SessionUserHolder.getUserId());
            task.run();
        });

        Thread taskThread = new Thread(() -> {
            decoratedTask.run();
            assertNull(SessionUserHolder.getUserId());
        });

        taskThread.start();
        taskThread.join();

        verify(task, times(1)).run();
        assertEquals(1L, SessionUserHolder.getUserId());
    }
}
