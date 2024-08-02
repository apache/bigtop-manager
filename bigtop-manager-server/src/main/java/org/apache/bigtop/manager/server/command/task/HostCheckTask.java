package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.HostCheckPayload;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;

public class HostCheckTask extends AbstractTask {

    public HostCheckTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.CUSTOM;
    }

    @Override
    protected String getCustomCommand() {
        return "check_host";
    }

    @Override
    protected CommandRequest getCommandRequest() {
        String hostname = taskContext.getHostname();
        HostCheckPayload messagePayload = new HostCheckPayload();
        messagePayload.setHostname(hostname);

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.HOST_CHECK);
        builder.setHostname(hostname);
        builder.setPayload(JsonUtils.writeAsString(messagePayload));

        return builder.build();
    }

    @Override
    public String getName() {
        return "Check host " + taskContext.getHostname();
    }
}
