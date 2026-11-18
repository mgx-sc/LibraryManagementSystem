# Library Management System

Ứng dụng quản lý thư viện desktop được xây dựng bằng Java Swing, JDBC và Microsoft SQL Server.

## 1. Công nghệ sử dụng

- Java 21
- Java Swing
- Apache Maven 3.10
- Microsoft SQL Server
- Microsoft SQL Server JDBC Driver
- JUnit 5

## 2. Chức năng chính

### Dashboard
- Thống kê đầu sách và độc giả.
- Thống kê sách đang được mượn và quá hạn.
- Theo dõi tình hình kho sách.
- Hiển thị các phiếu mượn gần đây.

### Quản lý thể loại
- Thêm, cập nhật thể loại.
- Ngừng sử dụng thể loại.
- Hiển thị danh sách thể loại.

### Quản lý sách
- Thêm, cập nhật và tìm kiếm sách.
- Tìm kiếm theo tên, tác giả, ISBN và nhà xuất bản.
- Quản lý số lượng sách.
- Ngừng sử dụng sách.
- Bảo toàn số lượng sách đang được mượn khi cập nhật.

### Quản lý độc giả
- Thêm và cập nhật thông tin độc giả.
- Tìm kiếm độc giả.
- Ngừng hoạt động độc giả.

### Quản lý mượn và trả sách
- Lập phiếu mượn.
- Mượn nhiều đầu sách trong một phiếu.
- Trả sách.
- Theo dõi sách đang mượn và quá hạn.
- Tự động cập nhật số lượng tồn kho.
- Sử dụng SQL Transaction để đảm bảo tính nhất quán dữ liệu.

## 3. Cấu trúc dự án

```text
LibraryManagementSystem/
├── database/
│   └── LibraryManagementDB_Final.sql
├── src/
│   ├── main/
│   │   ├── java/com/librarymanagement/
│   │   │   ├── config/
│   │   │   ├── dao/
│   │   │   ├── model/
│   │   │   ├── service/
│   │   │   ├── util/
│   │   │   ├── view/
│   │   │   └── App.java
│   │   └── resources/
│   └── test/
│       └── java/com/librarymanagement/
├── .gitignore
├── pom.xml
└── README.md