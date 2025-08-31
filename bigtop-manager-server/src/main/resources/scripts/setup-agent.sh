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

[ $# -lt 3 ] && {
    info "Usage: $0 <dir_prefix> <repo_url> <grpc_port> [checksum_alg checksum_value]"
    exit 1
}

USER=$(whoami)
GROUP=$(id -gn)

info() {
    echo $1 >> setup-agent.log 2>&1
}

error () {
    echo $1 >&2
    echo $1 >> setup-agent.log 2>&1
}

check_sudo() {
    # Refresh sudo privileges, currently only appears in openEuler24.03
    sudo visudo -c 2>/dev/null || true
    if ! sudo -n true 2>/dev/null; then
        error "User '$USER' doesn't have sudo privileges"
        exit 1
    fi
}

check_sudo

DIR_PREFIX="$1"
REPO_URL="$2"
GRPC_PORT="$3"
CHECK_ALG="${4:-}"
CHECK_VAL="${5:-}"

TAR_FILE="${DIR_PREFIX}/bigtop-manager-agent.tar.gz"
TARGET_DIR="${DIR_PREFIX}/bigtop-manager-agent"
DOWNLOAD_URL="${REPO_URL}/bigtop-manager-agent.tar.gz"
PROCESS_NAME="org.apache.bigtop.manager.agent.BigtopManagerAgent"

validate_checksum() {
    local file="$1"
    case "${CHECK_ALG}" in
        MD5)   sum_cmd="md5sum" ;;
        SHA-1)  sum_cmd="sha1sum" ;;
        SHA-256) sum_cmd="sha256sum" ;;
        *) return 0 ;;  # Skip validation if no algorithm specified
    esac

    local calc_val=$($sum_cmd "$file" | awk '{print $1}')
    [ "$calc_val" = "$CHECK_VAL" ] || {
        error "Checksum mismatch: Expected ${CHECK_ALG}=$CHECK_VAL, Found=$calc_val"
        return 1
    }
}

# File handling with validation
handle_tar_file() {
    # Validate existing file
    if [ -f "$TAR_FILE" ]; then
        if [ -n "$CHECK_ALG" ]; then
            validate_checksum "$TAR_FILE" || {
                info "Removing invalid file: $TAR_FILE"
                rm -f "$TAR_FILE"
            }
        else
            info "Using existing file: $TAR_FILE"
            return 0
        fi
    fi

    # Download file
    info "Downloading agent tarball from $DOWNLOAD_URL to $TAR_FILE"
    if command -v wget &> /dev/null; then
        wget -q "$DOWNLOAD_URL" -O "$TAR_FILE"
    else
        curl -sL "$DOWNLOAD_URL" -o "$TAR_FILE"
    fi

    # Post-download validation
    if [ -n "$CHECK_ALG" ]; then
      info "Validating checksum"
      validate_checksum "$TAR_FILE" || {
          rm -f "$TAR_FILE"
          error "Downloaded file checksum validation failed"
          exit 1
      }
    fi
}

deploy_agent() {
    [ -d "$TARGET_DIR" ] && return 0
    handle_tar_file
    sudo mkdir -p "${TARGET_DIR}"
    sudo chown -R ${USER}:${GROUP} "${TARGET_DIR}"
    tar -zxf "${TAR_FILE}" -C "${TARGET_DIR}" --strip-components=1
}

start() {
    export GRPC_PORT="$GRPC_PORT"

    info "Prepare to start agent"
    if ! pgrep -f "${PROCESS_NAME}" > /dev/null; then
        info "Starting agent"
        nohup ${TARGET_DIR}/bin/agent.sh start --debug > /dev/null 2>>setup-agent.log &
        sleep 10
        if ! pgrep -f "${PROCESS_NAME}" > /dev/null; then
          error "Failed to start agent, please check the log"
          exit 1
        else
          info "Agent started successfully"
        fi
    else
        info "Agent is already running"
    fi
}

# Remove previous log file
rm -f setup-agent.log

# Log variables
info "DIR_PREFIX: $DIR_PREFIX"
info "REPO_URL: $REPO_URL"
info "GRPC_PORT: $GRPC_PORT"
info "CHECK_ALG: $CHECK_ALG"
info "CHECK_VAL: $CHECK_VAL"

# Run deployment
deploy_agent
start
