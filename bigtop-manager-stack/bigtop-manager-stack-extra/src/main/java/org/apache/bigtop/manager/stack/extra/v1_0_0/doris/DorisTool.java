package org.apache.bigtop.manager.stack.extra.v1_0_0.doris;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class DorisTool {
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY_MS = 10000; // milliseconds

    private final String jdbcUrl;
    private final String user;
    private final String password;
    private Connection connection;

    public DorisTool(String host, String user, String password, String database, int port) {
        this.jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC",
                host, port, database);
        this.user = user;
        this.password = password;
        log.info("Connecting to database... {}", jdbcUrl);
    }

    public synchronized void connect() throws SQLException {
        if (isConnected()) {
            return;
        }

        int attempt = 0;
        SQLException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                connection = DriverManager.getConnection(jdbcUrl, user, password);
                log.info("Successfully connected to Doris");
                return;
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

    public synchronized boolean isConnected() throws SQLException {
        return connection != null && !connection.isClosed() && connection.isValid(5);
    }

    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.close();
                log.info("Database connection closed");
            }
        } catch (SQLException e) {
            log.error("Error closing connection", e);
        } finally {
            connection = null;
        }
    }

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, params);
        return statement.executeQuery();
    }

    public int executeUpdate(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, params);
        return statement.executeUpdate();
    }

    private void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
