
# Cluster

## Create a Cluster
This section describes how to create a cluster.

### Basic Information
When creating a cluster, users need to fill in the following information:
- **Name**: The unique identifier of the cluster, a key information for the code to recognize the cluster.
- **DisplayName**: The display name of the cluster, shown on the page for users to distinguish.
- **Description**: A detailed description of the cluster.
- **Root Directory**: The address where cluster services are installed. Corresponding service directories will be created here. For example, if this is `/opt`, ZooKeeper will be installed under `/opt/services/zookeeper`.
- **User Group**: The user group for cluster services. A separate username will be created for each service. If this is `hadoop`, the file permissions for ZooKeeper will be `zookeeper:hadoop`.

![Basic Info](https://github.com/user-attachments/assets/4fb8ccad-0694-4b9a-a0ac-33c736575391)

### Stack
The stack page is a display page that mainly shows which optional services are available for subsequent installation.

![Stack](https://github.com/user-attachments/assets/faf1112c-f0a6-4353-ba63-83abbb819c29)

Currently, since we do not provide an official repository, you need to set it up yourself.  
You can use a third-party repository: http://213.146.155.38/release/1.0.0/  
Or if you are in China, you can download the corresponding packages from BaiduNetdisk and set up a local repository: https://pan.baidu.com/s/162FXYsaRuwFQjrOlMuDRjg?pwd=hufb

![Repository](https://github.com/user-attachments/assets/dc06935b-f3d0-42c7-b535-d5f6f15f6356)

### Hosts
#### Add a Host
When adding a host, the following information needs to be filled in:
- **Username**: The user on the host.
- **Authentication Method**: Authentication method (password/key/no authentication).
- **Hostname**: Hostname, supporting batch addition such as `host-0[1-2]`.
- **Agent Path**: The path where the Agent is installed. If this is `/opt`, the Agent directory will be `/opt/bigtop-manager-agent`.
- **SSH Port**: The port used by the host's SSHD.
- **GRPC Port**: The port where the Agent's gRPC service is exposed as desired by the user.
- **Description**: Host description.

![Host](https://github.com/user-attachments/assets/761b9931-54f3-4309-adc0-87b611b68e7f)

#### Install Dependencies
After entering the host information and before proceeding to the next step, users need to install dependencies, i.e., install the agent application on the corresponding host.

![Dependency](https://github.com/user-attachments/assets/0dedfbb3-dfbb-4d06-8a9c-d0e0366a1f50)

Users can only proceed to the next step if all hosts are installed successfully. Otherwise, they need to fix the errors or remove the hosts where installation failed.

![Host](https://github.com/user-attachments/assets/c836e570-fa6a-411f-b3b4-0efb4b55d6ef)

### Create
Finally, wait for the cluster to be created successfully. If it fails, resolve the issues and retry.

![Create Cluster](https://github.com/user-attachments/assets/339a289e-c718-4953-bdb2-15232978fd49)