package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.grpc.generated.CommandReply;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandServiceGrpc;
import org.apache.bigtop.manager.grpc.utils.ProtobufUtil;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

public abstract class AbstractTask implements Task {

    protected TaskRepository taskRepository;

    protected TaskContext taskContext;

    protected CommandRequest commandRequest;
    protected TaskPO taskPO;

    public AbstractTask(TaskContext taskContext) {
        this.taskContext = taskContext;

        injectBeans();
    }

    protected void injectBeans() {
        this.taskRepository = SpringContextHolder.getBean(TaskRepository.class);
    }

    protected abstract Command getCommand();

    protected String getCustomCommand() {
        return null;
    }

    protected abstract CommandRequest getCommandRequest();

    @Override
    public void beforeRun() {
        taskPO.setState(JobState.PROCESSING);
        taskRepository.save(taskPO);
    }

    @Override
    public Boolean run() {
        beforeRun();

        CommandRequest.Builder builder = CommandRequest.newBuilder(getCommandRequest());
        builder.setTaskId(taskPO.getId());
        commandRequest = builder.build();

        CommandServiceGrpc.CommandServiceBlockingStub stub = GrpcClient.getBlockingStub(
                taskContext.getHostname(), CommandServiceGrpc.CommandServiceBlockingStub.class);
        CommandReply reply = stub.exec(commandRequest);

        boolean taskSuccess = reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;

        if (taskSuccess) {
            onSuccess();
        } else {
            onFailure();
        }

        return taskSuccess;
    }

    @Override
    public void onSuccess() {
        taskPO.setContent(ProtobufUtil.toJson(commandRequest));
        taskPO.setState(JobState.SUCCESSFUL);
        taskRepository.save(taskPO);
    }

    @Override
    public void onFailure() {
        taskPO.setContent(ProtobufUtil.toJson(commandRequest));
        taskPO.setState(JobState.FAILED);
        taskRepository.save(taskPO);
    }

    @Override
    public TaskContext getTaskContext() {
        return taskContext;
    }

    @Override
    public TaskPO toTaskPO() {
        if (taskPO == null) {
            taskPO = new TaskPO();
            taskPO.setName(getName());
            taskPO.setContext(JsonUtils.writeAsString(taskContext));
            taskPO.setStackName(taskContext.getStackName());
            taskPO.setStackVersion(taskContext.getStackVersion());
            taskPO.setHostname(taskContext.getHostname());
            taskPO.setServiceName(taskContext.getServiceName());
            taskPO.setServiceUser(taskContext.getServiceUser());
            taskPO.setServiceGroup(taskContext.getServiceGroup());
            taskPO.setComponentName(taskContext.getComponentName());
            taskPO.setCommand(getCommand());
            taskPO.setCustomCommand(getCustomCommand());
        }

        return taskPO;
    }
}
