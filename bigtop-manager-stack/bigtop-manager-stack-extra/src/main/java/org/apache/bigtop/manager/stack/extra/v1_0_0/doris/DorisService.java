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

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxOSUtils;

import org.apache.commons.lang3.StringUtils;
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
        String sql = MessageFormat.format(
                "ALTER SYSTEM ADD BACKEND ''{0}:{1,number,#}''",
                dorisParams.hostname(), dorisParams.dorisBeHeartbeatPort());
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", "mysql", dorisParams.dorisFeQueryPort());
        try {
            dorisTool.connect();
            dorisTool.executeUpdate(sql);
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
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", "mysql", dorisParams.dorisFeQueryPort());
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
                new MessageFormat("curl -s -o /dev/null -w '%{http_code}' http://{0}:{1,number,#}/api/bootstrap");
        try {
            for (String host : feHosts) {
                String cmd = messageFormat.format(new Object[] {host, dorisFeHttpPort});
                ShellResult shellResult;
                int attempts = 0;
                while (attempts < 10) {
                    shellResult = LinuxOSUtils.execCmd(cmd);
                    if (shellResult.getExitCode() == 0
                            && StringUtils.equals(shellResult.getOutput().trim(), "200")) {
                        aliveFe = host;
                        break;
                    }
                    attempts++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
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
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", "mysql", dorisParams.dorisFeQueryPort());
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
                    new DorisTool(registerFeList.getLeft(), "root", "", "mysql", dorisParams.dorisFeQueryPort());

            try {
                dorisTool.connect();

                String sql = MessageFormat.format(
                        "ALTER SYSTEM ADD FOLLOWER ''{0}:{1,number,#}''",
                        dorisParams.hostname(), dorisParams.dorisFeEditLogPort());

                dorisTool.executeUpdate(sql);
            } catch (Exception e) {
                log.error("Error registering follower FE: {}", e.getMessage(), e);
            } finally {
                dorisTool.close();
            }
        }
    }
}
