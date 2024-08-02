package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.HostComponentPO;

public class ComponentInstallTask extends AbstractComponentTask {

    public ComponentInstallTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.INSTALL;
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
        hostComponentPO.setState(MaintainState.INSTALLED);
        hostComponentRepository.save(hostComponentPO);
    }

    @Override
    public String getName() {
        return "Install component " + taskContext.getComponentName() + " on " + taskContext.getHostname();
    }
}
