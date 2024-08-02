package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.command.task.ComponentInstallTask;
import org.apache.bigtop.manager.server.command.task.Task;

public class ComponentInstallStage extends AbstractComponentStage {

    public ComponentInstallStage(StageContext stageContext) {
        super(stageContext);
    }


    @Override
    protected Task createTask(String hostname) {
        return new ComponentInstallTask(createTaskContext(hostname));
    }

    @Override
    public String getName() {
        return "Install " + stageContext.getComponentDTO().getDisplayName();
    }
}
