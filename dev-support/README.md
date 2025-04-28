<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
-->

## Build and install Bigtop Manager by dev-support
Dev support is used to quickly develop and test bigtop-manager, which runs on the docker containers.
Following steps are based on RockyLinux-8.

### **Step 1**: Install build tools: Git、Docker
The scripts require docker to be installed, since the compile process will run in a docker container and Bigtop-Manager cluster also deploys on containers.

**RHEL (Rocky 8) :**
```shell
yum install -y git docker
```
### **Step 2**: Download Bigtop Manager source
```shell
git clone https://github.com/apache/bigtop-manager.git
```

### **Step 3**: Build develop basic image
Run the setup command, you will get `bigtop-manager/develop:rocky-8` image. It has the environment needed to compile Bigtop-Manager and run servers such as Bigtop-Manager Server, Bigtop-Manager Agent, Mysql, etc.

**RHEL (Rocky 8) :**
```shell
/bin/bash dev-support/docker/image/build.sh rocky-8
```
### **Step 4**: Build source & create cluster
* Bigtop Manager UI、Bigtop Manager Server Debug Port、MariaDB Server are also exposed to local ports: 18080、15005、13306.
* Docker hostnames are: bm-1、bm-2、bm-3 and etc.

**RHEL (Rocky 8) :**
```shell
/bin/bash dev-support/docker/containers/build.sh -e postgres -c 3 -o rocky-8 [--skip-compile]
```
### **Step 5**: Access Web UI
Now you can access Web UI which exposes on `http://localhost:18080`. Log in with username `admin` and password `admin`.
### **Step 6**: Clear cluster
Clean up the containers when you are done developing or testing.

**RHEL (Rocky 8) :**
```shell
/bin/bash dev-support/docker/containers/build.sh -d
```
