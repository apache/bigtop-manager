package org.apache.bigtop.manager.server.command.job;

public class ServiceStartJob extends AbstractServiceJob {

    public ServiceStartJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        super.createStartStages();
    }

    @Override
    public String getName() {
        return "Start services";
    }
}
