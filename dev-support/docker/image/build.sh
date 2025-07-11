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
  echo "Usage: build.sh <OS-VERSION>"
  echo
  echo "Example: build.sh rocky-8"
  exit 1
fi

OS=$(echo "$1" | cut -d '-' -f 1)
VERSION=$(echo "$1" | cut -d '-' -f 2-)


# Decimals are not supported. Either use integers only
# e.g. 16.04 -> 16
VERSION_INT=$(echo "$VERSION" | cut -d '.' -f 1)

log "OS: ${OS}; VERSION: ${VERSION}; VERSION_INT: ${VERSION_INT}"
case ${OS}-${VERSION_INT} in
    rocky-8)
      DOCKERFILE="Dockerfile.rocky8"
        ;;
    openeuler-22)
      DOCKERFILE="Dockerfile.openeuler22"
        ;;
    openeuler-24)
      DOCKERFILE="Dockerfile.openeuler24"
        ;;
    *)
        echo "Unsupported OS ${OS}-${VERSION}."
        exit 1
esac

docker build --rm --no-cache -t bigtop-manager/develop:${OS}-${VERSION} -f ${DOCKERFILE} ../..
