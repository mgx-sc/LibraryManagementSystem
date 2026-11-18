
package com.librarymanagement.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {

    private static final String URL =
            getRequiredEnv("LIBRARY_DB_URL");

    private static final String USER =
            getRequiredEnv("LIBRARY_DB_USER");

    private static final String PASSWORD =
            getRequiredEnv("LIBRARY_DB_PASSWORD");

    // Không cho phép tạo đối tượng DatabaseConfig.
    private DatabaseConfig() {
    }

    // Lấy giá trị biến môi trường và kiểm tra dữ liệu.
    private static String getRequiredEnv(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing environment variable: " + name
            );
        }

        return value;
    }

    // Tạo và trả về kết nối tới SQL Server.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
