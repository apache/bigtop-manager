package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.command.task.ComponentStopTask;
import org.apache.bigtop.manager.server.command.task.Task;

public class ComponentStopStage extends AbstractComponentStage {

    public ComponentStopStage(StageContext stageContext) {
        super(stageContext);
    }


    @Override
    protected Task createTask(String hostname) {
        return new ComponentStopTask(createTaskContext(hostname));
    }

    @Override
    public String getName() {
        return "Stop " + stageContext.getComponentDTO().getDisplayName();
    }
}
