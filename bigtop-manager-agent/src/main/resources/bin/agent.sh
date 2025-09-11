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

set -e

BIN_DIR=$(dirname $0)
BIGTOP_MANAGER_HOME=${BIGTOP_MANAGER_HOME:-$(cd $BIN_DIR/..; pwd)}
PID_FILE="${BIGTOP_MANAGER_HOME}/bigtop-manager-agent.pid"

source "${BIGTOP_MANAGER_HOME}/bin/env.sh"

usage() {
    echo "usage: $PROG {start|stop|restart} [--debug]"
    echo "       start          - start the agent"
    echo "       stop           - stop the agent"
    echo "       restart        - restart the agent"
    echo "       --debug        - enable debug mode"
    echo "       -h, --help"
    exit 1
}

DEBUG="false"
DOCKER="false"
ACTION=""

error () {
    echo $1 >&2
}

# Parse command line arguments
while [ $# -gt 0 ]; do
    case "$1" in
    start|stop|restart)
        ACTION="$1"
        shift;;
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

# If no action specified, default to start for backward compatibility
if [ -z "$ACTION" ]; then
    ACTION="start"
fi

# Check if process is running
is_running() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            # Verify it's actually our agent process
            if ps -p "$pid" -o args= | grep -q "org.apache.bigtop.manager.agent.BigtopManagerAgent"; then
                return 0
            else
                # PID file exists but process is not our agent, remove stale PID file
                rm -f "$PID_FILE"
            fi
        else
            # PID file exists but process is not running, remove stale PID file
            rm -f "$PID_FILE"
        fi
    fi
    
    # No valid PID file, check if process is running and create PID file if found
    local running_pid=$(ps -ef | grep "org.apache.bigtop.manager.agent.BigtopManagerAgent" | grep -v grep | awk '{print $2}' | head -1)
    if [ -n "$running_pid" ]; then
        echo "$running_pid" > "$PID_FILE"
        echo "Found running Bigtop Manager Agent process (PID: $running_pid), created PID file"
        return 0
    fi
    
    return 1
}

start_agent() {
    if is_running; then
        error "Bigtop Manager Agent is already running (PID: $(cat $PID_FILE))"
        exit 1
    fi

    # Check and set default GRPC_PORT if not configured
    if [ -z "$GRPC_PORT" ]; then
        export GRPC_PORT="8835"
        echo "GRPC_PORT not set, using default: 8835"
    fi

    echo "Starting Bigtop Manager Agent..."

    JAVA_OPTS=${JAVA_OPTS:-"-server -Duser.timezone=${SPRING_JACKSON_TIME_ZONE} -Xms4g -Xmx4g -Xmn2g -XX:+IgnoreUnrecognizedVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof"}

    if [[ "$DOCKER" == "true" ]]; then
      JAVA_OPTS="${JAVA_OPTS} -XX:-UseContainerSupport"
    fi

    if [[ "$DEBUG" == "true" ]]; then
      JAVA_OPTS="${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006"
    fi

    cd $BIGTOP_MANAGER_HOME

    nohup $JAVA_CMD $JAVA_OPTS \
      -cp "${BIGTOP_MANAGER_HOME}/conf":"${BIGTOP_MANAGER_HOME}/libs/*" \
      org.apache.bigtop.manager.agent.BigtopManagerAgent > /dev/null 2>&1 &

    echo $! > "$PID_FILE"
    echo "Bigtop Manager Agent started successfully (PID: $!)"
}

stop_agent() {
    if ! is_running; then
        echo "Bigtop Manager Agent is not running"
        return 0
    fi

    local pid=$(cat "$PID_FILE")
    echo "Stopping Bigtop Manager Agent (PID: $pid)..."

    # Try graceful shutdown first
    kill "$pid" 2>/dev/null || true

    # Wait for process to terminate
    local count=0
    while [ $count -lt 30 ]; do
        if ! ps -p "$pid" > /dev/null 2>&1; then
            break
        fi
        sleep 1
        count=$((count + 1))
    done

    # Force kill if still running
    if ps -p "$pid" > /dev/null 2>&1; then
        echo "Force killing process..."
        kill -9 "$pid" 2>/dev/null || true
    fi

    rm -f "$PID_FILE"
    echo "Bigtop Manager Agent stopped successfully"
}

restart_agent() {
    stop_agent
    start_agent
}

# Execute the requested action
case "$ACTION" in
    start)
        start_agent
        ;;
    stop)
        stop_agent
        ;;
    restart)
        restart_agent
        ;;
    *)
        usage
        ;;
esac
