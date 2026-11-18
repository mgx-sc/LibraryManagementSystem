package com.librarymanagement.dao;

import com.librarymanagement.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.time.LocalDate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoanDAO {

    // Kiểm tra độc giả hoặc nhân viên có đang hoạt động không
    private boolean existsActive(
            Connection connection,
            String sql,
            int id
    ) throws SQLException {

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Tạo phiếu mượn và trả về LoanId
    private int insertLoan(
            Connection connection,
            int readerId,
            int staffId,
            LocalDate dueDate,
            String notes
    ) throws SQLException {

        String sql = """
                INSERT INTO dbo.Loan (
                    ReaderId, StaffId, LoanDate, DueDate, Notes
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, readerId);
            ps.setInt(2, staffId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setDate(4, Date.valueOf(dueDate));
            ps.setString(5, notes);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Không thể tạo phiếu mượn.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Không lấy được LoanId.");
    }

    // Thêm một đầu sách vào phiếu mượn
    private void insertLoanDetail(
            Connection connection,
            int loanId,
            int bookId
    ) throws SQLException {

        String sql = """
                INSERT INTO dbo.LoanDetail (
                    LoanId, BookId, Status
                )
                VALUES (?, ?, 'BORROWING')
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, loanId);
            ps.setInt(2, bookId);

            ps.executeUpdate();
        }
    }

    // Mượn nhiều đầu sách trong cùng một phiếu
    public int borrowBooks(
            int readerId,
            int staffId,
            LocalDate dueDate,
            List<Integer> bookIds,
            String notes
    ) throws SQLException {

        // Kiểm tra dữ liệu đầu vào
        if (readerId <= 0 || staffId <= 0) {
            throw new IllegalArgumentException(
                    "ReaderId và StaffId phải lớn hơn 0."
            );
        }

        if (dueDate == null || dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày hẹn trả không hợp lệ."
            );
        }

        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Phiếu mượn phải có ít nhất một đầu sách."
            );
        }

        if (bookIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException(
                    "BookId không hợp lệ."
            );
        }

        // Không cho mượn trùng một đầu sách trong cùng phiếu
        Set<Integer> uniqueBookIds = new HashSet<>(bookIds);

        if (uniqueBookIds.size() != bookIds.size()) {
            throw new IllegalArgumentException(
                    "Danh sách không được chứa đầu sách trùng lặp."
            );
        }

        if (notes != null && notes.length() > 255) {
            throw new IllegalArgumentException(
                    "Ghi chú không được vượt quá 255 ký tự."
            );
        }

        String checkReaderSql = """
                SELECT 1
                FROM dbo.Reader
                WHERE ReaderId = ? AND IsActive = 1
                """;

        String checkStaffSql = """
                SELECT 1
                FROM dbo.Staff
                WHERE StaffId = ? AND IsActive = 1
                """;

        String decreaseStockSql = """
                UPDATE dbo.Book
                SET AvailableQuantity = AvailableQuantity - 1
                WHERE BookId = ?
                  AND IsActive = 1
                  AND AvailableQuantity > 0
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Kiểm tra độc giả
                if (!existsActive(
                        connection,
                        checkReaderSql,
                        readerId
                )) {
                    throw new SQLException(
                            "Độc giả không tồn tại hoặc đã ngừng hoạt động."
                    );
                }

                // Kiểm tra nhân viên
                if (!existsActive(
                        connection,
                        checkStaffSql,
                        staffId
                )) {
                    throw new SQLException(
                            "Nhân viên không tồn tại hoặc đã ngừng hoạt động."
                    );
                }

                // Tạo phiếu mượn
                int loanId = insertLoan(
                        connection,
                        readerId,
                        staffId,
                        dueDate,
                        notes
                );

                // Xử lý từng đầu sách
                for (int bookId : bookIds) {

                    // Giảm số lượng tồn kho
                    try (PreparedStatement ps =
                                 connection.prepareStatement(
                                         decreaseStockSql
                                 )) {

                        ps.setInt(1, bookId);

                        int affectedRows = ps.executeUpdate();

                        if (affectedRows != 1) {
                            throw new SQLException(
                                    "Sách có ID " + bookId
                                            + " không tồn tại, đã ngừng sử dụng "
                                            + "hoặc không còn bản có thể mượn."
                            );
                        }
                    }

                    // Thêm chi tiết phiếu mượn
                    insertLoanDetail(
                            connection,
                            loanId,
                            bookId
                    );
                }

                // Hoàn tất toàn bộ giao dịch
                connection.commit();

                return loanId;

            } catch (SQLException | RuntimeException ex) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    ex.addSuppressed(rollbackException);
                }

                throw ex;
            }
        }
    }

    // Trả một đầu sách theo LoanDetailId
    public boolean returnBook(int loanDetailId)
            throws SQLException {

        if (loanDetailId <= 0) {
            throw new IllegalArgumentException(
                    "LoanDetailId phải lớn hơn 0."
            );
        }

        String findBookSql = """
                SELECT BookId
                FROM dbo.LoanDetail
                WHERE LoanDetailId = ?
                  AND Status = 'BORROWING'
                  AND ReturnDate IS NULL
                """;

        String updateDetailSql = """
                UPDATE dbo.LoanDetail
                SET Status = 'RETURNED',
                    ReturnDate = ?
                WHERE LoanDetailId = ?
                  AND Status = 'BORROWING'
                  AND ReturnDate IS NULL
                """;

        String increaseStockSql = """
                UPDATE dbo.Book
                SET AvailableQuantity = AvailableQuantity + 1
                WHERE BookId = ?
                  AND AvailableQuantity < TotalQuantity
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Tìm đầu sách thuộc chi tiết phiếu mượn
                int bookId;

                try (PreparedStatement ps =
                             connection.prepareStatement(findBookSql)) {

                    ps.setInt(1, loanDetailId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }

                        bookId = rs.getInt("BookId");
                    }
                }

                // Cập nhật trạng thái trả sách
                try (PreparedStatement ps =
                             connection.prepareStatement(updateDetailSql)) {

                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setInt(2, loanDetailId);

                    if (ps.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                // Tăng lại số lượng sách có thể cho mượn
                try (PreparedStatement ps =
                             connection.prepareStatement(increaseStockSql)) {

                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Không thể cập nhật số lượng sách sau khi trả."
                        );
                    }
                }

                // Hoàn tất giao dịch trả sách
                connection.commit();

                return true;

            } catch (SQLException | RuntimeException ex) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    ex.addSuppressed(rollbackException);
                }

                throw ex;
            }
        }
    }
}