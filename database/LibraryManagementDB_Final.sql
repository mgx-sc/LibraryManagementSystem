/* =========================================================
   ĐỒ ÁN: PHẦN MỀM QUẢN LÝ THƯ VIỆN
   DATABASE: LibraryManagementDB
   SQL SERVER 2025
   ========================================================= */


/* =========================================================
   1. TẠO DATABASE
   ========================================================= */

IF DB_ID('LibraryManagementDB') IS NULL
BEGIN
    EXEC('CREATE DATABASE LibraryManagementDB');
END;
GO

USE LibraryManagementDB;
GO


/* =========================================================
   2. BẢNG STAFF
   Lưu thông tin nhân viên và tài khoản đăng nhập
   ========================================================= */

IF OBJECT_ID('Staff', 'U') IS NULL
BEGIN
    CREATE TABLE Staff (
        StaffId INT IDENTITY(1,1) PRIMARY KEY,

        FullName NVARCHAR(100) NOT NULL,

        Username VARCHAR(50) NOT NULL UNIQUE,

        PasswordHash VARCHAR(255) NOT NULL,

        StaffRole VARCHAR(20) NOT NULL
            CONSTRAINT CK_Staff_StaffRole
            CHECK (StaffRole IN ('ADMIN', 'STAFF')),

        IsActive BIT NOT NULL
            CONSTRAINT DF_Staff_IsActive DEFAULT 1,

        CreatedAt DATETIME2 NOT NULL
            CONSTRAINT DF_Staff_CreatedAt DEFAULT SYSDATETIME()
    );
END;
GO


/* =========================================================
   3. BẢNG CATEGORY
   Lưu thể loại sách
   ========================================================= */

IF OBJECT_ID('Category', 'U') IS NULL
BEGIN
    CREATE TABLE Category (
        CategoryId INT IDENTITY(1,1) PRIMARY KEY,

        CategoryName NVARCHAR(100) NOT NULL UNIQUE,

        Description NVARCHAR(255) NULL,

        IsActive BIT NOT NULL
            CONSTRAINT DF_Category_IsActive DEFAULT 1
    );
END;
GO


/* =========================================================
   4. BẢNG BOOK
   Lưu thông tin sách và số lượng
   ========================================================= */

IF OBJECT_ID('Book', 'U') IS NULL
BEGIN
    CREATE TABLE Book (
        BookId INT IDENTITY(1,1) PRIMARY KEY,

        CategoryId INT NOT NULL,

        Title NVARCHAR(200) NOT NULL,

        Author NVARCHAR(150) NOT NULL,

        Publisher NVARCHAR(150) NULL,

        PublicationYear SMALLINT NULL,

        ISBN VARCHAR(20) NULL,

        TotalQuantity INT NOT NULL,

        AvailableQuantity INT NOT NULL,

        IsActive BIT NOT NULL
            CONSTRAINT DF_Book_IsActive DEFAULT 1,

        CONSTRAINT FK_Book_Category
            FOREIGN KEY (CategoryId)
            REFERENCES Category(CategoryId),

        CONSTRAINT CK_Book_PublicationYear
            CHECK (
                PublicationYear IS NULL
                OR PublicationYear BETWEEN 1000 AND 2100
            ),

        CONSTRAINT CK_Book_TotalQuantity
            CHECK (TotalQuantity >= 0),

        CONSTRAINT CK_Book_AvailableQuantity
            CHECK (
                AvailableQuantity >= 0
                AND AvailableQuantity <= TotalQuantity
            )
    );
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_Book_ISBN'
      AND object_id = OBJECT_ID('Book')
)
BEGIN
    CREATE UNIQUE INDEX UX_Book_ISBN
        ON Book(ISBN)
        WHERE ISBN IS NOT NULL;
END;
GO


/* =========================================================
   5. BẢNG READER
   Lưu thông tin độc giả
   ========================================================= */

