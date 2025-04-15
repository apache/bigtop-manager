# 服务
## 新增服务
本节我们来描述如何新增服务

首先需要明确的是，Infra Stack 服务和 Bigtop/Extra Stack 服务的安装入口不同，Infra Stack 服务的安装入口在 Infrastructure 中

![Infrastructure](https://github.com/user-attachments/assets/d4c8e62a-c704-4ab9-90f1-49a661dda951)

而 Bigtop/Extra Stack 服务的安装入口在集群中

![Cluster](https://github.com/user-attachments/assets/41a5ba8a-6a0e-457d-a249-674ebc139ac2)

不同的入口你看到的服务也会不同，后面我将以 Bigtop Stack 中的 ZooKeeper 服务为例来讲解

### 选择服务
在这个页面中，用户可以选择自己想要安装的服务，其中每个服务的右上角有 License 标识
* **绿色**：表明该 License 与 Apache License 兼容
* **红色**：表明该 License 与 Apache License 不兼容，对应服务在安装时默认通过官方服务器下载，用户配置的 Repository 无效，目前不兼容的服务有 Infra Stack 中的 MySQL 与 Grafana。

![Service](https://github.com/user-attachments/assets/0b6a9dfc-e4c4-48f2-8ca4-8b31ba558578)

### 分配组件
用户需要对组件进行主机分配，左侧切换组件

![Component](https://github.com/user-attachments/assets/a436966f-6cc0-4c80-84ab-310e65fe9948)

### 配置服务
用户可在此处对服务配置进行更改

![Configure](https://github.com/user-attachments/assets/aac410c4-335c-4461-92f6-4af711547717)

### 服务总览
用户可确定对应的组件与配置是否正确，若不正确则退到前一步进行更改

![Overview](https://github.com/user-attachments/assets/4bcbaea7-ddfb-4ee5-9bee-faea48216cab)

### 安装服务
最后用户只需等待服务添加完成即可，若失败则解决问题后重试

![Add Service](https://github.com/user-attachments/assets/0258888c-f7c5-40ba-9cba-dd73e492e919)