package org.apache.bigtop.manager.server.command.job;

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
