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
package org.apache.bigtop.manager.agent.grpc.interceptor;

import org.apache.bigtop.manager.agent.cache.Caches;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.generated.TaskLogRequest;

import org.slf4j.MDC;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;

@Slf4j
public class TaskInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(next.startCall(call, headers)) {

            private boolean taskRequest = false;

            @Override
            public void onMessage(ReqT message) {
                super.onMessage(message);
                taskRequest = isTaskRequest(message);

                if (taskRequest) {
                    try {
                        Method method = message.getClass().getDeclaredMethod("getTaskId");
                        Long taskId = (Long) method.invoke(message);
                        truncateLogFile(taskId);
                        MDC.put("taskId", String.valueOf(taskId));
                        Caches.RUNNING_TASK = taskId;
                    } catch (Exception e) {
                        log.error("Error when getting taskId from message", e);
                    }
                }
            }

            @Override
            public void onHalfClose() {
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                super.onCancel();

                if (taskRequest) {
                    Caches.RUNNING_TASK = null;
                    MDC.clear();
                }
            }

            @Override
            public void onComplete() {
                super.onComplete();

                if (taskRequest) {
                    Caches.RUNNING_TASK = null;
                    MDC.clear();
                }
            }

            @Override
            public void onReady() {
                super.onReady();
            }
        };
    }

    protected void truncateLogFile(Long taskId) {
        String filePath = ProjectPathUtils.getLogFilePath(taskId);
        File file = new File(filePath);
        if (file.exists()) {
            try (RandomAccessFile rf = new RandomAccessFile(file, "rw")) {
                rf.setLength(0);
            } catch (IOException e) {
                log.warn("Error when truncate file: {}", filePath, e);
            }
        }
    }

    protected Boolean isTaskRequest(Object obj) {
        if (obj == null) {
            return false;
        }

        // Every task will have taskId, but TaskLogRequest have it also, so we need to exclude it
        Class<?> clazz = obj.getClass();
        if (TaskLogRequest.class.isAssignableFrom(clazz)) {
            return false;
        }

        try {
            clazz.getDeclaredMethod("getTaskId");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
