package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.command.task.Task;

import java.util.List;

public interface Stage {

    String getName();

    void beforeRun();

    Boolean run();

    void onSuccess();

    void onFailure();

    StageContext getStageContext();

    List<Task> getTasks();

    StagePO toStagePO();
}
