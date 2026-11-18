
package com.librarymanagement;

import com.librarymanagement.config.DatabaseConfig;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @Test
    void shouldConnectToSqlServerAndReadCategories() throws SQLException {

        // Tạo kết nối tới SQL Server
        try (Connection connection = DatabaseConfig.getConnection()) {

            // Kiểm tra tên database
            assertEquals(
                    "LibraryManagementDB",
                    connection.getCatalog()
            );

            System.out.println("Ket noi SQL Server thanh cong!");
            System.out.println("Database: " + connection.getCatalog());

            // Kiểm tra tài khoản đăng nhập
            String loginSql =
                    "SELECT SUSER_SNAME() AS LoginName";

            try (PreparedStatement ps =
                         connection.prepareStatement(loginSql);
                 ResultSet rs = ps.executeQuery()) {

                assertTrue(rs.next());

                String loginName = rs.getString("LoginName");

                assertEquals("library_app", loginName);

                System.out.println("Login: " + loginName);
            }

            // Đọc thử 5 thể loại sách
            String categorySql = """
                    SELECT TOP (5) CategoryId, CategoryName
                    FROM dbo.Category
                    ORDER BY CategoryId
                    """;

            int count = 0;

            try (PreparedStatement ps =
                         connection.prepareStatement(categorySql);
                 ResultSet rs = ps.executeQuery()) {

                System.out.println("Danh sach the loai:");

                while (rs.next()) {
                    System.out.println(
                            rs.getInt("CategoryId")
                            + " - "
                            + rs.getString("CategoryName")
                    );

                    count++;
                }
            }

            assertEquals(5, count,
                    "Phai doc duoc 5 the loai sach");
        }
    }
}
