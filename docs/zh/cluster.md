# 集群
## 创建集群
本节我们来描述如何创建集群

### 基本信息
创建集群时用户需要填写如下信息
* Name：集群唯一标识，是代码识别集群的关键信息
* DisplayName：集群显示名，展示在页面上供用户区分
* Description：集群描述，描述集群详细信息
* Root Directory：集群服务安装的地址，对应的服务目录会创建至该处，若此处为 `/opt` 则 ZooKeeper 会被安装在 `/opt/services/zookeeper` 下
* User Group：集群服务的用户组，每个服务会分别创建一个用户名，如此处为 `hadoop`，则 ZooKeeper 文件对应的权限则为 `zookeeper:hadoop`

![Basic Info](https://github.com/user-attachments/assets/4fb8ccad-0694-4b9a-a0ac-33c736575391)

### 组件栈
组件栈页面为展示页面，主要显示后续安装服务时有哪些可选服务

目前 Bigtop 组件栈提供的是 Apache Bigtop 生成的 tarball 文件而非 rpm/deb 包

![Stack](https://github.com/user-attachments/assets/faf1112c-f0a6-4353-ba63-83abbb819c29)

除了官方 Repository 外，用户也可以搭建自己的 Repository 并在此处进行配置（注意 Infra 中的 MySQL/Grafana 由于 License 原因，是从官方网站下载而不是从 Repository 下载）

我们也提供了其他方式供您下载相应的依赖：
* 百度云：https://pan.baidu.com/s/162FXYsaRuwFQjrOlMuDRjg?pwd=hufb

![Repository](https://github.com/user-attachments/assets/9ff12f07-5a15-42e7-84d7-a3eab3455468)

### 主机
#### 新增主机
新增主机时需要填写如下信息
* Username：该主机上的用户
* Authentication Method：认证方式，密码/密钥/无认证
* Hostname：主机名，支持批量添加如 `host-0[1-2]`
* Agent Path：Agent 安装的路径，若此处为 `/opt`，则 Agent 目录则为 `/opt/bigtop-manager-agent`
* SSH Port：主机 SSHD 所使用的端口
* GRPC Port：用户希望 Agent 的 gRPC 服务暴露的端口
* Description：主机描述

![Host](https://github.com/user-attachments/assets/761b9931-54f3-4309-adc0-87b611b68e7f)

#### 安装依赖
主机信息输入完成后，在进入下一步之前，用户需要安装依赖，即在对应的主机上安装 Agent 应用

![Dependency](https://github.com/user-attachments/assets/0dedfbb3-dfbb-4d06-8a9c-d0e0366a1f50)

且只有所有主机均安装成功时用户才可进入下一步，否则需要修复错误或者移除安装失败的主机

![Host](https://github.com/user-attachments/assets/c836e570-fa6a-411f-b3b4-0efb4b55d6ef)

### 创建集群
最后等待集群创建成功即可，若失败则解决问题后重试

![Create Cluster](https://github.com/user-attachments/assets/339a289e-c718-4953-bdb2-15232978fd49)