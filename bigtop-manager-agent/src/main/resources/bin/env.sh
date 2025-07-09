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

JAVA_SEARCH_DIRS=(
  "/usr/local/java*"
  "/usr/lib/jvm/java-*"
  "/usr/java/jdk*"
  "/opt/java*"
  "/opt/jdk*"
  "/usr/lib/jvm/*"
)

# Find a suitable Java (version >= 17)
find_java() {
  if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CANDIDATES=("$JAVA_HOME/bin/java")
  else
    JAVA_CANDIDATES=()
    JAVA_PATH=$(command -v java 2>/dev/null || true)
    [ -n "$JAVA_PATH" ] && JAVA_CANDIDATES+=("$JAVA_PATH")

    for pattern in "${JAVA_SEARCH_DIRS[@]}"; do
      for dir in $(compgen -G "$pattern" 2>/dev/null); do
        # Check privileges, make sure the script won't exit
        [ -x "$dir/bin/java" ] 2>/dev/null || true
        [ -x "$dir/bin/java" ] && JAVA_CANDIDATES+=("$dir/bin/java")
      done
    done
  fi

  for JAVA_CMD in "${JAVA_CANDIDATES[@]}"; do
    VERSION_STR=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    MAJOR_VERSION=$(echo "$VERSION_STR" | awk -F. '{if ($1 == "1") print $2; else print $1}')
    if [ "$MAJOR_VERSION" -ge 17 ]; then
      export JAVA_CMD
      return 0
    fi
  done

  echo "Error: Java 17 or higher is required. No suitable java found." >&2
  exit 1
}

find_java

export JAVA_OPTS=""
export JAVA_CMD
