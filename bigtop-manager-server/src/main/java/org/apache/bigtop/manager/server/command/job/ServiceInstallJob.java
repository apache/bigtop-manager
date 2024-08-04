package org.apache.bigtop.manager.server.command.job;

import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.HostRepository;
import org.apache.bigtop.manager.dao.repository.ServiceRepository;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.ComponentHostDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.service.ConfigService;
import org.apache.bigtop.manager.server.utils.StackUtils;

import java.util.ArrayList;
import java.util.List;

public class ServiceInstallJob extends AbstractServiceJob {

    private ConfigService configService;
    private ServiceRepository serviceRepository;
    private HostRepository hostRepository;

    public ServiceInstallJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.configService = SpringContextHolder.getBean(ConfigService.class);
        this.serviceRepository = SpringContextHolder.getBean(ServiceRepository.class);
        this.hostRepository = SpringContextHolder.getBean(HostRepository.class);
    }

    @Override
    protected void createStages() {
        // Install components
        super.createInstallStages();

        // Update cache files after installed
        super.createCacheStage();

        // Start all master components
        super.createStartStages();

        // Check all master components after started
        super.createCheckStages();
    }

    @Override
    protected List<String> getComponentNames() {
        List<String> componentNames = new ArrayList<>();
        for (ServiceCommandDTO serviceCommand : jobContext.getCommandDTO().getServiceCommands()) {
            List<ComponentHostDTO> componentHosts = serviceCommand.getComponentHosts();
            for (ComponentHostDTO componentHost : componentHosts) {
                String componentName = componentHost.getComponentName();
                componentNames.add(componentName);
            }
        }

        return componentNames;
    }

    @Override
    protected List<String> findHostnamesByComponentName(String componentName) {
        for (ServiceCommandDTO serviceCommand : jobContext.getCommandDTO().getServiceCommands()) {
            List<ComponentHostDTO> componentHosts = serviceCommand.getComponentHosts();
            for (ComponentHostDTO componentHost : componentHosts) {
                if (componentHost.getComponentName().equals(componentName)) {
                    List<String> hostnames = new ArrayList<>(componentHost.getHostnames());
                    if (serviceCommand.getInstalled()) {
                        List<String> existHostnames = hostComponentRepository
                                .findAllByComponentPOClusterPOIdAndComponentPOComponentNameAndHostPOHostnameIn(
                                        clusterPO.getId(), componentName, hostnames)
                                .stream()
                                .map(hostComponent -> hostComponent.getHostPO().getHostname())
                                .toList();

                        hostnames.removeAll(existHostnames);
                    }

                    return hostnames;
                }
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        CommandDTO commandDTO = jobContext.getCommandDTO();
        List<ServiceCommandDTO> serviceCommands = commandDTO.getServiceCommands();
        Long clusterId = commandDTO.getClusterId();

        // Persist service, component and hostComponent metadata to database
        for (ServiceCommandDTO serviceCommand : serviceCommands) {
            String serviceName = serviceCommand.getServiceName();
            ServicePO servicePO = serviceRepository.findByClusterPOIdAndServiceName(clusterId, serviceName);
            upsertService(servicePO, serviceCommand);
        }
    }

    private void upsertService(ServicePO servicePO, ServiceCommandDTO serviceCommand) {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        Long clusterId = commandDTO.getClusterId();
        String serviceName = serviceCommand.getServiceName();
        ClusterPO clusterPO = clusterRepository.getReferenceById(clusterId);

        String stackName = clusterPO.getStackPO().getStackName();
        String stackVersion = clusterPO.getStackPO().getStackVersion();

        // 1. Persist service and components
        if (servicePO == null) {
            ServiceDTO serviceDTO = StackUtils.getServiceDTO(stackName, stackVersion, serviceName);
            servicePO = ServiceConverter.INSTANCE.fromDTO2PO(serviceDTO, clusterPO);
            servicePO = serviceRepository.save(servicePO);
        }

        // 2. Update configs
        configService.upsert(clusterId, servicePO.getId(), serviceCommand.getConfigs());

        for (ComponentHostDTO componentHostDTO : serviceCommand.getComponentHosts()) {
            String componentName = componentHostDTO.getComponentName();

            // 3. Persist component
            ComponentPO componentPO = componentRepository.findByClusterPOIdAndComponentName(clusterId, componentName);
            if (componentPO == null) {
                ComponentDTO componentDTO = StackUtils.getComponentDTO(stackName, stackVersion, componentName);
                componentPO = ComponentConverter.INSTANCE.fromDTO2PO(componentDTO, servicePO, clusterPO);
                componentPO = componentRepository.save(componentPO);
            }

            // 4. Persist hostComponent
            for (String hostname : componentHostDTO.getHostnames()) {
                HostComponentPO hostComponentPO =
                        hostComponentRepository.findByComponentPOComponentNameAndHostPOHostname(
                                componentName, hostname);
                if (hostComponentPO == null) {
                    HostPO hostPO = hostRepository.findByHostname(hostname);

                    hostComponentPO = new HostComponentPO();
                    hostComponentPO.setHostPO(hostPO);
                    hostComponentPO.setComponentPO(componentPO);
                    hostComponentPO.setState(MaintainState.UNINSTALLED);
                    hostComponentRepository.save(hostComponentPO);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Install services";
    }
}
