package org.apache.bigtop.manager.server.command.job;

import org.apache.bigtop.manager.server.command.job.factory.JobContext;

public class ServiceConfigureJob extends AbstractServiceJob {

    public ServiceConfigureJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        super.createCacheStage();
    }

    @Override
    public String getName() {
        return "Configure services";
    }
}
