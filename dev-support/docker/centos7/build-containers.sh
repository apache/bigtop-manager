#!/bin/bash

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

BIN_DIR=$(dirname $0)
cd $BIN_DIR
echo $PWD

echo -e "\033[32mStarting container bigtop-manager-build\033[0m"
if [[ -z $(docker ps -a --format "table {{.Names}}" | grep "bigtop-manager-build") ]];then
  docker run -it -d --name bigtop-manager-build --privileged=true -e "container=docker" \
    -v /sys/fs/cgroup:/sys/fs/cgroup:ro -v $PWD/../../../:/opt/bigtop-manager/ \
    -w /opt/bigtop-manager \
    bigtop-manager/develop:trunk-centos-7
else
  docker start bigtop-manager-build
fi

echo -e "\033[32mCompiling bigtop-manager\033[0m"
docker exec bigtop-manager-build bash -c "mvn clean package -DskipTests"
docker stop bigtop-manager-build

echo -e "\033[32mCreating network bigtop-manager\033[0m"
docker network create --driver bridge bigtop-manager

echo -e "\033[32mCreating container bigtop-manager-server\033[0m"
docker run -d -p 3306:3306 -p 5005:5005 -p 8080:8080 --name bigtop-manager-server --hostname bigtop-manager-server --network bigtop-manager --privileged -e "container=docker" -v /sys/fs/cgroup:/sys/fs/cgroup:ro bigtop-manager/develop:trunk-centos-7 /usr/sbin/init
docker cp ../../../bigtop-manager-server/target/bigtop-manager-server bigtop-manager-server:/opt/
docker cp ../../../bigtop-manager-agent/target/bigtop-manager-agent bigtop-manager-server:/opt/
SERVER_PUB_KEY=`docker exec bigtop-manager-server /bin/cat /root/.ssh/id_rsa.pub`
docker exec bigtop-manager-server bash -c "echo '$SERVER_PUB_KEY' > /root/.ssh/authorized_keys"
docker exec bigtop-manager-server /bin/systemctl enable sshd
docker exec bigtop-manager-server /bin/systemctl start sshd

echo -e "\033[32mSetting up mariadb-server\033[0m"
docker exec bigtop-manager-server /bin/systemctl enable mariadb
docker exec bigtop-manager-server /bin/systemctl start mariadb
docker exec bigtop-manager-server bash -c "mysql -e \"UPDATE mysql.user SET Password = PASSWORD('root') WHERE User = 'root'\""
docker exec bigtop-manager-server bash -c "mysql -e \"GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION\""
docker exec bigtop-manager-server bash -c "mysql -e \"DROP USER ''@'localhost'\""
docker exec bigtop-manager-server bash -c "mysql -e \"DROP DATABASE test\""
docker exec bigtop-manager-server bash -c "mysql -e \"CREATE DATABASE bigtop_manager\""

docker exec bigtop-manager-server bash -c "mysql -e \"FLUSH PRIVILEGES\""

echo -e "\033[32mCreating container bigtop-manager-agent-01\033[0m"
docker run -d --name bigtop-manager-agent-01 --hostname bigtop-manager-agent-01 --network bigtop-manager --privileged -e "container=docker" -v /sys/fs/cgroup:/sys/fs/cgroup:ro bigtop-manager/develop:trunk-centos-7 /usr/sbin/init
docker cp ../../../bigtop-manager-agent/target/bigtop-manager-agent bigtop-manager-agent-01:/opt/
docker exec bigtop-manager-agent-01 bash -c "echo '$SERVER_PUB_KEY' > /root/.ssh/authorized_keys"
docker exec bigtop-manager-agent-01 /bin/systemctl enable sshd
docker exec bigtop-manager-agent-01 /bin/systemctl start sshd

