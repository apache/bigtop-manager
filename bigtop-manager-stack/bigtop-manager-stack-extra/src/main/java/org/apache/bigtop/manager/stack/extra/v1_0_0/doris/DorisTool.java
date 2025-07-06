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
    private static final int RETRY_DELAY = 10000; // milliseconds

    private final String host;
    private final String user;
    private final String password;
    private final String database;
    private final int port;
    private Connection connection;

    public DorisTool(String host, String user, String password, String database, int port) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
        this.port = port;
        this.connection = null;
    }

    public void connect() throws SQLException, ClassNotFoundException {
        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        int attempt = 0;
        while (true) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
                return;
            } catch (SQLException | ClassNotFoundException e) {
                attempt++;
                if (attempt > MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Retry connect interrupt", ie);
                }
            }
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("Database connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
