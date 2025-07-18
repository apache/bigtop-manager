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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class DorisService {
    public static void registerBe(DorisParams dorisParams) {
        String hostname = dorisParams.hostname();
        log.info("registerBe: ip {}, hostname {}", dorisParams.ip(), hostname);
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            log.error("registerBe: No FE alive!!!");
            return;
        }

        List<String> beList = DorisService.getBeList(dorisParams);
        if (beList.contains(hostname)) {
            log.info("Doris BE {} already registered", hostname);
            return;
        }
        String sql = MessageFormat.format(
                "ALTER SYSTEM ADD BACKEND ''{0}:{1,number,#}''", hostname, dorisParams.dorisBeHeartbeatPort());
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", dorisParams.dorisFeArrowFlightSqlPort());
        try {
            dorisTool.executeQuery(sql);
        } catch (Exception e) {
            log.error("Error registering Doris BE {} ", hostname, e);
        }
    }

    public static List<String> getBeList(DorisParams dorisParams) {
        log.info("getBeList: ip {}, hostname {}", dorisParams.ip(), dorisParams.hostname());
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            log.error("getBeList: No FE alive!!!");
            return List.of();
        }
        String sql = "SELECT * FROM BACKENDS()";
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", dorisParams.dorisFeArrowFlightSqlPort());
        List<String> beList = new ArrayList<>();
        try {
            beList = dorisTool.executeQuery(sql).stream()
                    .map(map -> (String) map.get("_table_valued_function_backends.Host"))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error executing SQL query: {}, {}", sql, e.getMessage());
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
                String cmd = messageFormat.format(new Object[] {host, dorisFeHttpPort});
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
            log.error("Error checking alive FE hosts: {} {}", e.getMessage(), e.getMessage());
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
        AtomicReference<String> masterHost =
                new AtomicReference<>(dorisParams.dorisFeHosts().get(0));
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            log.warn("getMasterAndFeList: No alive FE ");
            return Pair.of(masterHost.get(), List.of());
        }
        List<String> feList = new ArrayList<>();
        String sql = "SELECT * FROM FRONTENDS()";
        DorisTool dorisTool = new DorisTool(aliveFe, "root", "", dorisParams.dorisFeArrowFlightSqlPort());
        try {
            dorisTool.executeQuery(sql).stream()
                    .map(map -> {
                        String host = (String) map.get("_table_valued_function_frontends.Host");
                        boolean isMaster = (Boolean) map.get("_table_valued_function_frontends.IsMaster");
                        if (isMaster) {
                            masterHost.set(host);
                        }
                        return host;
                    })
                    .forEach(feList::add);
        } catch (Exception e) {
            log.error("Error executing SQL query: {} {}", sql, e.getMessage());
        }
        return Pair.of(masterHost.get(), feList);
    }

    public static void registerFollower(DorisParams dorisParams, String hostname) {
        log.info("registerFollower: hostname {}", hostname);
        String aliveFe = getAliveFe(dorisParams);
        if (aliveFe == null) {
            log.error("registerFollower: No FE alive!!!");
            return;
        }

        Pair<String, List<String>> masterAndFeList = getMasterAndFeList(dorisParams);
        List<String> feList = masterAndFeList.getRight();
        String feMaster = masterAndFeList.getLeft();

        if (feList.contains(hostname)) {
            log.info("Doris FE {} already registered", hostname);
        } else {
            DorisTool dorisTool = new DorisTool(feMaster, "root", "", dorisParams.dorisFeArrowFlightSqlPort());

            try {
                String sql = MessageFormat.format(
                        "ALTER SYSTEM ADD FOLLOWER ''{0}:{1,number,#}''", hostname, dorisParams.dorisFeEditLogPort());

                dorisTool.executeQuery(sql);
            } catch (Exception e) {
                log.error("Error registering Doris Follower: {}", e.getMessage());
            }
        }
    }
}
