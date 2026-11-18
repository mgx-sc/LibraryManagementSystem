package com.librarymanagement;

import com.librarymanagement.dao.ReaderDAO;
import com.librarymanagement.model.Reader;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReaderDAOTest {

    @Test
    void testReaderCRUD() throws SQLException {

        ReaderDAO readerDAO = new ReaderDAO();

        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String testName = "DAO Test " + uniqueId;
        String testPhone = "09" + uniqueId.substring(0, 10);
        String testEmail = "reader-" + uniqueId + "@test.local";

        Reader reader = new Reader();

        reader.setFullName(testName);
        reader.setDateOfBirth(LocalDate.of(2002, 4, 10));
        reader.setPhone(testPhone);
        reader.setEmail(testEmail);
        reader.setAddress("Test Address");

        int readerId = 0;

        try {
            // 1. Kiểm tra thêm độc giả
            assertTrue(readerDAO.insert(reader),
                    "Thêm độc giả thất bại");

            readerId = reader.getReaderId();
            final int insertedReaderId = readerId;

            assertTrue(readerId > 0);

            // 2. Kiểm tra tìm kiếm độc giả
            assertTrue(
                    readerDAO.search(testName).stream()
                            .anyMatch(r ->
                                    r.getReaderId() == insertedReaderId),
                    "Không tìm thấy độc giả vừa thêm"
            );

            // 3. Kiểm tra danh sách toàn bộ độc giả
            assertTrue(
                    readerDAO.findAll().stream()
                            .anyMatch(r ->
                                    r.getReaderId() == insertedReaderId),
                    "Danh sách không chứa độc giả vừa thêm"
            );

            // 4. Kiểm tra danh sách độc giả đang hoạt động
            assertTrue(
                    readerDAO.findAllActive().stream()
                            .anyMatch(r ->
                                    r.getReaderId() == insertedReaderId),
                    "Độc giả mới không xuất hiện trong danh sách hoạt động"
            );

            // 5. Kiểm tra cập nhật thông tin
            reader.setFullName(testName + " Updated");
            reader.setAddress("Updated Test Address");

            assertTrue(readerDAO.update(reader),
                    "Cập nhật độc giả thất bại");

            assertTrue(
                    readerDAO.search(testName + " Updated").stream()
                            .anyMatch(r ->
                                    r.getReaderId() == insertedReaderId),
                    "Thông tin độc giả chưa được cập nhật"
            );

            // 6. Kiểm tra ngừng hoạt động độc giả
            assertTrue(readerDAO.deactivate(readerId),
                    "Ngừng hoạt động độc giả thất bại");

            assertFalse(
                    readerDAO.findAllActive().stream()
                            .anyMatch(r ->
                                    r.getReaderId() == insertedReaderId),
                    "Độc giả đã ngừng hoạt động nhưng vẫn xuất hiện trong danh sách hoạt động"
            );

            // Độc giả vẫn phải tồn tại trong danh sách toàn bộ
            assertTrue(
                    readerDAO.findAll().stream()
                            .anyMatch(r ->
                                    r.getReaderId() == insertedReaderId),
                    "Dữ liệu độc giả đã bị xóa thay vì ngừng hoạt động"
            );

        } finally {
            // Xóa bản ghi kiểm thử, không ảnh hưởng dữ liệu mẫu
            if (readerId > 0) {

                String deleteSql = """
                        DELETE FROM dbo.Reader
                        WHERE ReaderId = ?
                        """;

                try (var connection =
                             com.librarymanagement.config.DatabaseConfig
                                     .getConnection();
                     var ps = connection.prepareStatement(deleteSql)) {

                    ps.setInt(1, readerId);
                    ps.executeUpdate();
                }
            }
        }
    }
}