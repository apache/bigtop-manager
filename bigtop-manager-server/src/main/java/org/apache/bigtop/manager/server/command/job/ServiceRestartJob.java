package org.apache.bigtop.manager.server.command.job;

import org.apache.bigtop.manager.server.command.job.factory.JobContext;

public class ServiceRestartJob extends AbstractServiceJob {

    public ServiceRestartJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        super.createStopStages();

        super.createStartStages();
    }

    @Override
    public String getName() {
        return "Restart services";
    }
}
