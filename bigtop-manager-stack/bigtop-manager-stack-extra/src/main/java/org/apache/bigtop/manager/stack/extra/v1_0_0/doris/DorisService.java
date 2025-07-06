package org.apache.bigtop.manager.stack.extra.v1_0_0.doris;

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DorisService {
    public static void registerBe(DorisParams dorisParams) {
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            return;
        }
        String sql = "ALTER SYSTEM ADD BACKEND '?:?'";
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", "mysql", dorisParams.dorisFeHttpPort());
        try {
            dorisTool.connect();
            dorisTool.executeUpdate(sql, dorisParams.hostname(), dorisParams.dorisBeHeartbeatPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dorisTool.close();
        }
    }

    public static List<String> getBeList(DorisParams dorisParams) {
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            return List.of();
        }
        String sql = "SHOW BACKENDS";
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", "mysql", dorisParams.dorisFeHttpPort());
        List<String> beList = new ArrayList<>();
        try {
            dorisTool.connect();

            try (ResultSet resultSet = dorisTool.executeQuery(sql)) {
                while (resultSet.next()) {
                    String host = resultSet.getString("Host");
                    beList.add(host);
                }
            }
        } catch (Exception e) {
            log.error("Error executing SQL query: {}", sql, e);
        } finally {
            dorisTool.close();
        }
        return beList;
    }

    public static String getFeMaster(DorisParams dorisParams) throws SQLException, IOException {
        return getRegisteredFeList(dorisParams).getLeft();
    }

    public static String getAliveFe(DorisParams dorisParams) {
        int dorisFeHttpPort = dorisParams.dorisFeHttpPort();
        List<String> feHosts = dorisParams.dorisFeHosts();
        String aliveFe = null;

        MessageFormat messageFormat =
                new MessageFormat("curl -s -o /dev/null -w'%{{http_code}}' http://{0}:{1}/api/bootstrap");
        try {
            for (String host : feHosts) {
                String cmd = messageFormat.format(new Object[]{host, dorisFeHttpPort});
                ShellResult shellResult = LinuxOSUtils.execCmd(cmd);
                Integer exitCode = shellResult.getExitCode();
                String output = shellResult.getOutput();
                if (exitCode == 0 && "200".equals(output)) {
                    aliveFe = host;
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error checking alive FE hosts: {}", e.getMessage(), e);
        }
        if (aliveFe == null) {
            log.warn("No alive FE host found in the list: {}", feHosts);
        } else {
            log.info("Alive FE host: {}", aliveFe);
        }
        return aliveFe;
    }

    public static Pair<String, List<String>> getRegisteredFeList(DorisParams dorisParams) {
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            return Pair.of(null, List.of());
        }
        List<String> registeredFeHosts = new ArrayList<>();
        String sql = "SHOW FRONTENDS";
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", "mysql", dorisParams.dorisFeHttpPort());
        try {
            dorisTool.connect();
            try (ResultSet resultSet = dorisTool.executeQuery(sql)) {
                while (resultSet.next()) {
                    String host = resultSet.getString("Host");
                    boolean isMaster = resultSet.getBoolean("IsMaster");
                    registeredFeHosts.add(host);
                    if (isMaster) {
                        aliveFe = host;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing SQL query: {}", sql, e);
        } finally {
            dorisTool.close();
        }
        return Pair.of(aliveFe, registeredFeHosts);
    }

    public static void registerFollower(DorisParams dorisParams) {
        Pair<String, List<String>> registerFeList = getRegisteredFeList(dorisParams);
        if (registerFeList.getKey().contains(dorisParams.hostname())) {
            log.info("Doris FE {} already registered", dorisParams.hostname());
        } else {
            DorisTool dorisTool =
                    new DorisTool(registerFeList.getLeft(), "root", "", "mysql", dorisParams.dorisFeHttpPort());

            try {
                dorisTool.connect();

                String sql = "ALTER SYSTEM ADD FOLLOWER '?:?'";

                dorisTool.executeUpdate(sql, dorisParams.hostname(), dorisParams.dorisFeEditLogPort());
            } catch (Exception e) {
                log.error("Error registering follower FE: {}", e.getMessage(), e);
            } finally {
                dorisTool.close();
            }
        }
    }
}
