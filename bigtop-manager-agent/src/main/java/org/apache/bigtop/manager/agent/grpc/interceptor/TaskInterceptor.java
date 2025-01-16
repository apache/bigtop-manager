package org.apache.bigtop.manager.agent.grpc.interceptor;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.agent.cache.Caches;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.grpc.generated.TaskLogRequest;
import org.slf4j.MDC;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;

@Slf4j
public class TaskInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(next.startCall(call, headers)) {
            @Override
            public void onMessage(ReqT message) {
                super.onMessage(message);

                if (isTaskRequest(message)) {
                    try {
                        Method method = message.getClass().getDeclaredMethod("getTaskId");
                        method.setAccessible(true);
                        Long taskId = (Long) method.invoke(message);
                        method.setAccessible(false);
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

                Caches.RUNNING_TASK = null;
                MDC.clear();
            }

            @Override
            public void onComplete() {
                super.onComplete();

                Caches.RUNNING_TASK = null;
                MDC.clear();
            }

            @Override
            public void onReady() {
                super.onReady();
            }
        };
    }

    private void truncateLogFile(Long taskId) {
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

    private Boolean isTaskRequest(Object obj) {
        if (obj == null) {
            return false;
        }

        // Every task will have taskId, but TaskLogRequest have it also, so we need to exclude it
        Class<?> clazz = obj.getClass();
        if (clazz.isInstance(TaskLogRequest.class)) {
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
