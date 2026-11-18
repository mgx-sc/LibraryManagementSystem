package com.librarymanagement.dao;

import com.librarymanagement.config.DatabaseConfig;
import com.librarymanagement.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // Lấy toàn bộ thể loại
    public List<Category> findAll() throws SQLException {

        String sql = """
                SELECT CategoryId, CategoryName, Description, IsActive
                FROM dbo.Category
                ORDER BY CategoryId
                """;

        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Category category = new Category();

                category.setCategoryId(rs.getInt("CategoryId"));
                category.setCategoryName(rs.getString("CategoryName"));
                category.setDescription(rs.getString("Description"));
                category.setActive(rs.getBoolean("IsActive"));

                categories.add(category);
            }
        }

        return categories;
    }

    // Lấy các thể loại đang hoạt động
    public List<Category> findAllActive() throws SQLException {

        String sql = """
                SELECT CategoryId, CategoryName, Description, IsActive
                FROM dbo.Category
                WHERE IsActive = 1
                ORDER BY CategoryId
                """;

        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Category category = new Category();

                category.setCategoryId(rs.getInt("CategoryId"));
                category.setCategoryName(rs.getString("CategoryName"));
                category.setDescription(rs.getString("Description"));
                category.setActive(rs.getBoolean("IsActive"));

                categories.add(category);
            }
        }

        return categories;
    }

    // Thêm thể loại
    public boolean insert(Category category) throws SQLException {

        String sql = """
                INSERT INTO dbo.Category (CategoryName, Description)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {
                    category.setCategoryId(keys.getInt(1));
                    return true;
                }
            }

            return false;
        }
    }

    // Cập nhật tên và mô tả, giữ nguyên trạng thái hoạt động
    public boolean update(Category category) throws SQLException {

        String sql = """
                UPDATE dbo.Category
                SET CategoryName = ?,
                    Description = ?
                WHERE CategoryId = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryId());

            return ps.executeUpdate() > 0;
        }
    }

    // Ngừng sử dụng thể loại
    public boolean deactivate(int categoryId) throws SQLException {

        String sql = """
                UPDATE dbo.Category
                SET IsActive = 0
                WHERE CategoryId = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            return ps.executeUpdate() > 0;
        }
    }
}