echo -e "\033[32mCreating container bigtop-manager-agent-02\033[0m"
docker run -d --name bigtop-manager-agent-02 --hostname bigtop-manager-agent-02 --network bigtop-manager --privileged -e "container=docker" -v /sys/fs/cgroup:/sys/fs/cgroup:ro bigtop-manager/develop:trunk-centos-7 /usr/sbin/init
docker cp ../../../bigtop-manager-agent/target/bigtop-manager-agent bigtop-manager-agent-02:/opt/
docker exec bigtop-manager-agent-02 bash -c "echo '$SERVER_PUB_KEY' > /root/.ssh/authorized_keys"
docker exec bigtop-manager-agent-02 /bin/systemctl enable sshd
docker exec bigtop-manager-agent-02 /bin/systemctl start sshd

echo -e "\033[32mConfiguring hosts file\033[0m"
BIGTOP_MANAGER_SERVER_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' bigtop-manager-server`
BIGTOP_MANAGER_AGENT_01_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' bigtop-manager-agent-01`
BIGTOP_MANAGER_AGENT_02_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' bigtop-manager-agent-02`
docker exec bigtop-manager-server bash -c "echo '$BIGTOP_MANAGER_AGENT_01_IP      bigtop-manager-agent-01' >> /etc/hosts"
docker exec bigtop-manager-server bash -c "echo '$BIGTOP_MANAGER_AGENT_02_IP      bigtop-manager-agent-02' >> /etc/hosts"
docker exec bigtop-manager-agent-01 bash -c "echo '$BIGTOP_MANAGER_SERVER_IP      bigtop-manager-server' >> /etc/hosts"
docker exec bigtop-manager-agent-01 bash -c "echo '$BIGTOP_MANAGER_AGENT_02_IP      bigtop-manager-agent-02' >> /etc/hosts"
docker exec bigtop-manager-agent-02 bash -c "echo '$BIGTOP_MANAGER_SERVER_IP      bigtop-manager-server' >> /etc/hosts"
docker exec bigtop-manager-agent-02 bash -c "echo '$BIGTOP_MANAGER_AGENT_01_IP      bigtop-manager-agent-01' >> /etc/hosts"

echo -e "\033[32mSynchronize Chrony\033[0m"
docker exec bigtop-manager-server bash -c "systemctl enable chronyd; systemctl start chronyd; chronyc tracking"
docker exec bigtop-manager-agent-01 bash -c "systemctl enable chronyd; systemctl start chronyd; chronyc tracking"
docker exec bigtop-manager-agent-02 bash -c "systemctl enable chronyd; systemctl start chronyd; chronyc tracking"

echo -e "\033[32mServer Ip Setting\033[0m"
docker exec bigtop-manager-server bash -c "sed -i 's/bigtop.manager.server.host=localhost/bigtop.manager.server.host=$BIGTOP_MANAGER_SERVER_IP/' /opt/bigtop-manager-agent/conf/application.yml"
docker exec bigtop-manager-agent-01 bash -c "sed -i 's/bigtop.manager.server.host=localhost/bigtop.manager.server.host=$BIGTOP_MANAGER_SERVER_IP/' /opt/bigtop-manager-agent/conf/application.yml"
docker exec bigtop-manager-agent-02 bash -c "sed -i 's/bigtop.manager.server.host=localhost/bigtop.manager.server.host=$BIGTOP_MANAGER_SERVER_IP/' /opt/bigtop-manager-agent/conf/application.yml"

docker exec bigtop-manager-server bash -c "nohup /bin/bash /opt/bigtop-manager-server/bin/start.sh > /dev/null 2>&1 &"
docker exec bigtop-manager-server bash -c "nohup /bin/bash /opt/bigtop-manager-agent/bin/start.sh > /dev/null 2>&1 &"
docker exec bigtop-manager-agent-01 bash -c "nohup /bin/bash /opt/bigtop-manager-agent/bin/start.sh > /dev/null 2>&1 &"
docker exec bigtop-manager-agent-02 bash -c "nohup /bin/bash /opt/bigtop-manager-agent/bin/start.sh > /dev/null 2>&1 &"

echo -e "\033[32mPrint Bigtop-Manager Server RSA Private Key\033[0m"
docker exec bigtop-manager-server bash -c "cat ~/.ssh/id_rsa"

# MySQL HOST: bigtop-manager-server
# MySQL PORT: 3306
# DATABASE NAME: bigtop_manager
# DATABASE USER NAME: root
# DATABASE PASSWORD: root

