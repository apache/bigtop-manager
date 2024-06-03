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

echo -e "\033[32mRestarting containers\033[0m"
docker restart bigtop-manager-server
docker restart bigtop-manager-agent-01
docker restart bigtop-manager-agent-02

echo -e "\033[32mRe-enabling systemctl servers\033[0m"
docker exec bigtop-manager-server bash -c "systemctl start mariadb"
docker exec bigtop-manager-server bash -c "systemctl start sshd"
docker exec bigtop-manager-server bash -c "systemctl start chronyd"
docker exec bigtop-manager-agent-01 bash -c "systemctl start sshd"
docker exec bigtop-manager-agent-01 bash -c "systemctl start chronyd"
docker exec bigtop-manager-agent-02 bash -c "systemctl start sshd"
docker exec bigtop-manager-agent-02 bash -c "systemctl start chronyd"

echo -e "\033[32mSynchronize Chrony\033[0m"
docker exec bigtop-manager-server bash -c "chronyc tracking"
docker exec bigtop-manager-agent-01 bash -c "chronyc tracking"
docker exec bigtop-manager-agent-02 bash -c "chronyc tracking"

echo -e "\033[32mRestarting bigtop-manager servers\033[0m"
docker exec bigtop-manager-server bash -c "nohup /bin/bash /opt/bigtop-manager-server/bin/start.sh --debug > /dev/null 2>&1 &"
docker exec bigtop-manager-server bash -c "nohup /bin/bash /opt/bigtop-manager-agent/bin/start.sh > /dev/null 2>&1 &"
docker exec bigtop-manager-agent-01 bash -c "nohup /bin/bash /opt/bigtop-manager-agent/bin/start.sh > /dev/null 2>&1 &"
docker exec bigtop-manager-agent-02 bash -c "nohup /bin/bash /opt/bigtop-manager-agent/bin/start.sh > /dev/null 2>&1 &"
