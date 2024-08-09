<!---
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--->

# mysql数据库支持

## 1、下载相关jar包
- mysql-connector-java-6.0.6.jar
- mysql-connector-java-8.0.33.jar --mysql8


## 2、修改application.yml
```yaml
bigtop:
  manager:
    orm:
      # hibernate/eclipselink(default=eclipselink)
      type: eclipselink

spring:
  application:
    name: bigtop-manager-server
  datasource:
    # driver-class-name: com.mysql.jdbc.Driver（mysql8以下类名）
    driver-class-name: com.mysql.cj.jdbc.Driver
    password: root
    type: com.zaxxer.hikari.HikariDataSource
    url: jdbc:mysql://localhost:3306/bigtop_manager
    username: root
    hikari:
      auto-commit: true
      connection-test-query: select 1
      connection-timeout: 30000
      idle-timeout: 600000
      initialization-fail-timeout: 1
      leak-detection-threshold: 0
      maximum-pool-size: 50
      minimum-idle: 5
      pool-name: BigtopManagerHikariCP
      validation-timeout: 3000

  jackson:
    default-property-inclusion: non-null

  jpa:
    show-sql: true
    properties:
      eclipselink:
        ddl-generation: create-or-extend-tables
        weaving: false
        persistence-context:
          persist-on-commit: false
        cache:
          shared:
            default: false
        logging:
          connection: false
          logger: org.eclipse.persistence.logging.slf4j.SLF4JLogger
          parameters: true
          session: false
          thread: false
          timestamp: false
          level:
            connection: FINE
            sql: FINEST
            jpa: FINER
```

## 4、初始化DDL
```bash
bigtop-manager-server/src/main/resources/ddl/MySQL-DDL-CREATE.sql
```


## 4、启动程序
```bash
./bin/start.sh
```


## 参考
- [驱动下载](https://dev.mysql.com/downloads/connector/j/)