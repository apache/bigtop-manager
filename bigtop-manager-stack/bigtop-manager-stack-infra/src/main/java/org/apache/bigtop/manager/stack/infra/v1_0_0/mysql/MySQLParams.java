package org.apache.bigtop.manager.stack.infra.v1_0_0.mysql;

import com.google.auto.service.AutoService;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.common.message.entity.payload.CommandPayload;
import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.stack.core.annotations.GlobalParams;
import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.LocalSettings;
import org.apache.bigtop.manager.stack.infra.param.InfraParams;

import java.util.Map;

@Getter
@Slf4j
@AutoService(Params.class)
@NoArgsConstructor
public class MySQLParams extends InfraParams {

    private String mysqlLogDir = "/var/log/mysql";
    private String mysqlPidDir = "/var/run/mysql";

    private String rootPassword;
    private String myCnfContent;

    public MySQLParams(CommandPayload commandPayload) {
        super(commandPayload);

        globalParamsMap.put("mysql_home", serviceHome());
        globalParamsMap.put("mysql_conf_dir", confDir());
        globalParamsMap.put("mysql_user", user());
        globalParamsMap.put("mysql_group", group());

        common();
    }

    public Map<String, Object> common() {
        Map<String, Object> common = LocalSettings.configurations(getServiceName(), "common");
        rootPassword = common.get("root_password").toString();
        return common;
    }

    @GlobalParams
    public Map<String, Object> myCnf() {
        Map<String, Object> myCnf = LocalSettings.configurations(getServiceName(), "my.cnf");
        mysqlPidDir = myCnf.get("mysql_pid_dir").toString();
        mysqlLogDir = myCnf.get("mysql_log_dir").toString();
        myCnfContent = myCnf.get("content").toString();
        return myCnf;
    }

    @Override
    public String getServiceName() {
        return "mysql";
    }
}
