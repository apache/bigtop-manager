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
BIGTOP_MANAGER_HOME=${BIGTOP_MANAGER_HOME:-$(cd $BIN_DIR/..; pwd)}

source "${BIGTOP_MANAGER_HOME}/bin/env.sh"

usage() {
    echo "usage: $PROG [--debug]"
    echo "       --debug        - enable debug mode"
    echo "       -h, --help"
    exit 1
}

DEBUG="false"
DOCKER="false"

while [ $# -gt 0 ]; do
    case "$1" in
    --debug)
        echo "enable debug mode."
        DEBUG="true"
        shift;;
    -h|--help)
        usage
        shift;;
    *)
        echo "Unknown argument: '$1'" 1>&2
        usage;;
    esac
done

JAVA_OPTS=${JAVA_OPTS:-"-server -Duser.timezone=${SPRING_JACKSON_TIME_ZONE} -Xms4g -Xmx4g -Xmn2g -XX:+IgnoreUnrecognizedVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof"}

if [[ "$DOCKER" == "true" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -XX:-UseContainerSupport"
fi

if [[ "$DEBUG" == "true" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
fi

cd $BIGTOP_MANAGER_HOME

$JAVA_CMD $JAVA_OPTS \
  -cp "${BIGTOP_MANAGER_HOME}/conf":"${BIGTOP_MANAGER_HOME}/libs/*" \
  org.apache.bigtop.manager.server.BigtopManagerServer
