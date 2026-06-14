package com.credo.qa.db;

import com.credo.qa.config.AppConfig;

import java.sql.*;

public class TestResultRepository {

    private static final String CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS test_results (" +
        "id             INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "test_name      TEXT, " +
        "status         TEXT, " +
        "execution_time DATETIME)";

    private static final String SELECT_BY_NAME =
        "SELECT COUNT(*) FROM test_results WHERE test_name = ?";

    private static final String INSERT_SQL =
        "INSERT INTO test_results (test_name, status, execution_time) VALUES (?, ?, ?)";

    private static final String UPDATE_SQL =
        "UPDATE test_results SET status = ?, execution_time = ? WHERE test_name = ?";

    public TestResultRepository() {
        initTable();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(AppConfig.DB_URL);
    }

    private void initTable() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create test_results table", e);
        }
    }

    public void save(TestResult result) {
        try (Connection conn = connect()) {

            // ვამოწმებთ უკვე არსებობს თუ არა ასეთი test_name
            int count = 0;
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME)) {
                ps.setString(1, result.getTestName());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

            if (count > 0) {
                // UPDATE - ჩანაწერი უკვე არსებობს
                try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                    ps.setString(1, result.getStatus());
                    ps.setString(2, result.getExecutionTime());
                    ps.setString(3, result.getTestName());
                    ps.executeUpdate();
                }
            } else {
                // INSERT - ახალი ჩანაწერი
                try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                    ps.setString(1, result.getTestName());
                    ps.setString(2, result.getStatus());
                    ps.setString(3, result.getExecutionTime());
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not save result for: " + result.getTestName(), e);
        }
    }
}
