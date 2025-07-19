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

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DorisTool {
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY_MS = 10000; // milliseconds

    private final String jdbcUrl;
    private final String user;
    private final String password;

    public DorisTool(String host, String user, String password, int port) {
        this.jdbcUrl = String.format(
                "jdbc:arrow-flight-sql://%s:%d?useServerPrepStmts=false&cachePrepStmts=true&useSSL=false&useEncryption=false",
                host, port);
        this.user = user;
        this.password = password;
        log.info("Connecting to database: [jdbc:arrow-flight-sql://{}:{}]", host, port);
    }

    public Connection connect() throws Exception {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
                if (connection != null && !connection.isClosed()) {
                    log.info("Successfully connected to Doris");
                    return connection;
                } else {
                    throw new Exception("Connection is null or closed");
                }
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Connection attempt [{}] failed: [{}]", attempt, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Connection interrupted", ie);
                    }
                }
            }
        }

        throw new Exception("Failed to connect after " + MAX_RETRIES + " attempts", lastException);
    }

    public List<Map<String, Object>> executeQuery(String sql) throws Exception {
        log.info("Executing SQL query: [{}]", sql);

        List<Map<String, Object>> resultList = new ArrayList<>();

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                resultList.add(row);
            }

            return resultList;
        }
    }
}
