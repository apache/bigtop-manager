package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractStage implements Stage {

    protected StageRepository stageRepository;

    protected StageContext stageContext;
    protected List<Task> tasks;

    /**
     * Do not use this directly, please use {@link #getStagePO()} to make sure it's initialized.
     */
    private StagePO stagePO;

    public AbstractStage(StageContext stageContext) {
        this.stageContext = stageContext;
        this.tasks = new ArrayList<>();

        injectBeans();

        beforeCreateTasks();

        for (String hostname : stageContext.getHostnames()) {
            tasks.add(createTask(hostname));
        }
    }

    protected void injectBeans() {
        this.stageRepository = SpringContextHolder.getBean(StageRepository.class);
    }

    protected abstract void beforeCreateTasks();

    protected abstract Task createTask(String hostname);

    protected String getServiceName() {
        return "cluster";
    }

    protected String getComponentName() {
        return "agent";
    }

    @Override
    public void beforeRun() {
        StagePO stagePO = getStagePO();
        stagePO.setState(JobState.PROCESSING);
        stageRepository.save(stagePO);
    }

    @Override
    public Boolean run() {
        beforeRun();

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (Task task : tasks) {
            futures.add(CompletableFuture.supplyAsync(task::run));
        }

        List<Boolean> taskResults = futures.stream()
                .map((future) -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();

        boolean allTaskSuccess = taskResults.stream().allMatch(Boolean::booleanValue);
        if (allTaskSuccess) {
            onSuccess();
        } else {
            onFailure();
        }

        return allTaskSuccess;
    }

    @Override
    public void onSuccess() {
        StagePO stagePO = getStagePO();
        stagePO.setState(JobState.SUCCESSFUL);
        stageRepository.save(stagePO);
    }

    @Override
    public void onFailure() {
        StagePO stagePO = getStagePO();
        stagePO.setState(JobState.FAILED);
        stageRepository.save(stagePO);
    }

    @Override
    public StageContext getStageContext() {
        return stageContext;
    }

    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public StagePO getStagePO() {
        if (stagePO == null) {
            stagePO = new StagePO();
            stagePO.setName(getName());
            stagePO.setServiceName(getServiceName());
            stagePO.setComponentName(getComponentName());
            stagePO.setContext(JsonUtils.writeAsString(stageContext));
        }

        return stagePO;
    }
}
