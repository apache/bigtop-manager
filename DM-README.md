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

# 达梦数据库支持

## 1、下载相关jar包
- byte-buddy-1.14.5.jar
- DmDialect-for-hibernate6.1.jar
- DmJdbcDriver18-8.1.2.192.jar
- hibernate-commons-annotations-6.0.6.Final.jar
- hibernate-core-6.2.5.Final.jar
- jakarta.inject-api-2.0.1.jar
- jakarta.transaction-api-2.0.1.jar

## 2、修改application.yml
```yaml
bigtop:
  manager:
    orm:
      # hibernate/eclipselink(default=eclipselink)
      type: hibernate

spring:
  datasource:
    driver-class-name: dm.jdbc.driver.DmDriver
    username: SYSDBA
    password: SYSDBA
    url: jdbc:dm://localhost:5236?schema=bigtop_manager&compatibleMode=mysql&ignoreCase=true&characterEncoding=PG_UTF8

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        show_sql: true
        format_sql: true
        dialect: org.hibernate.dialect.DmDialect

```

## 4、初始化DDL
```bash
bigtop-manager-server/src/main/resources/ddl/DaMeng-DDL-CREATE.sql
```


## 4、启动程序
```bash
./bin/start.sh
```


## 参考
- [驱动下载](https://eco.dameng.com/download/)
- [从 MySQL 移植到 DM](https://eco.dameng.com/document/dm/zh-cn/start/mysql_dm.html#3.4.1%20%E6%BA%90%E7%AB%AF%20MySQL%20%E5%87%86%E5%A4%87)