IF OBJECT_ID('Reader', 'U') IS NULL
BEGIN
    CREATE TABLE Reader (
        ReaderId INT IDENTITY(1,1) PRIMARY KEY,

        FullName NVARCHAR(100) NOT NULL,

        DateOfBirth DATE NULL,

        Phone VARCHAR(15) NULL,

        Email VARCHAR(100) NULL,

        Address NVARCHAR(255) NULL,

        RegisteredAt DATETIME2 NOT NULL
            CONSTRAINT DF_Reader_RegisteredAt
            DEFAULT SYSDATETIME(),

        IsActive BIT NOT NULL
            CONSTRAINT DF_Reader_IsActive DEFAULT 1
    );
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_Reader_Phone'
      AND object_id = OBJECT_ID('Reader')
)
BEGIN
    CREATE UNIQUE INDEX UX_Reader_Phone
        ON Reader(Phone)
        WHERE Phone IS NOT NULL;
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_Reader_Email'
      AND object_id = OBJECT_ID('Reader')
)
BEGIN
    CREATE UNIQUE INDEX UX_Reader_Email
        ON Reader(Email)
        WHERE Email IS NOT NULL;
END;
GO


/* =========================================================
   6. BẢNG LOAN
   Lưu thông tin chung của phiếu mượn
   ========================================================= */

IF OBJECT_ID('Loan', 'U') IS NULL
BEGIN
    CREATE TABLE Loan (
        LoanId INT IDENTITY(1,1) PRIMARY KEY,

        ReaderId INT NOT NULL,

        StaffId INT NOT NULL,

        LoanDate DATE NOT NULL
            CONSTRAINT DF_Loan_LoanDate
            DEFAULT CAST(SYSDATETIME() AS DATE),

        DueDate DATE NOT NULL,

        Notes NVARCHAR(255) NULL,

        CONSTRAINT FK_Loan_Reader
            FOREIGN KEY (ReaderId)
            REFERENCES Reader(ReaderId),

        CONSTRAINT FK_Loan_Staff
            FOREIGN KEY (StaffId)
            REFERENCES Staff(StaffId),

        CONSTRAINT CK_Loan_DueDate
            CHECK (DueDate >= LoanDate)
    );
END;
GO


/* =========================================================
   7. BẢNG LOANDETAIL
   Lưu các sách thuộc từng phiếu mượn
   ========================================================= */

IF OBJECT_ID('LoanDetail', 'U') IS NULL
BEGIN
    CREATE TABLE LoanDetail (
        LoanDetailId INT IDENTITY(1,1) PRIMARY KEY,

        LoanId INT NOT NULL,

        BookId INT NOT NULL,

        ReturnDate DATE NULL,

        Status VARCHAR(20) NOT NULL
            CONSTRAINT DF_LoanDetail_Status
            DEFAULT 'BORROWING',

        Notes NVARCHAR(255) NULL,

        CONSTRAINT FK_LoanDetail_Loan
            FOREIGN KEY (LoanId)
            REFERENCES Loan(LoanId),

        CONSTRAINT FK_LoanDetail_Book
            FOREIGN KEY (BookId)
            REFERENCES Book(BookId),

        CONSTRAINT CK_LoanDetail_Status
            CHECK (
                Status IN (
                    'BORROWING',
                    'RETURNED',
                    'LOST'
                )
            ),

        CONSTRAINT UQ_LoanDetail_Loan_Book
            UNIQUE (LoanId, BookId)
    );
END;
GO


/* =========================================================
   8. DỮ LIỆU MẪU - CATEGORY
   ========================================================= */

IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Văn học'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Văn học',
    N'Tiểu thuyết, truyện ngắn và các tác phẩm văn học'
);


IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Lịch sử'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Lịch sử',
    N'Sách về lịch sử Việt Nam và thế giới'
);


IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Tâm lý học'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Tâm lý học',
    N'Sách về tâm lý, hành vi và tư duy con người'
);


IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Kinh tế'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Kinh tế',
    N'Sách về kinh tế, tài chính và kinh doanh'
);


IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Khoa học'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Khoa học',
    N'Sách khoa học tự nhiên và kiến thức phổ thông'
);


IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Công nghệ thông tin'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Công nghệ thông tin',
    N'Lập trình, cơ sở dữ liệu và công nghệ'
);


IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Thiếu nhi'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Thiếu nhi',
    N'Sách và truyện dành cho thiếu nhi'
);


IF NOT EXISTS (
    SELECT 1 FROM Category
    WHERE CategoryName = N'Kỹ năng sống'
)
INSERT INTO Category (CategoryName, Description)
VALUES (
    N'Kỹ năng sống',
    N'Phát triển bản thân và kỹ năng thực tế'
);
GO


/* =========================================================
   9. DỮ LIỆU MẪU - BOOK
   ========================================================= */

IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Dế Mèn phiêu lưu ký'
)
INSERT INTO Book (
    CategoryId,
    Title,
    Author,
    Publisher,
    PublicationYear,
    ISBN,
    TotalQuantity,
    AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Văn học'),
    N'Dế Mèn phiêu lưu ký',
    N'Tô Hoài',
    N'Kim Đồng',
    1941,
    NULL,
    10,
    10
);


IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Đại Việt sử ký toàn thư'
)
INSERT INTO Book (
    CategoryId, Title, Author, Publisher,
    PublicationYear, ISBN,
    TotalQuantity, AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Lịch sử'),
    N'Đại Việt sử ký toàn thư',
    N'Ngô Sĩ Liên',
    N'Văn học',
    NULL,
    NULL,
    5,
    5
);


IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Tư duy nhanh và chậm'
)
INSERT INTO Book (
    CategoryId, Title, Author, Publisher,
    PublicationYear, ISBN,
    TotalQuantity, AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Tâm lý học'),
    N'Tư duy nhanh và chậm',
    N'Daniel Kahneman',
    N'Thế Giới',
    2011,
    NULL,
    7,
    7
);


IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Cha giàu cha nghèo'
)
INSERT INTO Book (
    CategoryId, Title, Author, Publisher,
    PublicationYear, ISBN,
    TotalQuantity, AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Kinh tế'),
    N'Cha giàu cha nghèo',
    N'Robert T. Kiyosaki',
    N'Trẻ',
    1997,
    NULL,
    6,
    6
);


IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Lược sử thời gian'
)
INSERT INTO Book (
    CategoryId, Title, Author, Publisher,
    PublicationYear, ISBN,
    TotalQuantity, AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Khoa học'),
    N'Lược sử thời gian',
    N'Stephen Hawking',
    N'Trẻ',
    1988,
    NULL,
    5,
    5
);


IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Clean Code'
)
INSERT INTO Book (
    CategoryId, Title, Author, Publisher,
    PublicationYear, ISBN,
    TotalQuantity, AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Công nghệ thông tin'),
    N'Clean Code',
    N'Robert C. Martin',
    N'Prentice Hall',
    2008,
    NULL,
    8,
    8
);


IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Hoàng tử bé'
)
INSERT INTO Book (
    CategoryId, Title, Author, Publisher,
    PublicationYear, ISBN,
    TotalQuantity, AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Thiếu nhi'),
    N'Hoàng tử bé',
    N'Antoine de Saint-Exupéry',
    N'Kim Đồng',
    1943,
    NULL,
    9,
    9
);


IF NOT EXISTS (
    SELECT 1 FROM Book
    WHERE Title = N'Đắc nhân tâm'
)
INSERT INTO Book (
    CategoryId, Title, Author, Publisher,
    PublicationYear, ISBN,
    TotalQuantity, AvailableQuantity
)
VALUES (
    (SELECT CategoryId FROM Category
     WHERE CategoryName = N'Kỹ năng sống'),
    N'Đắc nhân tâm',
    N'Dale Carnegie',
    N'Tổng hợp Thành phố Hồ Chí Minh',
    1936,
    NULL,
    7,
    7
);
GO


