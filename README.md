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


# Bigtop-Manager

Bigtop-Manager is a platform for managing Bigtop components. Inspired by Apache Ambari.

## Prerequisites

JDK: Requires JDK 17 or 21  
Metadata DB: Mariadb or Mysql(8 or above)

### API-DOCS
[swagger-ui](http://localhost:8080/swagger-ui/index.html)

### Compile
```bash
mvn clean package -DskipTests
```

### Developer
1. Create Database which named "bigtop_manager", Configure DB connect name & password, default both are 'root'
2. Run SQL DDL Script at `bigtop-manager-server/src/main/resources/ddl/MySQL-DDL-CREATE.sql`
3. Insert Test SQL Data at `dev-support/example/bigtop_manager/user.sql`
4. Start bigtop-manager-server `bigtop-manager-server/src/main/java/org/apache/bigtop/manager/server/ServerApplication.java`
5. Start bigtop-manager-agent `similar with run bm-server`
6. Start bigtop-manager-ui `configure nodejs environmment, default folder is bigtop-manager-ui/node, then run with package.json`
7. Visit `http://localhost:5173/`, default login user & password are `"admin"`

### How to test a Service
> 1. Login
> 2. Create cluster ->Register host
> 3. Installation Services
> 4. Start Service
> 5. Stop Service

### API Testing
- request `http://localhost:8080/swagger-ui/index.html` to check swagger API Doc
