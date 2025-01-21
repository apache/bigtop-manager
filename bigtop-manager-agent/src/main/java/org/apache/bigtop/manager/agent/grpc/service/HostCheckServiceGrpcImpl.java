package org.apache.bigtop.manager.agent.grpc.service;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.common.utils.os.TimeSyncDetection;
import org.apache.bigtop.manager.grpc.generated.HostCheckReply;
import org.apache.bigtop.manager.grpc.generated.HostCheckRequest;
import org.apache.bigtop.manager.grpc.generated.HostCheckServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@GrpcService
public class HostCheckServiceGrpcImpl extends HostCheckServiceGrpc.HostCheckServiceImplBase {

    @Override
    public void check(HostCheckRequest request, StreamObserver<HostCheckReply> responseObserver) {
        try {
            if (Environments.isDevMode()) {
                HostCheckReply reply = HostCheckReply.newBuilder()
                        .setCode(MessageConstants.SUCCESS_CODE)
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                return;
            }

            ShellResult shellResult = runChecks(List.of(this::checkTimeSync));
            HostCheckReply reply = HostCheckReply.newBuilder()
                    .setCode(shellResult.getExitCode())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error when running host check", e);
            responseObserver.onError(e);
        }
    }

    private ShellResult runChecks(List<Supplier<ShellResult>> suppliers) {
        ShellResult shellResult = ShellResult.success();
        for (Supplier<ShellResult> supplier : suppliers) {
            shellResult = supplier.get();
            if (shellResult.getExitCode() != MessageConstants.SUCCESS_CODE) {
                return shellResult;
            }
        }

        return shellResult;
    }

    private ShellResult checkTimeSync() {
        return TimeSyncDetection.checkTimeSync();
    }
}
