
# Service

## Add a Service
This section describes how to add a service.

First, it should be clear that the installation entry for Infra Stack services is different from that of Bigtop/Extra Stack services. Infra Stack services are installed via the **Infrastructure** entry:

![Infrastructure](https://github.com/user-attachments/assets/d4c8e62a-c704-4ab9-90f1-49a661dda951)

While Bigtop/Extra Stack services are installed via the **Cluster** entry:

![Cluster](https://github.com/user-attachments/assets/41a5ba8a-6a0e-457d-a249-674ebc139ac2)

The services displayed differ by entry. Below, we use the ZooKeeper service in the Bigtop Stack as an example for explanation.

### Select Services
On this page, users can select the service we want to install. Each service has a **License** icon in the upper right corner:
* **Green**: Indicates the license is compatible with the Apache License.
* **Red**: Indicates license incompatibility. For such services, installation will default to downloading from the official website, user-configured repository will be inactive. Currently incompatible services include MySQL and Grafana in the Infra Stack.

![Service](https://github.com/user-attachments/assets/0b6a9dfc-e4c4-48f2-8ca4-8b31ba558578)

### Assign Components
Users need to assign components to hosts, switch components via the left sidebar:

![Component](https://github.com/user-attachments/assets/a436966f-6cc0-4c80-84ab-310e65fe9948)

### Configure Service
Users can modify service configurations here:

![Configure](https://github.com/user-attachments/assets/aac410c4-335c-4461-92f6-4af711547717)

### Service Overview
Users can verify if the assigned components and configurations are correct. If not, return to the previous step to make changes:

![Overview](https://github.com/user-attachments/assets/4bcbaea7-ddfb-4ee5-9bee-faea48216cab)

### Install
Finally, users only need to wait for the service to be added successfully. If it fails, resolve the issues and retry:

![Add Service](https://github.com/user-attachments/assets/0258888c-f7c5-40ba-9cba-dd73e492e919)