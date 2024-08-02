package org.apache.bigtop.manager.server.command.task;

import lombok.Data;
import org.apache.bigtop.manager.common.enums.Command;

import java.util.Map;

@Data
public class TaskContext {

    private Long clusterId;

    private String clusterName;

    private String hostname;

    private String stackName;

    private String stackVersion;

    private String serviceName;

    private String serviceUser;

    private String serviceGroup;

    private String componentName;

    private Command command;

    private String customCommand;

    private String root;

    // Extra properties for specific tasks
    protected Map<String, Object> properties;
}
