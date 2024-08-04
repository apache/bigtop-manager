package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.server.command.task.HostCheckTask;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.command.task.TaskContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

public class HostCheckStage extends AbstractStage {

    private ClusterRepository clusterRepository;

    public HostCheckStage(StageContext stageContext) {
        super(stageContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterRepository = SpringContextHolder.getBean(ClusterRepository.class);
    }

    @Override
    protected void beforeCreateTasks() {
        if (stageContext.getClusterId() != null) {
            ClusterPO clusterPO = clusterRepository.getReferenceById(stageContext.getClusterId());

            stageContext.setStackName(clusterPO.getStackPO().getStackName());
            stageContext.setStackVersion(clusterPO.getStackPO().getStackVersion());
        }
    }

    @Override
    protected Task createTask(String hostname) {
        TaskContext taskContext = new TaskContext();
        taskContext.setHostname(hostname);
        taskContext.setClusterId(stageContext.getClusterId());
        taskContext.setClusterName(stageContext.getClusterName());
        taskContext.setStackName(stageContext.getStackName());
        taskContext.setStackVersion(stageContext.getStackVersion());
        taskContext.setServiceName("cluster");
        taskContext.setServiceUser("root");
        taskContext.setServiceGroup("root");
        taskContext.setComponentName("agent");
        taskContext.setCommand(Command.CUSTOM);
        taskContext.setCustomCommand("check_host");

        return new HostCheckTask(taskContext);
    }

    @Override
    public String getName() {
        return "Check hosts";
    }
}
