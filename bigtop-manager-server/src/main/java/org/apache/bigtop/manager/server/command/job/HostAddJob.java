package org.apache.bigtop.manager.server.command.job;

import org.apache.bigtop.manager.server.command.stage.CacheFileUpdateStage;
import org.apache.bigtop.manager.server.command.stage.HostCheckStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.HostCommandDTO;
import org.apache.bigtop.manager.server.service.HostService;

import java.util.List;

public class HostAddJob extends AbstractJob {

    private HostService hostService;

    public HostAddJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.hostService = SpringContextHolder.getBean(HostService.class);
    }

    @Override
    protected void createStages() {
        StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());
        stages.add(new HostCheckStage(stageContext));
        stages.add(new CacheFileUpdateStage(stageContext));
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<HostCommandDTO> hostCommands = commandDTO.getHostCommands();

        List<String> hostnames =
                hostCommands.stream().map(HostCommandDTO::getHostname).toList();
        hostService.batchSave(commandDTO.getClusterId(), hostnames);
    }

    @Override
    public String getName() {
        return "Add host";
    }
}
