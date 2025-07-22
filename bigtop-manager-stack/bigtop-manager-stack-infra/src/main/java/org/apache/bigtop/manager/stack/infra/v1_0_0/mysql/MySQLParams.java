/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.stack.infra.v1_0_0.mysql;

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.infra.param.InfraParams;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class MySQLParams extends InfraParams {

    private String mysqlLogDir;
    private String mysqlPidDir;
    private String mysqlDataDir;

    private String rootPassword;
    private String myCnfContent;

    public MySQLParams(ComponentCommandPayload componentCommandPayload) {
        super(componentCommandPayload);

        globalParamsMap.put("mysql_home", serviceHome());
        globalParamsMap.put("mysql_conf_dir", confDir());
        globalParamsMap.put("mysql_user", user());
        globalParamsMap.put("mysql_group", group());

        common();
    }

    @Override
    public void initGlobalParams() {
        super.initGlobalParams();

        Map<String, Object> map = getGlobalParamsMap();
        mysqlPidDir = map.get("mysql_pid_dir").toString();
        mysqlLogDir = map.get("mysql_log_dir").toString();
        mysqlDataDir = map.get("mysql_data_dir").toString();
    }

    public Map<String, Object> common() {
        Map<String, Object> common = LocalSettings.configurations(getServiceName(), "common");
        rootPassword = common.get("root_password").toString();
        return common;
    }

    @GlobalParams
    public Map<String, Object> myCnf() {
        Map<String, Object> myCnf = LocalSettings.configurations(getServiceName(), "my.cnf");
        myCnfContent = myCnf.get("content").toString();
        return myCnf;
    }

    @Override
    public RepoInfo repo() {
        return LocalSettings.repo("mysql");
    }

    @Override
    public String getServiceName() {
        return "mysql";
    }
}
