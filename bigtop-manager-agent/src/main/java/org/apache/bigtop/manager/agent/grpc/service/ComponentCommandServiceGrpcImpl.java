package org.apache.bigtop.manager.agent.grpc.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandReply;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandRequest;
import org.apache.bigtop.manager.grpc.generated.ComponentCommandServiceGrpc;
import org.apache.bigtop.manager.stack.core.executor.StackExecutor;

@Slf4j
@GrpcService
public class ComponentCommandServiceGrpcImpl extends ComponentCommandServiceGrpc.ComponentCommandServiceImplBase {

    @Override
    public void exec(ComponentCommandRequest request, StreamObserver<ComponentCommandReply> responseObserver) {
        ComponentCommandPayload payload = JsonUtils.readFromString(request.getPayload(), ComponentCommandPayload.class);
        try {
            ShellResult shellResult = StackExecutor.execute(payload);
            ComponentCommandReply reply = ComponentCommandReply.newBuilder()
                    .setCode(shellResult.getExitCode())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
