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

set -e

usage() {
    echo "usage: $PROG args"
    echo "  commands:"
    echo "       -c NUM_INSTANCES, --create NUM_INSTANCES  - Create Docker containers based bigtop-manager cluster, defaults to 3"
    echo "       -e, --database                            - The specified database, defaults to postgres"
    echo "       -o, --os                                  - Specify the operating system, default is rocky-8"
    echo "       --skip-compile                            - Skip Compile"
    echo "       -d, --destroy                             - Destroy all containers"
    echo "       -h, --help"
    exit 1
}

log() {
    echo -e "\033[32m[LOG] $1\033[0m"
}

build() {
    log "Build on docker: $SKIP_COMPILE"
    if ! $SKIP_COMPILE; then
      log "Compiling bigtop-manager"
      docker run -it --rm -u $(id -u):$(id -g) \
        -v $PWD/../../../:/opt/develop/bigtop-manager/ \
        -v /$USER/.m2:/$USER/.m2 \
        -w /opt/develop/bigtop-manager \
        bigtop-manager/develop:${OS} bash -c "mvn clean package -DskipTests"
    else
      log "Skip Compile!!!"
    fi
    log "Build Success!!!"
}

destroy() {
  log "Destroy Containers!!!"
  docker rm -f $(docker ps -aq --filter network=bigtop-manager --filter "name=^bm-")
  exit 0
}

create() {
  log "Create Containers!!!"
  docker network inspect bigtop-manager >/dev/null 2>&1 || docker network create --driver bridge bigtop-manager
  create_db
  create_container
}

create_container() {
  for ((i=1;i<=$NUM_INSTANCES;i+=1))
  do
    container_name="bm-$i"
    log "Create ${container_name}"
    if [ $i -eq 1 ]; then
      docker run -itd -p 15005:5005 -p 15006:5006 -p 18080:8080 --name ${container_name} --hostname ${container_name} --network bigtop-manager --cap-add=SYS_TIME bigtop-manager/develop:${OS}
      docker cp ../../../bigtop-manager-dist/target/apache-bigtop-manager-*-server.tar.gz ${container_name}:/opt/bigtop-manager-server.tar.gz
      docker exec ${container_name} bash -c "cd /opt && tar -zxvf bigtop-manager-server.tar.gz"
      docker exec ${container_name} bash -c "ssh-keygen -f '/root/.ssh/id_rsa' -N '' -t rsa"
      SERVER_PUB_KEY=`docker exec ${container_name} /bin/cat /root/.ssh/id_rsa.pub`
    else
      docker run -itd --name ${container_name} --hostname ${container_name} --network bigtop-manager --cap-add=SYS_TIME bigtop-manager/develop:${OS}
    fi

    docker cp ../../../bigtop-manager-dist/target/apache-bigtop-manager-*-agent.tar.gz ${container_name}:/opt/bigtop-manager-agent.tar.gz
    docker exec ${container_name} bash -c "mkdir -p /root/.ssh && echo '$SERVER_PUB_KEY' > /root/.ssh/authorized_keys"
    docker exec ${container_name} ssh-keygen -N '' -t rsa -b 2048 -f /etc/ssh/ssh_host_rsa_key
    docker exec ${container_name} ssh-keygen -N '' -t ecdsa -b 256 -f /etc/ssh/ssh_host_ecdsa_key
    docker exec ${container_name} ssh-keygen -N '' -t ed25519 -b 256 -f /etc/ssh/ssh_host_ed25519_key
    docker exec ${container_name} /bin/systemctl start sshd
    docker exec ${container_name} bash -c "systemctl start chronyd && chronyc tracking"
  done

  containers=($(docker network inspect bigtop-manager -f '{{range .Containers}}{{.Name}}{{" "}}{{end}}'))
  for container in ${containers[@]}; do
    container_ip=$(docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $container)
    log "container: ${container}; container_ip: ${container_ip}"
    for container2 in ${containers[@]}; do
      if [ ${container2} != $container ]; then
        docker exec ${container2} bash -c "echo '${container_ip}      ${container}' >> /etc/hosts"
      fi
    done
  done

  # wait database started
  for container in ${containers[@]}; do
    if [ ${container} == "bm-1" ]; then
      if [ $DATABASE == "mysql" ]; then
        log "docker exec ${container} bash -c \"mysql -h bm-mysql -P 3306 -uroot -proot -e 'create database bigtop_manager'\""
        docker exec ${container} bash -c "mysql -h bm-mysql -P 3306 -uroot -proot -e 'create database bigtop_manager'"
        docker exec ${container} bash -c "mysql -h bm-mysql -P 3306 -uroot -proot -Dbigtop_manager < /opt/bigtop-manager-server/ddl/MySQL-DDL-CREATE.sql"

        docker exec ${container} bash -c "wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar -O /opt/bigtop-manager-server/libs/mysql-connector-java-8.0.33.jar"
        docker exec ${container} bash -c "sed -i 's/org.postgresql.Driver/com.mysql.cj.jdbc.Driver/' /opt/bigtop-manager-server/conf/application.yml"
        docker exec ${container} bash -c "sed -i 's/postgresql/mysql/' /opt/bigtop-manager-server/conf/application.yml"
        docker exec ${container} bash -c "sed -i 's/localhost:5432/bm-mysql:3306/' /opt/bigtop-manager-server/conf/application.yml"
        docker exec ${container} bash -c "sed -i 's/postgres/root/' /opt/bigtop-manager-server/conf/application.yml"
      elif [ $DATABASE == "postgres" ]; then
        docker exec ${container} bash -c "PGPASSWORD=postgres psql -h bm-postgres -p5432 -U postgres -c 'create database bigtop_manager'"
        docker exec ${container} bash -c "PGPASSWORD=postgres psql -h bm-postgres -p5432 -U postgres -d bigtop_manager -f /opt/bigtop-manager-server/ddl/PostgreSQL-DDL-CREATE.sql"
        docker exec ${container} bash -c "sed -i 's/localhost:5432/bm-postgres:5432/' /opt/bigtop-manager-server/conf/application.yml"
      fi
      docker exec ${container} bash -c "nohup /bin/bash /opt/bigtop-manager-server/bin/server.sh start --debug > /dev/null 2>&1 &"
    fi
    log "All Service Started!!!"
  done

}

