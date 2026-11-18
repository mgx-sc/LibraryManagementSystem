package com.librarymanagement;

import com.librarymanagement.config.DatabaseConfig;
import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.model.Book;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookDAOTest {

    @Test
    void testBookCRUD() throws SQLException {

        BookDAO bookDAO = new BookDAO();

        // Lấy một CategoryId đang hoạt động trong database
        int categoryId;

        String categorySql = """
                SELECT TOP (1) CategoryId
                FROM dbo.Category
                WHERE IsActive = 1
                ORDER BY CategoryId
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(categorySql);
             ResultSet rs = ps.executeQuery()) {

            assertTrue(rs.next(), "Database chưa có thể loại sách");

            categoryId = rs.getInt("CategoryId");
        }

        // Tạo sách kiểm thử với tên duy nhất
        String testTitle = "DAO Test " + UUID.randomUUID();

        Book book = new Book(
                categoryId,
                testTitle,
                "DAO Test Author",
                "Test Publisher",
                (short) 2020,
                null,
                3
        );

        int bookId = 0;

        try {
            // 1. Kiểm tra thêm sách
            assertTrue(bookDAO.insert(book));

            bookId = book.getBookId();
            final int insertedBookId = bookId;

            assertTrue(bookId > 0);

            // 2. Kiểm tra tìm kiếm sách
            assertTrue(
                    bookDAO.search(testTitle).stream()
                            .anyMatch(b -> b.getBookId() == insertedBookId),
                    "Không tìm thấy sách vừa thêm"
            );

            // 3. Kiểm tra lấy toàn bộ danh sách
            assertTrue(
                    bookDAO.findAll().stream()
                            .anyMatch(b -> b.getBookId() == insertedBookId),
                    "Danh sách không chứa sách vừa thêm"
            );

            // 4. Giả lập một bản sách đang được mượn
            // Tổng số lượng = 3, còn lại = 2
            String simulateBorrowSql = """
                    UPDATE dbo.Book
                    SET AvailableQuantity = 2
                    WHERE BookId = ?
                    """;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement ps =
                         connection.prepareStatement(simulateBorrowSql)) {

                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            // 5. Tăng tổng số lượng từ 3 lên 5
            // Số sách đang mượn vẫn là 1
            book.setTitle(testTitle + " Updated");
            book.setTotalQuantity(5);

            assertTrue(bookDAO.update(book));

            // Kiểm tra tồn kho sau khi cập nhật
            String checkStockSql = """
                    SELECT TotalQuantity, AvailableQuantity
                    FROM dbo.Book
                    WHERE BookId = ?
                    """;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement ps =
                         connection.prepareStatement(checkStockSql)) {

                ps.setInt(1, bookId);

                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());

                    assertEquals(5, rs.getInt("TotalQuantity"));
                    assertEquals(4, rs.getInt("AvailableQuantity"));
                }
            }

            // 6. Không cho giảm tổng số lượng xuống dưới số đang mượn
            book.setTotalQuantity(0);

            assertFalse(
                    bookDAO.update(book),
                    "Không được giảm tổng số lượng xuống thấp hơn số sách đang mượn"
            );

            // 7. Kiểm tra ngừng sử dụng sách
            assertTrue(bookDAO.deactivate(bookId));

            String checkActiveSql = """
                    SELECT IsActive
                    FROM dbo.Book
                    WHERE BookId = ?
                    """;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement ps =
                         connection.prepareStatement(checkActiveSql)) {

                ps.setInt(1, bookId);

                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertFalse(rs.getBoolean("IsActive"));
                }
            }

        } finally {
            // Xóa sách kiểm thử để không làm thay đổi dữ liệu mẫu
            if (bookId > 0) {

                String deleteSql = """
                        DELETE FROM dbo.Book
                        WHERE BookId = ?
                        """;

                try (Connection connection = DatabaseConfig.getConnection();
                     PreparedStatement ps =
                             connection.prepareStatement(deleteSql)) {

                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
            }
        }
    }
}