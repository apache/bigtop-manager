package org.apache.bigtop.manager.server.command.job;

import org.apache.bigtop.manager.server.command.job.factory.JobContext;

public class ServiceStopJob extends AbstractServiceJob {

    public ServiceStopJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        super.createStopStages();
    }

    @Override
    public String getName() {
        return "Stop services";
    }
}
