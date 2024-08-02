package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.enums.Command;

public class ComponentCheckTask extends AbstractComponentTask {

    public ComponentCheckTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected Command getCommand() {
        return Command.CHECK;
    }

    @Override
    public String getName() {
        return "Check component " + taskContext.getComponentName() + " on " + taskContext.getHostname();
    }
}
