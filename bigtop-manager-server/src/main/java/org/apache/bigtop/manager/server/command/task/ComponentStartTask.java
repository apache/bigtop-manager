package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.HostComponentPO;

public class ComponentStartTask extends AbstractComponentTask {

    public ComponentStartTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.START;
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        Long clusterId = taskContext.getClusterId();
        String componentName = taskContext.getComponentName();
        String hostname = taskContext.getHostname();
        HostComponentPO hostComponentPO =
                hostComponentRepository.findByComponentPOClusterPOIdAndComponentPOComponentNameAndHostPOHostname(
                        clusterId, componentName, hostname);
        hostComponentPO.setState(MaintainState.STARTED);
        hostComponentRepository.save(hostComponentPO);
    }

    @Override
    public String getName() {
        return "Start component " + taskContext.getComponentName() + " on " + taskContext.getHostname();
    }
}