create_db() {
  if [ $DATABASE == "mysql" ]; then
    docker run --restart=always -it -d \
       -p 13306:3306 \
       --cap-add=SYS_TIME \
       --network bigtop-manager \
       --name bm-mysql \
       --hostname bm-mysql \
       -e MYSQL_ROOT_PASSWORD=root \
       mysql:8.0
    # Loop check log
    while true; do
        # Use the Docker logs command to retrieve the latest log content
        logs=$(docker logs bm-mysql | tail -n 100)
        log "$logs"

        # Check if the log contains specific strings
        if echo "$logs" | grep -q "/usr/sbin/mysqld: ready for connections"; then
            echo "MySQL is ready for connections."
            break
        else
            echo "MySQL is not ready yet, waiting..."
            # Wait for a while and check again
            sleep 5
        fi
    done
  elif [ $DATABASE == "postgres" ]; then
    docker run --restart=always -d \
      -p 15432:5432 \
      --network bigtop-manager \
      --name bm-postgres \
      --hostname bm-postgres \
    	-e POSTGRES_PASSWORD=postgres \
    	postgres:16
  fi
}


BIN_DIR=$(dirname $0)
cd $BIN_DIR
echo $PWD

PROG=`basename $0`

DATABASE=postgres
OS=rocky-8
NUM_INSTANCES=3
SKIP_COMPILE=false

while [ $# -gt 0 ]; do
    case "$1" in
    -e|--database)
        if [ $# -lt 2 ]; then
          echo "Requires a db" 1>&2
          usage
        fi
        if [ $2 != "postgres" ] && [ $2 != "mysql" ]; then
          echo "The Database should be [postgres], or [mysql]" 1>&2
          usage
        fi
        DATABASE=$2
        shift 2;;
    -o|--os)
        if [ $# -lt 2 ]; then
          echo "Requires a os" 1>&2
          usage
        fi
        if [ $2 != "rocky-8" ] && [ $2 != "openeuler-22" ] && [ $2 != "openeuler-24" ]; then
          echo "The OS should be [rocky-8], [openeuler-22] or [openeuler-24]" 1>&2
          usage
        fi
        OS=$2
        shift 2;;
    -c|--create)
        if [ $# -lt 2 ]; then
          echo "Requires a container number" 1>&2
          usage
        fi
        if [ $2 -gt 10 ] || [ $2 -lt 1 ]; then
          echo "NUM-INSTANCES should be between [1-10]" 1>&2
          usage
        fi
        NUM_INSTANCES=$2
        shift 2;;
    --skip-compile)
        SKIP_COMPILE=true
        shift;;
    -d|--destroy)
        destroy
        shift;;
    -h|--help)
        usage
        shift;;
    *)
        echo "Unknown argument: '$1'" 1>&2
        usage;;
    esac
done

log "DATABASE: $DATABASE; OS: $OS; NUM_INSTANCES: $NUM_INSTANCES; SKIP_COMPILE: $SKIP_COMPILE; "

build

create
