package com.librarymanagement.dao;

import com.librarymanagement.config.DatabaseConfig;
import com.librarymanagement.model.Reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class ReaderDAO {

    // Chuyển một dòng ResultSet thành đối tượng Reader
    private Reader mapRow(ResultSet rs) throws SQLException {

        Reader reader = new Reader();

        reader.setReaderId(rs.getInt("ReaderId"));
        reader.setFullName(rs.getString("FullName"));

        Date dateOfBirth = rs.getDate("DateOfBirth");

        reader.setDateOfBirth(
                dateOfBirth == null
                        ? null
                        : dateOfBirth.toLocalDate()
        );

        reader.setPhone(rs.getString("Phone"));
        reader.setEmail(rs.getString("Email"));
        reader.setAddress(rs.getString("Address"));

        Timestamp registeredAt = rs.getTimestamp("RegisteredAt");

        reader.setRegisteredAt(
                registeredAt == null
                        ? null
                        : registeredAt.toLocalDateTime()
        );

        reader.setActive(rs.getBoolean("IsActive"));

        return reader;
    }

    // Lấy toàn bộ độc giả
    public List<Reader> findAll() throws SQLException {

        String sql = """
                SELECT ReaderId, FullName, DateOfBirth,
                       Phone, Email, Address,
                       RegisteredAt, IsActive
                FROM dbo.Reader
                ORDER BY ReaderId
                """;

        List<Reader> readers = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                readers.add(mapRow(rs));
            }
        }

        return readers;
    }

    // Lấy danh sách độc giả đang hoạt động
    public List<Reader> findAllActive() throws SQLException {

        String sql = """
                SELECT ReaderId, FullName, DateOfBirth,
                       Phone, Email, Address,
                       RegisteredAt, IsActive
                FROM dbo.Reader
                WHERE IsActive = 1
                ORDER BY ReaderId
                """;

        List<Reader> readers = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                readers.add(mapRow(rs));
            }
        }

        return readers;
    }

    // Tìm kiếm độc giả theo tên, số điện thoại hoặc email
    public List<Reader> search(String keyword) throws SQLException {

        String sql = """
                SELECT ReaderId, FullName, DateOfBirth,
                       Phone, Email, Address,
                       RegisteredAt, IsActive
                FROM dbo.Reader
                WHERE FullName LIKE ?
                   OR Phone LIKE ?
                   OR Email LIKE ?
                ORDER BY ReaderId
                """;

        String searchValue = "%"
                + (keyword == null ? "" : keyword.trim())
                + "%";

        List<Reader> readers = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    readers.add(mapRow(rs));
                }
            }
        }

        return readers;
    }

    // Thêm độc giả mới
    public boolean insert(Reader reader) throws SQLException {

        String sql = """
                INSERT INTO dbo.Reader (
                    FullName, DateOfBirth,
                    Phone, Email, Address
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, reader.getFullName());

            if (reader.getDateOfBirth() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setDate(
                        2,
                        Date.valueOf(reader.getDateOfBirth())
                );
            }

            if (reader.getPhone() == null
                    || reader.getPhone().isBlank()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, reader.getPhone());
            }

            if (reader.getEmail() == null
                    || reader.getEmail().isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, reader.getEmail());
            }

            ps.setString(5, reader.getAddress());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {
                    reader.setReaderId(keys.getInt(1));
                    return true;
                }
            }

            return false;
        }
    }

    // Cập nhật thông tin độc giả
    public boolean update(Reader reader) throws SQLException {

        String sql = """
                UPDATE dbo.Reader
                SET FullName = ?,
                    DateOfBirth = ?,
                    Phone = ?,
                    Email = ?,
                    Address = ?
                WHERE ReaderId = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, reader.getFullName());

            if (reader.getDateOfBirth() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setDate(
                        2,
                        Date.valueOf(reader.getDateOfBirth())
                );
            }

            if (reader.getPhone() == null
                    || reader.getPhone().isBlank()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, reader.getPhone());
            }

            if (reader.getEmail() == null
                    || reader.getEmail().isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, reader.getEmail());
            }

            ps.setString(5, reader.getAddress());
            ps.setInt(6, reader.getReaderId());

            return ps.executeUpdate() > 0;
        }
    }

    // Ngừng hoạt động độc giả, không xóa dữ liệu vật lý
    public boolean deactivate(int readerId) throws SQLException {

        String sql = """
                UPDATE dbo.Reader
                SET IsActive = 0
                WHERE ReaderId = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, readerId);

            return ps.executeUpdate() > 0;
        }
    }
}