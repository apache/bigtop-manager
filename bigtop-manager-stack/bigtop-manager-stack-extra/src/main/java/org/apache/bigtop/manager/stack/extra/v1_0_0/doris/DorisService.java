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

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DorisService {
    public static void registerBe(DorisParams dorisParams) {
        log.info("registerBe: ip {}, hostname {}", dorisParams.ip(), dorisParams.hostname());
        List<String> beList = DorisService.getBeList(dorisParams);
        if (beList.isEmpty() || beList.contains(dorisParams.hostname())) {
            log.info("No FE alive or Doris BE {} already registered", dorisParams.hostname());
            return;
        }
        String aliveFe = getAliveFe(dorisParams);
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
        log.info("getBeList: ip {}, hostname {}", dorisParams.ip(), dorisParams.hostname());
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            log.error("No FE alive!!!");
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

    public static String getAliveFe(DorisParams dorisParams) {
        log.info("getAliveFe: ip {}, hostname {}", dorisParams.ip(), dorisParams.hostname());
        int dorisFeHttpPort = dorisParams.dorisFeHttpPort();
        List<String> feHosts = dorisParams.dorisFeHosts();
        String aliveFe = null;

        MessageFormat messageFormat =
                new MessageFormat("curl -s -o /dev/null -w '%{http_code}' http://{0}:{1,number,#}/api/bootstrap");
        try {
            for (String host : feHosts) {
                String cmd = messageFormat.format(new Object[]{host, dorisFeHttpPort});
                ShellResult shellResult;
                int attempts = 0;
                while (attempts < 5) {
                    shellResult = LinuxOSUtils.execCmd(cmd);
                    if (shellResult.getExitCode() == 0
                            && StringUtils.equals(shellResult.getOutput().trim(), "200")) {
                        aliveFe = host;
                        break;
                    }
                    attempts++;
                    try {
                        log.info("Retry {} to connect {}", attempts, host);
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

    /**
     * Return MasterFeHost and AllFeHost
     *
     * @param dorisParams DorisParams
     * @return (MasterFeHost, AllFeHost)
     */
    public static Pair<String, List<String>> getMasterAndFeList(DorisParams dorisParams) {
        log.info("getMasterAndFeList: ip {}, hostname {}", dorisParams.ip(), dorisParams.hostname());
        String aliveFe = getAliveFe(dorisParams);
        String masterHost = dorisParams.dorisFeHosts().get(0);
        if (aliveFe == null) {
            return Pair.of(masterHost, List.of());
        }
        List<String> feList = new ArrayList<>();
        String sql = "SHOW FRONTENDS";
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", "mysql", dorisParams.dorisFeQueryPort());
        try {
            dorisTool.connect();
            try (ResultSet resultSet = dorisTool.executeQuery(sql)) {
                while (resultSet.next()) {
                    String host = resultSet.getString("Host");
                    boolean isMaster = resultSet.getBoolean("IsMaster");
                    feList.add(host);
                    if (isMaster) {
                        masterHost = host;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing SQL query: {}", sql, e);
        } finally {
            dorisTool.close();
        }
        return Pair.of(masterHost, feList);
    }

    public static void registerFollower(DorisParams dorisParams) {
        log.info("registerFollower: ip {}, hostname {}", dorisParams.ip(), dorisParams.hostname());
        Pair<String, List<String>> masterAndFeList = getMasterAndFeList(dorisParams);
        List<String> feList = masterAndFeList.getRight();
        String feMaster = masterAndFeList.getLeft();

        if (feList.isEmpty() || feList.contains(dorisParams.hostname())) {
            log.info("No FE alive or Doris FE {} already registered", dorisParams.hostname());
        } else {
            DorisTool dorisTool =
                    new DorisTool(feMaster, "root", "", "mysql", dorisParams.dorisFeQueryPort());

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
