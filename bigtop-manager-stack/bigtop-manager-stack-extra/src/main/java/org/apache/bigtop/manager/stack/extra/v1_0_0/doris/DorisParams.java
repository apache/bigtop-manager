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
package org.apache.bigtop.manager.stack.extra.v1_0_0.doris;

import org.apache.bigtop.manager.grpc.payload.ComponentCommandPayload;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.core.utils.template.BaseTemplate;
import org.apache.bigtop.manager.stack.extra.param.ExtraParams;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@AutoService(Params.class)
@NoArgsConstructor
public class DorisParams extends ExtraParams {

    private final String limitsConfDir = "/etc/security/limits.d";
    private final String sysctlConfDir = "/etc/sysctl.d";

    public DorisParams(ComponentCommandPayload payload) {
        super(payload);
        globalParamsMap.put("java_home", javaHome());
        globalParamsMap.put("doris_user", user());
        globalParamsMap.put("doris_group", group());
        globalParamsMap.put("doris_home", serviceHome());
        globalParamsMap.put("doris_fe_home", dorisFeHome());
        globalParamsMap.put("doris_be_home", dorisBeHome());
    }

    /*================================FE===================================================*/
    public String dorisFeHome() {
        return serviceHome() + "/fe";
    }

    public String dorisFeBinDir() {
        return dorisFeHome() + "/bin";
    }

    public String dorisFeConfDir() {
        return dorisFeHome() + "/conf";
    }

    public String dorisFeMetaDir() {
        return dorisFeConf().get("meta_dir") != null
                ? BaseTemplate.writeCustomTemplateAsString(
                        globalParamsMap, (String) dorisFeConf().get("meta_dir"))
                : dorisFeHome() + "/doris-meta";
    }

    public String dorisFeLogDir() {
        return dorisEnv().get("doris_fe_log_dir") != null
                ? BaseTemplate.writeCustomTemplateAsString(
                        globalParamsMap, (String) dorisEnv().get("doris_fe_log_dir"))
                : dorisFeHome() + "/log";
    }

    public String dorisFePidDir() {
        return (String) dorisEnv().get("doris_fe_pid_dir");
    }

    public String dorisFePidFile() {
        return dorisFePidDir() + "/fe.pid";
    }

    public List<String> dorisFeHosts() {
        return LocalSettings.componentHosts("doris_fe");
    }

    public int dorisFeHttpPort() {
        return Integer.parseInt(dorisFeConf().get("http_port").toString());
    }

    public int dorisFeEditLogPort() {
        return Integer.parseInt(dorisFeConf().get("edit_log_port").toString());
    }

    public int dorisFeQueryPort() {
        return Integer.parseInt(dorisFeConf().get("query_port").toString());
    }

    public int dorisFeArrowFlightSqlPort() {
        return Integer.parseInt(dorisFeConf().get("fe_arrow_flight_sql_port").toString());
    }

    /*================================FE===================================================*/
    /*================================BE===================================================*/
    public String dorisBeHome() {
        return serviceHome() + "/be";
    }

    public String dorisBeBinDir() {
        return dorisBeHome() + "/bin";
    }

    public String dorisBeConfDir() {
        return dorisBeHome() + "/conf";
    }

    public String dorisBeStorage() {
        return dorisBeConf().get("storage_root_path") != null
                ? BaseTemplate.writeCustomTemplateAsString(
                        globalParamsMap, (String) dorisBeConf().get("storage_root_path"))
                : dorisBeHome() + "/storage";
    }

    public String dorisBeLogDir() {
        return dorisEnv().get("doris_be_log_dir") != null
                ? BaseTemplate.writeCustomTemplateAsString(
                        globalParamsMap, (String) dorisEnv().get("doris_be_log_dir"))
                : dorisBeHome() + "/log";
    }

    public String dorisBePidDir() {
        return (String) dorisEnv().get("doris_be_pid_dir");
    }

    public String dorisBePidFile() {
        return dorisBePidDir() + "/be.pid";
    }

    public int dorisBeHeartbeatPort() {
        return Integer.parseInt(dorisBeConf().get("heartbeat_service_port").toString());
    }

    public int dorisBeArrowFlightSqlPort() {
        return Integer.parseInt(dorisBeConf().get("be_arrow_flight_sql_port").toString());
    }
    /*================================BE===================================================*/

    public String ip() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getServiceName() {
        return "doris";
    }

    public String dorisConf() {
        return (String)
                LocalSettings.configurations(getServiceName(), "doris.conf").get("content");
    }

    public String dorisSysctlConf() {
        return (String) LocalSettings.configurations(getServiceName(), "doris-sysctl.conf")
                .get("content");
    }

    public String dorisFeConfContent() {
        return (String) dorisFeConf().get("content");
    }

    public String dorisBeConfContent() {
        return (String) dorisBeConf().get("content");
    }

    @GlobalParams
    public Map<String, Object> dorisFeConf() {
        return LocalSettings.configurations(getServiceName(), "doris-fe-conf");
    }

    @GlobalParams
    public Map<String, Object> dorisBeConf() {
        return LocalSettings.configurations(getServiceName(), "doris-be-conf");
    }

    @GlobalParams
    public Map<String, Object> dorisEnv() {
        return LocalSettings.configurations(getServiceName(), "doris-env.sh");
    }
}
