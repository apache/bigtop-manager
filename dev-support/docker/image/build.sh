#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -ex

log() {
    echo -e "\n[LOG] $1\n"
}

exception() {
  echo "[ERROR] Unsupported [${OS}-${VERSION}]"
  exit 1
}

BIN_DIR=$(dirname $0)
cd $BIN_DIR
echo $PWD

if [ $# != 1 ]; then
  echo "Creates bigtop-manager/develop image"
  echo
  echo "Usage: build.sh <PREFIX-OS-VERSION>"
  echo
  echo "Example: build.sh trunk-rockylinux-8"
  echo "       : build.sh 1.0.0-rockylinux-8"
  exit 1
fi

PREFIX=$(echo "$1" | cut -d '-' -f 1)
OS=$(echo "$1" | cut -d '-' -f 2)
VERSION=$(echo "$1" | cut -d '-' -f 3-)


# Decimals are not supported. Either use integers only
# e.g. 16.04 -> 16
VERSION_INT=$(echo "$VERSION" | cut -d '.' -f 1)

log "PREFIX: ${PREFIX}; OS: ${OS}; VERSION: ${VERSION}; VERSION_INT: ${VERSION_INT}"
DOCKER_OS=${OS}
DOCKER_VERSION=${VERSION}
CUSTOM_REPO=""
case ${OS}-${VERSION_INT} in
    centos-7)
      CUSTOM_REPO="RUN mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.back \
      && curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo"
        ;;
    rocky-8|rocky-9)
      DOCKER_OS=rockylinux
        ;;
    openeuler-22)
      DOCKER_OS=openeuler/openeuler
      DOCKER_VERSION=22.03
        ;;
    *)
        echo "Unsupported OS ${OS}-${VERSION}."
        exit 1
esac

# generate Dockerfile for build
sed -e "s|OS|${DOCKER_OS}|;s|VERSION|${DOCKER_VERSION}|" Dockerfile.template |
 sed -e "/MAINTAINER dev@bigtop.apache.org/a\\$CUSTOM_REPO" > Dockerfile

docker build --rm --no-cache -t bigtop-manager/develop:${PREFIX}-${OS}-${VERSION} -f Dockerfile ../..
rm -f Dockerfile
