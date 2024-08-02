package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.HostComponentPO;

public class ComponentStopTask extends AbstractComponentTask {

    public ComponentStopTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.STOP;
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
        hostComponentPO.setState(MaintainState.STOPPED);
        hostComponentRepository.save(hostComponentPO);
    }

    @Override
    public String getName() {
        return "Stop component " + taskContext.getComponentName() + " on " + taskContext.getHostname();
    }
}
