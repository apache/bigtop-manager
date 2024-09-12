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

if docker ps -a | grep -q 'bigtop-manager-build-oe22'; then
  echo -e "\033[32mStopping container bigtop-manager-build-oe22 and maven process\033[0m"
  if [ `docker inspect --format '{{.State.Running}}' bigtop-manager-build-oe22` == true ];then
    docker exec bigtop-manager-build-oe22 bash -c "pkill -KILL -f maven"
    docker stop bigtop-manager-build-oe22
  fi
fi

echo -e "\033[32mRemoving container bigtop-manager-server\033[0m"
docker rm -f bigtop-manager-server

echo -e "\033[32mRemoving container bigtop-manager-agent-01\033[0m"
docker rm -f bigtop-manager-agent-01

echo -e "\033[32mRemoving container bigtop-manager-agent-02\033[0m"
docker rm -f bigtop-manager-agent-02

echo -e "\033[32mRemoving network bigtop-manager\033[0m"
docker network rm bigtop-manager