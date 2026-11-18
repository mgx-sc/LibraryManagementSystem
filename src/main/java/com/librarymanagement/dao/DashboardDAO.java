package com.librarymanagement.dao;

import com.librarymanagement.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public record Statistics(
            int activeBookTitles,
            int totalCopies,
            int availableCopies,
            int activeReaders,
            int activeCategories,
            int borrowingCount,
            int overdueCount
    ) {
    }

    public record RecentLoan(
            int loanId,
            String readerName,
            LocalDate loanDate,
            LocalDate dueDate,
            int bookCount
    ) {
    }

    // Lấy số liệu thống kê
    public Statistics getStatistics() throws SQLException {

        String sql = """
                SELECT
                    (SELECT COUNT(*)
                     FROM dbo.Book
                     WHERE IsActive = 1) AS ActiveBookTitles,

                    (SELECT COALESCE(SUM(TotalQuantity), 0)
                     FROM dbo.Book
                     WHERE IsActive = 1) AS TotalCopies,

                    (SELECT COALESCE(SUM(AvailableQuantity), 0)
                     FROM dbo.Book
                     WHERE IsActive = 1) AS AvailableCopies,

                    (SELECT COUNT(*)
                     FROM dbo.Reader
                     WHERE IsActive = 1) AS ActiveReaders,

                    (SELECT COUNT(*)
                     FROM dbo.Category
                     WHERE IsActive = 1) AS ActiveCategories,

                    (SELECT COUNT(*)
                     FROM dbo.LoanDetail
                     WHERE Status = 'BORROWING') AS BorrowingCount,

                    (SELECT COUNT(*)
                     FROM dbo.LoanDetail ld
                     INNER JOIN dbo.Loan l
                         ON ld.LoanId = l.LoanId
                     WHERE ld.Status = 'BORROWING'
                       AND l.DueDate < CAST(GETDATE() AS DATE)
                    ) AS OverdueCount
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                return new Statistics(
                        rs.getInt("ActiveBookTitles"),
                        rs.getInt("TotalCopies"),
                        rs.getInt("AvailableCopies"),
                        rs.getInt("ActiveReaders"),
                        rs.getInt("ActiveCategories"),
                        rs.getInt("BorrowingCount"),
                        rs.getInt("OverdueCount")
                );
            }
        }

        throw new SQLException("Không thể lấy dữ liệu thống kê.");
    }

    // Lấy 5 phiếu mượn gần đây
    public List<RecentLoan> findRecentLoans() throws SQLException {

        String sql = """
                SELECT TOP (5)
                    l.LoanId,
                    r.FullName AS ReaderName,
                    l.LoanDate,
                    l.DueDate,
                    COUNT(ld.LoanDetailId) AS BookCount
                FROM dbo.Loan l
                INNER JOIN dbo.Reader r
                    ON l.ReaderId = r.ReaderId
                LEFT JOIN dbo.LoanDetail ld
                    ON l.LoanId = ld.LoanId
                GROUP BY
                    l.LoanId,
                    r.FullName,
                    l.LoanDate,
                    l.DueDate
                ORDER BY
                    l.LoanDate DESC,
                    l.LoanId DESC
                """;

        List<RecentLoan> loans = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                loans.add(new RecentLoan(
                        rs.getInt("LoanId"),
                        rs.getString("ReaderName"),
                        rs.getDate("LoanDate").toLocalDate(),
                        rs.getDate("DueDate").toLocalDate(),
                        rs.getInt("BookCount")
                ));
            }
        }

        return loans;
    }
}