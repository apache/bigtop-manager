package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.server.command.task.ComponentStartTask;
import org.apache.bigtop.manager.server.command.task.Task;

public class ComponentStartStage extends AbstractComponentStage {

    public ComponentStartStage(StageContext stageContext) {
        super(stageContext);
    }


    @Override
    protected Task createTask(String hostname) {
        return new ComponentStartTask(createTaskContext(hostname));
    }

    @Override
    public String getName() {
        return "Start " + stageContext.getComponentDTO().getDisplayName();
    }
}
