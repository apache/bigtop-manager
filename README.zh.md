<!---
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--->

# Bigtop-Manager

Bigtop-Manager 是一个用于管理 Bigtop 组件的平台。灵感来自 Apache Ambari。

## 先决条件

JDK：需要 JDK 17 或 21  
Metadata DB：Mariadb 或 Mysql

### API-文档

Swagger 用户界面

### 编译

```
mvn clean package -DskipTests
```

### 开发 人员

1. 创建数据库"bigtop_manager",配置数据库连接用户名和密码，默认均为“root”
2. 运行SQL DDL 脚本 `bigtop-manager-server/src/main/resources/ddl/MySQL-DDL-CREATE.sql`
3. 插入测试数据，数据脚本位于`dev-support/example/bigtop_manager/user.sql`
4. 启动 bigtop-manager-server `bigtop-manager-server/src/main/java/org/apache/bigtop/manager/server/ServerApplication.java`
5. 启动 bigtop-manager-agent `类似于启动bm-server`
6. 启动 bigtop-manager-ui `配置 nodejs 环境, 默认nodejs位于bigtop-manager-ui/node, 运行package.json`
7. 访问 `http://localhost:5173/`, 默认登录名和密码为 `"admin"`

### 如何测试服务

> 1. 登录
> 2. 创建群集 ->注册主机
> 3. 安装服务
> 4. 启动服务
> 5. 停止服务

### API 测试
- 访问 `http://localhost:8080/swagger-ui/index.html` 查看swagger API 文档