/* =========================================================
   10. DỮ LIỆU MẪU - READER
   ========================================================= */

IF NOT EXISTS (
    SELECT 1 FROM Reader
    WHERE Email = 'minhanh@example.com'
)
INSERT INTO Reader (
    FullName, DateOfBirth, Phone, Email, Address
)
VALUES (
    N'Nguyễn Minh Anh',
    '2004-03-15',
    '0912345678',
    'minhanh@example.com',
    N'Hà Nội'
);


IF NOT EXISTS (
    SELECT 1 FROM Reader
    WHERE Email = 'quochuy@example.com'
)
INSERT INTO Reader (
    FullName, DateOfBirth, Phone, Email, Address
)
VALUES (
    N'Trần Quốc Huy',
    '2003-08-21',
    '0923456789',
    'quochuy@example.com',
    N'Hà Nội'
);


IF NOT EXISTS (
    SELECT 1 FROM Reader
    WHERE Email = 'thutrang@example.com'
)
INSERT INTO Reader (
    FullName, DateOfBirth, Phone, Email, Address
)
VALUES (
    N'Lê Thu Trang',
    '2005-01-10',
    '0934567890',
    'thutrang@example.com',
    N'Bắc Ninh'
);


IF NOT EXISTS (
    SELECT 1 FROM Reader
    WHERE Email = 'duclong@example.com'
)
INSERT INTO Reader (
    FullName, DateOfBirth, Phone, Email, Address
)
VALUES (
    N'Phạm Đức Long',
    '2002-11-05',
    '0945678901',
    'duclong@example.com',
    N'Hải Phòng'
);


IF NOT EXISTS (
    SELECT 1 FROM Reader
    WHERE Email = 'ngocmai@example.com'
)
INSERT INTO Reader (
    FullName, DateOfBirth, Phone, Email, Address
)
VALUES (
    N'Hoàng Ngọc Mai',
    '2004-06-27',
    '0956789012',
    'ngocmai@example.com',
    N'Hà Nam'
);
GO


/* =========================================================
   11. DỮ LIỆU MẪU - STAFF
   Username: admin
   Mật khẩu demo: Admin@123
   Database chỉ lưu PBKDF2 hash
   ========================================================= */

IF NOT EXISTS (
    SELECT 1 FROM Staff
    WHERE Username = 'admin'
)
INSERT INTO Staff (
    FullName,
    Username,
    PasswordHash,
    StaffRole
)
VALUES (
    N'Quản trị viên',
    'admin',
    'pbkdf2_sha256$120000$kPXEmDn1iGcgLdouI8Vblg==$2xt190izN9J0rw7jUnRtbsZl5AsPFQbD8O554bZzCL0=',
    'ADMIN'
);
GO


/* =========================================================
   12. DỮ LIỆU MẪU - PHIẾU MƯỢN
   ========================================================= */

DECLARE @ReaderId INT;
DECLARE @StaffId INT;
DECLARE @ReturnedBookId INT;
DECLARE @BorrowingBookId INT;
DECLARE @SampleLoanId INT;

SELECT @ReaderId = ReaderId
FROM Reader
WHERE Email = 'minhanh@example.com';

SELECT @StaffId = StaffId
FROM Staff
WHERE Username = 'admin';

SELECT @ReturnedBookId = BookId
FROM Book
WHERE Title = N'Dế Mèn phiêu lưu ký';

SELECT @BorrowingBookId = BookId
FROM Book
WHERE Title = N'Tư duy nhanh và chậm';


