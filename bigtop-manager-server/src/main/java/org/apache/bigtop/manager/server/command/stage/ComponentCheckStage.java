package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.server.command.task.ComponentCheckTask;
import org.apache.bigtop.manager.server.command.task.Task;

public class ComponentCheckStage extends AbstractComponentStage {

    public ComponentCheckStage(StageContext stageContext) {
        super(stageContext);
    }


    @Override
    protected Task createTask(String hostname) {
        return new ComponentCheckTask(createTaskContext(hostname));
    }

    @Override
    public String getName() {
        return "Check " + stageContext.getComponentDTO().getDisplayName();
    }
}
