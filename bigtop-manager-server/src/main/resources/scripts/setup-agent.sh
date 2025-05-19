#!/bin/bash
set -e

[ $# -lt 3 ] && {
    echo "Usage: $0 <dir_prefix> <repo_url> <grpc_port> [checksum_alg checksum_value]" >&2
    exit 1
}

USER=$(whoami)
GROUP=$(id -gn)

check_sudo() {
    if ! sudo -n true 2>/dev/null; then
        echo "ERROR: User '$USER' doesn't have sudo privileges" >&2
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
PROCESS_NAME="org.apache.bigtop.manager.agent.AgentApplication"

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
        echo "Checksum mismatch: Expected ${CHECK_ALG}=$CHECK_VAL, Found=$calc_val" >&2
        return 1
    }
}

# File handling with validation
handle_tar_file() {
    # Validate existing file
    if [ -f "$TAR_FILE" ]; then
        if [ -n "$CHECK_ALG" ]; then
            validate_checksum "$TAR_FILE" || {
                echo "Removing invalid file: $TAR_FILE" >&2
                rm -f "$TAR_FILE"
                return 1
            }
        fi
        return 0
    fi

    # Download file
    if command -v wget &> /dev/null; then
        wget "$DOWNLOAD_URL" -O "$TAR_FILE"
    else
        curl -L "$DOWNLOAD_URL" -o "$TAR_FILE"
    fi

    # Post-download validation
    if [ -n "$CHECK_ALG" ]; then
      echo "Validating checksum"
      validate_checksum "$TAR_FILE" || {
          rm -f "$TAR_FILE"
          echo "Downloaded file checksum validation failed" >&2
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

    if ! pgrep -f "${PROCESS_NAME}" > /dev/null; then
        nohup ${TARGET_DIR}/bin/start.sh --debug > /dev/null 2>&1 &
        sleep 10
        pgrep -f "${PROCESS_NAME}" > /dev/null || exit 1
    fi
}

deploy_agent
start