IF NOT EXISTS (
    SELECT 1
    FROM Loan
    WHERE ReaderId = @ReaderId
      AND LoanDate = '2026-10-28'
      AND Notes = N'Phiếu mượn mẫu'
)
BEGIN
    BEGIN TRANSACTION;

    INSERT INTO Loan (
        ReaderId,
        StaffId,
        LoanDate,
        DueDate,
        Notes
    )
    VALUES (
        @ReaderId,
        @StaffId,
        '2026-10-28',
        '2026-11-11',
        N'Phiếu mượn mẫu'
    );

    SET @SampleLoanId = SCOPE_IDENTITY();

    -- Sách đã được trả
    INSERT INTO LoanDetail (
        LoanId,
        BookId,
        ReturnDate,
        Status
    )
    VALUES (
        @SampleLoanId,
        @ReturnedBookId,
        '2026-10-29',
        'RETURNED'
    );

    -- Sách vẫn đang mượn
    INSERT INTO LoanDetail (
        LoanId,
        BookId,
        Status
    )
    VALUES (
        @SampleLoanId,
        @BorrowingBookId,
        'BORROWING'
    );

    UPDATE Book
    SET AvailableQuantity = AvailableQuantity - 1
    WHERE BookId = @BorrowingBookId;

    COMMIT TRANSACTION;
END;
GO


/* =========================================================
   13. TRUY VẤN KIỂM TRA DỮ LIỆU
   ========================================================= */

-- Danh sách sách kèm thể loại
SELECT
    b.BookId,
    b.Title,
    b.Author,
    c.CategoryName,
    b.TotalQuantity,
    b.AvailableQuantity
FROM Book b
INNER JOIN Category c
    ON b.CategoryId = c.CategoryId
ORDER BY b.BookId;


-- Danh sách độc giả
SELECT
    ReaderId,
    FullName,
    DateOfBirth,
    Phone,
    Email,
    Address,
    IsActive
FROM Reader
ORDER BY ReaderId;


-- Danh sách phiếu mượn và chi tiết sách
SELECT
    l.LoanId,
    r.FullName AS ReaderName,
    s.FullName AS StaffName,
    l.LoanDate,
    l.DueDate,
    b.Title,
    ld.Status,
    ld.ReturnDate
FROM Loan l
INNER JOIN Reader r
    ON l.ReaderId = r.ReaderId
INNER JOIN Staff s
    ON l.StaffId = s.StaffId
INNER JOIN LoanDetail ld
    ON l.LoanId = ld.LoanId
INNER JOIN Book b
    ON ld.BookId = b.BookId
ORDER BY l.LoanId, ld.LoanDetailId;


-- Danh sách sách đang mượn
SELECT
    l.LoanId,
    r.FullName AS ReaderName,
    b.Title,
    l.LoanDate,
    l.DueDate
FROM LoanDetail ld
INNER JOIN Loan l
    ON ld.LoanId = l.LoanId
INNER JOIN Reader r
    ON l.ReaderId = r.ReaderId
INNER JOIN Book b
    ON ld.BookId = b.BookId
WHERE ld.Status = 'BORROWING';


-- Danh sách sách quá hạn
SELECT
    l.LoanId,
    r.FullName AS ReaderName,
    b.Title,
    l.LoanDate,
    l.DueDate,
    DATEDIFF(DAY, l.DueDate, CAST(GETDATE() AS DATE))
        AS OverdueDays
FROM LoanDetail ld
INNER JOIN Loan l
    ON ld.LoanId = l.LoanId
INNER JOIN Reader r
    ON l.ReaderId = r.ReaderId
INNER JOIN Book b
    ON ld.BookId = b.BookId
WHERE ld.Status = 'BORROWING'
  AND l.DueDate < CAST(GETDATE() AS DATE);
GO

SELECT
    (SELECT COUNT(*) FROM Staff) AS StaffCount,
    (SELECT COUNT(*) FROM Category) AS CategoryCount,
    (SELECT COUNT(*) FROM Book) AS BookCount,
    (SELECT COUNT(*) FROM Reader) AS ReaderCount,
    (SELECT COUNT(*) FROM Loan) AS LoanCount,
    (SELECT COUNT(*) FROM LoanDetail) AS LoanDetailCount;