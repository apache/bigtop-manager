package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.server.command.task.TaskContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractComponentStage extends AbstractStage {

    private ClusterRepository clusterRepository;

    private ClusterPO clusterPO;

    public AbstractComponentStage(StageContext stageContext) {
        super(stageContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterRepository = SpringContextHolder.getBean(ClusterRepository.class);
    }

    @Override
    protected void beforeCreateTasks() {
        this.clusterPO = clusterRepository.getReferenceById(stageContext.getClusterId());
    }

    @Override
    protected String getServiceName() {
        return stageContext.getServiceDTO().getServiceName();
    }

    @Override
    protected String getComponentName() {
        return stageContext.getComponentDTO().getComponentName();
    }

    protected TaskContext createTaskContext(String hostname) {
        ServiceDTO serviceDTO = stageContext.getServiceDTO();
        ComponentDTO componentDTO = stageContext.getComponentDTO();

        TaskContext taskContext = new TaskContext();
        taskContext.setHostname(hostname);
        taskContext.setClusterId(clusterPO.getId());
        taskContext.setClusterName(clusterPO.getClusterName());
        taskContext.setServiceName(serviceDTO.getServiceName());
        taskContext.setStackName(stageContext.getStackName());
        taskContext.setStackVersion(stageContext.getStackVersion());
        taskContext.setComponentName(componentDTO.getComponentName());
        taskContext.setServiceUser(serviceDTO.getServiceUser());
        taskContext.setServiceGroup(serviceDTO.getServiceGroup());
        taskContext.setRoot(clusterPO.getRoot());

        Map<String, Object> properties = new HashMap<>();
        properties.put("customCommands", componentDTO.getCustomCommands());
        properties.put("osSpecifics", serviceDTO.getOsSpecifics());
        properties.put("commandScript", componentDTO.getCommandScript());
        taskContext.setProperties(properties);
        return taskContext;
    }
}
