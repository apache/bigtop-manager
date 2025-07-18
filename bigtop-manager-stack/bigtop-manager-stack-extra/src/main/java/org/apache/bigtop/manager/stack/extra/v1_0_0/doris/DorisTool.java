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
import java.util.stream.Collectors;

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
        log.info("Connecting to database... {}", jdbcUrl);
    }

    public Connection connect() throws SQLException {
        int attempt = 0;
        SQLException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
                log.info("Successfully connected to Doris");
                return connection;
            } catch (SQLException e) {
                lastException = e;
                attempt++;
                log.warn("Connection attempt {} failed: {}", attempt, e.getMessage());

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

        throw new SQLException("Failed to connect after " + MAX_RETRIES + " attempts", lastException);
    }

    public List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        log.info("Executing SQL query: {}", sql);

        List<Map<String, Object>> resultList = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            // 获取结果集元数据
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 处理结果集
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
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.debug("Error closing resource", e);
            }
        }
    }

    public <T> List<T> executeQuery(String sql, RowMapper<T> mapper) throws SQLException {
        List<Map<String, Object>> rawResults = executeQuery(sql);
        return rawResults.stream().map(mapper::mapRow).collect(Collectors.toList());
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(Map<String, Object> row);
    }
}
