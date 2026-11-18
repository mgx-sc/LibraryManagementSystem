
package com.librarymanagement;

import com.librarymanagement.config.DatabaseConfig;
import com.librarymanagement.dao.CategoryDAO;
import com.librarymanagement.model.Category;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDAOTest {

    @Test
    void shouldPerformCategoryOperations() throws SQLException {

        CategoryDAO dao = new CategoryDAO();

        String uniqueName = "DAO_TEST_"
                + UUID.randomUUID().toString().replace("-", "");

        Category category = new Category(
                uniqueName,
                "Temporary category for testing"
        );

        try {
            // 1. Kiểm tra thêm thể loại
            assertTrue(dao.insert(category));

            assertTrue(category.getCategoryId() > 0);

            int categoryId = category.getCategoryId();

            // 2. Kiểm tra đọc thể loại
            List<Category> categories = dao.findAllActive();

            assertTrue(
                    categories.stream()
                            .anyMatch(c -> c.getCategoryId() == categoryId)
            );

            // 3. Kiểm tra cập nhật
            category.setCategoryName(uniqueName + "_UPDATED");
            category.setDescription("Updated description");

            assertTrue(dao.update(category));

            Category updated = dao.findAll()
                    .stream()
                    .filter(c -> c.getCategoryId() == categoryId)
                    .findFirst()
                    .orElseThrow();

            assertEquals(
                    uniqueName + "_UPDATED",
                    updated.getCategoryName()
            );

            // 4. Kiểm tra ngừng hoạt động
            assertTrue(dao.deactivate(categoryId));

            Category deactivated = dao.findAll()
                    .stream()
                    .filter(c -> c.getCategoryId() == categoryId)
                    .findFirst()
                    .orElseThrow();

            assertFalse(deactivated.isActive());

            assertFalse(
                    dao.findAllActive()
                            .stream()
                            .anyMatch(c -> c.getCategoryId() == categoryId)
            );

        } finally {
            // Xóa dữ liệu kiểm thử, không ảnh hưởng dữ liệu thật
            if (category.getCategoryId() > 0) {

                try (Connection connection =
                             DatabaseConfig.getConnection();

                     PreparedStatement ps = connection.prepareStatement(
                             "DELETE FROM dbo.Category WHERE CategoryId = ?"
                     )) {

                    ps.setInt(1, category.getCategoryId());
                    ps.executeUpdate();
                }
            }
        }
    }
}
