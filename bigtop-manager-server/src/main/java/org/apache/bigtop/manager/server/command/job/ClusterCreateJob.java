package org.apache.bigtop.manager.server.command.job;

import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.server.command.job.factory.JobContext;
import org.apache.bigtop.manager.server.command.stage.CacheFileUpdateStage;
import org.apache.bigtop.manager.server.command.stage.HostCheckStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.service.ClusterService;

public class ClusterCreateJob extends AbstractJob {

    private ClusterService clusterService;

    public ClusterCreateJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterService = SpringContextHolder.getBean(ClusterService.class);
    }

    @Override
    protected void createStages() {
        StageContext stageContext = StageContext.fromCommandDTO(jobContext.getCommandDTO());
        stages.add(new HostCheckStage(stageContext));
        stages.add(new CacheFileUpdateStage(stageContext));
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        // Save cluster
        CommandDTO commandDTO = jobContext.getCommandDTO();
        ClusterDTO clusterDTO = ClusterConverter.INSTANCE.fromCommand2DTO(commandDTO.getClusterCommand());
        clusterService.save(clusterDTO);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();

        CommandDTO commandDTO = jobContext.getCommandDTO();
        ClusterPO clusterPO = clusterRepository
                .findByClusterName(commandDTO.getClusterCommand().getClusterName())
                .orElse(new ClusterPO());

        // Update cluster state to installed
        clusterPO.setState(MaintainState.INSTALLED);
        clusterRepository.save(clusterPO);

        // Link job to cluster after cluster successfully added
        JobPO jobPO = getJobPO();
        jobPO.setClusterPO(clusterPO);
        jobRepository.save(jobPO);

        for (Stage stage : getStages()) {
            StagePO stagePO = stage.getStagePO();
            stagePO.setClusterPO(clusterPO);
            stageRepository.save(stagePO);

            for (Task task : stage.getTasks()) {
                TaskPO taskPO = task.getTaskPO();
                taskPO.setClusterPO(clusterPO);
                taskRepository.save(taskPO);
            }
        }
    }

    @Override
    public String getName() {
        return "Create cluster";
    }
}
