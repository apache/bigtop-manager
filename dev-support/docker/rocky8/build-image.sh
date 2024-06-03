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

echo -e "\033[32mRemoving image bigtop-manager:trunk-rocky-8\033[0m"
docker rmi bigtop-manager/develop:trunk-rocky-8

echo -e "\033[32mBuilding image bigtop-manager:trunk-rocky-8\033[0m"
docker build -t bigtop-manager/develop:trunk-rocky-8 .
