package com.librarymanagement;

import com.librarymanagement.config.DatabaseConfig;
import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.dao.LoanDAO;
import com.librarymanagement.dao.ReaderDAO;
import com.librarymanagement.model.Book;
import com.librarymanagement.model.Reader;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoanDAOTest {

    @Test
    void testBorrowAndReturnBook() throws SQLException {

        LoanDAO loanDAO = new LoanDAO();
        ReaderDAO readerDAO = new ReaderDAO();
        BookDAO bookDAO = new BookDAO();

        int readerId = 0;
        int bookId = 0;

        // Tạo dữ liệu kiểm thử có giá trị duy nhất
        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "");

        try {
            // 1. Lấy một thể loại sách đang hoạt động
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

                assertTrue(rs.next(), "Không có thể loại sách hoạt động");

                categoryId = rs.getInt("CategoryId");
            }

            // 2. Lấy một nhân viên đang hoạt động
            int staffId;

            String staffSql = """
                    SELECT TOP (1) StaffId
                    FROM dbo.Staff
                    WHERE IsActive = 1
                    ORDER BY StaffId
                    """;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement ps = connection.prepareStatement(staffSql);
                 ResultSet rs = ps.executeQuery()) {

                assertTrue(rs.next(), "Không có nhân viên hoạt động");

                staffId = rs.getInt("StaffId");
            }

            // 3. Tạo độc giả kiểm thử
            Reader reader = new Reader();

            reader.setFullName("Loan Test " + uniqueId);
            reader.setDateOfBirth(LocalDate.of(2002, 4, 10));
            reader.setPhone("09" + uniqueId.substring(0, 10));
            reader.setEmail("loan-" + uniqueId + "@test.local");
            reader.setAddress("Loan Test Address");

            assertTrue(readerDAO.insert(reader));

            readerId = reader.getReaderId();

            final int testReaderId = readerId;

            // 4. Tạo sách kiểm thử với số lượng bằng 1
            Book book = new Book(
                    categoryId,
                    "Loan Test Book " + uniqueId,
                    "Loan Test Author",
                    "Test Publisher",
                    (short) 2020,
                    null,
                    1
            );

            assertTrue(bookDAO.insert(book));

            bookId = book.getBookId();

            final int testBookId = bookId;

            // 5. Kiểm tra mượn sách
            int loanId = loanDAO.borrowBooks(
                    testReaderId,
                    staffId,
                    LocalDate.now().plusDays(7),
                    List.of(testBookId),
                    "LoanDAO test"
            );

            assertTrue(loanId > 0);

            // Sau khi mượn, số lượng có thể mượn phải bằng 0
            assertEquals(0, getAvailableQuantity(testBookId));

            // 6. Lấy LoanDetailId vừa tạo
            int loanDetailId;

            String detailSql = """
                    SELECT LoanDetailId
                    FROM dbo.LoanDetail
                    WHERE LoanId = ? AND BookId = ?
                    """;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement ps = connection.prepareStatement(detailSql)) {

                ps.setInt(1, loanId);
                ps.setInt(2, testBookId);

                try (ResultSet rs = ps.executeQuery()) {

                    assertTrue(rs.next());

                    loanDetailId = rs.getInt("LoanDetailId");
                }
            }

            // 7. Thử mượn lại khi sách đã hết
            // Phải xảy ra SQLException
            assertThrows(
                    SQLException.class,
                    () -> loanDAO.borrowBooks(
                            testReaderId,
                            staffId,
                            LocalDate.now().plusDays(7),
                            List.of(testBookId),
                            "Test insufficient stock"
                    )
            );

            // Giao dịch thất bại phải được rollback
            // Chỉ còn một phiếu mượn đã commit
            assertEquals(1, countLoans(testReaderId));

            // Tồn kho vẫn phải bằng 0
            assertEquals(0, getAvailableQuantity(testBookId));

            // 8. Trả sách
            assertTrue(loanDAO.returnBook(loanDetailId));

            // Sau khi trả, tồn kho phải tăng lên 1
            assertEquals(1, getAvailableQuantity(testBookId));

            // 9. Kiểm tra trạng thái và ngày trả
            String checkReturnSql = """
                    SELECT Status, ReturnDate
                    FROM dbo.LoanDetail
                    WHERE LoanDetailId = ?
                    """;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement ps = connection.prepareStatement(checkReturnSql)) {

                ps.setInt(1, loanDetailId);

                try (ResultSet rs = ps.executeQuery()) {

                    assertTrue(rs.next());

                    assertEquals("RETURNED", rs.getString("Status"));
                    assertNotNull(rs.getDate("ReturnDate"));
                }
            }

            // 10. Không cho trả cùng một sách hai lần
            assertFalse(
                    loanDAO.returnBook(loanDetailId),
                    "Không được trả lại một sách đã được trả"
            );

            // Tồn kho không được tăng thêm lần nữa
            assertEquals(1, getAvailableQuantity(testBookId));

        } finally {

            // Xóa dữ liệu kiểm thử theo đúng thứ tự khóa ngoại
            try (Connection connection = DatabaseConfig.getConnection()) {

                if (readerId > 0) {

                    String deleteDetailsSql = """
                            DELETE ld
                            FROM dbo.LoanDetail ld
                            INNER JOIN dbo.Loan l
                                ON ld.LoanId = l.LoanId
                            WHERE l.ReaderId = ?
                            """;

                    try (PreparedStatement ps =
                                 connection.prepareStatement(deleteDetailsSql)) {

                        ps.setInt(1, readerId);
                        ps.executeUpdate();
                    }

                    String deleteLoansSql = """
                            DELETE FROM dbo.Loan
                            WHERE ReaderId = ?
                            """;

                    try (PreparedStatement ps =
                                 connection.prepareStatement(deleteLoansSql)) {

                        ps.setInt(1, readerId);
                        ps.executeUpdate();
                    }
                }

                if (bookId > 0) {

                    String deleteBookSql = """
                            DELETE FROM dbo.Book
                            WHERE BookId = ?
                            """;

                    try (PreparedStatement ps =
                                 connection.prepareStatement(deleteBookSql)) {

                        ps.setInt(1, bookId);
                        ps.executeUpdate();
                    }
                }

                if (readerId > 0) {

                    String deleteReaderSql = """
                            DELETE FROM dbo.Reader
                            WHERE ReaderId = ?
                            """;

                    try (PreparedStatement ps =
                                 connection.prepareStatement(deleteReaderSql)) {

                        ps.setInt(1, readerId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    // Lấy số lượng sách còn có thể cho mượn
    private int getAvailableQuantity(int bookId) throws SQLException {

        String sql = """
                SELECT AvailableQuantity
                FROM dbo.Book
                WHERE BookId = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(rs.next());

                return rs.getInt("AvailableQuantity");
            }
        }
    }

    // Đếm số phiếu mượn của độc giả
    private int countLoans(int readerId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM dbo.Loan
                WHERE ReaderId = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, readerId);

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(rs.next());

                return rs.getInt(1);
            }
        }
    }
}