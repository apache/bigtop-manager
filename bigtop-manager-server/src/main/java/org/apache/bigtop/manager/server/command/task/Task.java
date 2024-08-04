package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.dao.po.TaskPO;

public interface Task {

    String getName();

    void beforeRun();

    Boolean run();

    void onSuccess();

    void onFailure();

    TaskContext getTaskContext();

    void loadTaskPO(TaskPO taskPO);

    TaskPO getTaskPO();
}
