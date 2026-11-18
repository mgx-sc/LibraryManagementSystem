
package com.librarymanagement.dao;

import com.librarymanagement.config.DatabaseConfig;
import com.librarymanagement.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // Chuyển dữ liệu từ ResultSet thành đối tượng Book
    private Book mapRow(ResultSet rs) throws SQLException {

        short yearValue = rs.getShort("PublicationYear");

        Short publicationYear = rs.wasNull()
                ? null
                : yearValue;

        return new Book(
                rs.getInt("BookId"),
                rs.getInt("CategoryId"),
                rs.getString("Title"),
                rs.getString("Author"),
                rs.getString("Publisher"),
                publicationYear,
                rs.getString("ISBN"),
                rs.getInt("TotalQuantity"),
                rs.getInt("AvailableQuantity"),
                rs.getBoolean("IsActive")
        );
    }

    // Lấy toàn bộ danh sách sách
    public List<Book> findAll() throws SQLException {

        String sql = """
                SELECT BookId, CategoryId, Title, Author,
                       Publisher, PublicationYear, ISBN,
                       TotalQuantity, AvailableQuantity, IsActive
                FROM dbo.Book
                ORDER BY BookId
                """;

        List<Book> books = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                books.add(mapRow(rs));
            }
        }

        return books;
    }

    // Tìm kiếm sách theo tên, tác giả, ISBN hoặc nhà xuất bản
    public List<Book> search(String keyword) throws SQLException {

        String sql = """
                SELECT BookId, CategoryId, Title, Author,
                       Publisher, PublicationYear, ISBN,
                       TotalQuantity, AvailableQuantity, IsActive
                FROM dbo.Book
                WHERE Title LIKE ?
                   OR Author LIKE ?
                   OR ISBN LIKE ?
                   OR Publisher LIKE ?
                ORDER BY BookId
                """;

        List<Book> books = new ArrayList<>();

        String searchValue = "%"
                + (keyword == null ? "" : keyword.trim())
                + "%";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        }

        return books;
    }

    // Thêm sách mới
    public boolean insert(Book book) throws SQLException {

        String sql = """
                INSERT INTO dbo.Book (
                    CategoryId, Title, Author, Publisher,
                    PublicationYear, ISBN,
                    TotalQuantity, AvailableQuantity
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, book.getCategoryId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());

            if (book.getPublicationYear() == null) {
                ps.setNull(5, Types.SMALLINT);
            } else {
                ps.setShort(5, book.getPublicationYear());
            }

            ps.setString(6, book.getIsbn());

            ps.setInt(7, book.getTotalQuantity());

            // Khi thêm sách mới, toàn bộ sách đều có sẵn
            ps.setInt(8, book.getTotalQuantity());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setBookId(keys.getInt(1));
                    return true;
                }
            }

            return false;
        }
    }

    // Cập nhật thông tin sách
    public boolean update(Book book) throws SQLException {

        String sql = """
                UPDATE dbo.Book
                SET CategoryId = ?,
                    Title = ?,
                    Author = ?,
                    Publisher = ?,
                    PublicationYear = ?,
                    ISBN = ?,
                    TotalQuantity = ?,
                    AvailableQuantity =
                        ? - (TotalQuantity - AvailableQuantity)
                WHERE BookId = ?
                  AND ? >= (TotalQuantity - AvailableQuantity)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, book.getCategoryId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());

            if (book.getPublicationYear() == null) {
                ps.setNull(5, Types.SMALLINT);
            } else {
                ps.setShort(5, book.getPublicationYear());
            }

            ps.setString(6, book.getIsbn());

            ps.setInt(7, book.getTotalQuantity());
            ps.setInt(8, book.getTotalQuantity());
            ps.setInt(9, book.getBookId());
            ps.setInt(10, book.getTotalQuantity());

            return ps.executeUpdate() > 0;
        }
    }

    // Ngừng sử dụng sách, không xóa vật lý
    public boolean deactivate(int bookId) throws SQLException {

        String sql = """
                UPDATE dbo.Book
                SET IsActive = 0
                WHERE BookId = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bookId);

            return ps.executeUpdate() > 0;
        }
    }
}
