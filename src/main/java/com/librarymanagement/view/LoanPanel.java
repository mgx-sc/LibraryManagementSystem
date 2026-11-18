package com.librarymanagement.view;

import com.librarymanagement.config.DatabaseConfig;
import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.dao.LoanDAO;
import com.librarymanagement.dao.ReaderDAO;
import com.librarymanagement.model.Book;
import com.librarymanagement.model.Reader;
import com.librarymanagement.model.Staff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class LoanPanel extends JPanel {

    private final LoanDAO loanDAO = new LoanDAO();
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final BookDAO bookDAO = new BookDAO();

    private JComboBox<Reader> readerComboBox;
    private JComboBox<Staff> staffComboBox;

    private JTextField dueDateField;
    private JTextField notesField;

    private JList<Book> bookList;
    private DefaultListModel<Book> bookListModel;

    private JTable loanTable;
    private DefaultTableModel tableModel;

    public LoanPanel() {

        setLayout(new BorderLayout(12, 12));

        setBackground(new Color(245, 247, 250));

        setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadReaders();
        loadStaffs();
        loadBooks();
        loadLoans();
    }

    // =========================
    // FORM LẬP PHIẾU MƯỢN
    // =========================

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder("Lập phiếu mượn sách")
        );

        // Khu vực thông tin phiếu mượn
        JPanel inputPanel = new JPanel(new GridBagLayout());

        inputPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        readerComboBox = new JComboBox<>();
        staffComboBox = new JComboBox<>();

        dueDateField = new JTextField(
                LocalDate.now().plusDays(14).toString()
        );

        notesField = new JTextField();

        addInputField(
                inputPanel, gbc, 0, 0,
                "Độc giả:", readerComboBox
        );

        addInputField(
                inputPanel, gbc, 2, 0,
                "Nhân viên:", staffComboBox
        );

        addInputField(
                inputPanel, gbc, 0, 1,
                "Ngày hẹn trả:", dueDateField
        );

        addInputField(
                inputPanel, gbc, 2, 1,
                "Ghi chú:", notesField
        );

        // Khu vực chọn sách
        JPanel bookPanel = new JPanel(new BorderLayout(5, 5));

        bookPanel.setBackground(Color.WHITE);

        bookPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Chọn sách cần mượn (có thể chọn nhiều)"
                )
        );

        bookListModel = new DefaultListModel<>();

        bookList = new JList<>(bookListModel);

        bookList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );

        bookList.setVisibleRowCount(3);

        bookList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focus
            ) {

                super.getListCellRendererComponent(
                        list, value, index, selected, focus
                );

                if (value instanceof Book book) {

                    setText(
                            book.getTitle()
                                    + " - Còn "
                                    + book.getAvailableQuantity()
                                    + " cuốn"
                    );
                }

                return this;
            }
        });

        bookPanel.add(
                new JScrollPane(bookList),
                BorderLayout.CENTER
        );

        // Các nút chức năng
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 3)
        );

        buttonPanel.setBackground(Color.WHITE);

        JButton borrowButton = new JButton("Lập phiếu mượn");

        JButton refreshButton = new JButton("Làm mới");

        buttonPanel.add(borrowButton);
        buttonPanel.add(refreshButton);

        borrowButton.addActionListener(e -> borrowBooks());

        refreshButton.addActionListener(e -> refreshAll());

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(bookPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addInputField(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            String label,
            JComponent component
    ) {

        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;

        panel.add(component, gbc);
    }

    // =========================
    // BẢNG THEO DÕI MƯỢN / TRẢ
    // =========================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách mượn / trả sách"
                )
        );

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Mã chi tiết",
                        "Mã phiếu",
                        "Độc giả",
                        "Nhân viên",
                        "Tên sách",
                        "Ngày mượn",
                        "Hẹn trả",
                        "Trạng thái",
                        "Ngày trả"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loanTable = new JTable(tableModel);

        loanTable.setRowHeight(28);

        loanTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        loanTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        loanTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        int[] widths = {
                90, 75, 150, 150, 200, 100, 100, 100, 100
        };

        for (int i = 0; i < widths.length; i++) {

            loanTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // Nút trả sách
        JButton returnButton = new JButton("Trả sách đã chọn");

        returnButton.addActionListener(
                e -> returnSelectedBook()
        );

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        actionPanel.setBackground(Color.WHITE);

        actionPanel.add(returnButton);

        panel.add(actionPanel, BorderLayout.NORTH);

        panel.add(
                new JScrollPane(loanTable),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================
    // TẢI DANH SÁCH ĐỘC GIẢ
    // =========================

    private void loadReaders() {

        try {

            readerComboBox.removeAllItems();

            List<Reader> readers = readerDAO.findAllActive();

            for (Reader reader : readers) {
                readerComboBox.addItem(reader);
            }

        } catch (SQLException e) {

            showError("Không thể tải danh sách độc giả.", e);
        }
    }

    // =========================
    // TẢI DANH SÁCH NHÂN VIÊN
    // =========================

    private void loadStaffs() {

        String sql = """
                SELECT StaffId, FullName
                FROM dbo.Staff
                WHERE IsActive = 1
                ORDER BY StaffId
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            staffComboBox.removeAllItems();

            while (rs.next()) {

                Staff staff = new Staff();

                staff.setStaffId(rs.getInt("StaffId"));

                staff.setFullName(rs.getString("FullName"));

                staffComboBox.addItem(staff);
            }

        } catch (SQLException e) {

            showError("Không thể tải danh sách nhân viên.", e);
        }
    }

    // =========================
    // TẢI SÁCH CÓ THỂ CHO MƯỢN
    // =========================

    private void loadBooks() {

        try {

            bookListModel.clear();

            List<Book> books = bookDAO.findAll();

            for (Book book : books) {

                if (book.isActive()
                        && book.getAvailableQuantity() > 0) {

                    bookListModel.addElement(book);
                }
            }

        } catch (SQLException e) {

            showError("Không thể tải danh sách sách.", e);
        }
    }

    // =========================
    // TẢI LỊCH SỬ MƯỢN / TRẢ
    // =========================

    private void loadLoans() {

        String sql = """
                SELECT
                    ld.LoanDetailId,
                    l.LoanId,
                    r.FullName AS ReaderName,
                    s.FullName AS StaffName,
                    b.Title AS BookTitle,
                    l.LoanDate,
                    l.DueDate,
                    ld.ReturnDate,
                    CASE
                        WHEN ld.Status = 'RETURNED'
                            THEN N'Đã trả'
                        WHEN ld.Status = 'LOST'
                            THEN N'Đã mất'
                        WHEN l.DueDate < CAST(GETDATE() AS DATE)
                            THEN N'Quá hạn'
                        ELSE N'Đang mượn'
                    END AS DisplayStatus
                FROM dbo.LoanDetail ld
                INNER JOIN dbo.Loan l
                    ON ld.LoanId = l.LoanId
                INNER JOIN dbo.Reader r
                    ON l.ReaderId = r.ReaderId
                INNER JOIN dbo.Staff s
                    ON l.StaffId = s.StaffId
                INNER JOIN dbo.Book b
                    ON ld.BookId = b.BookId
                ORDER BY l.LoanId DESC, ld.LoanDetailId
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            tableModel.setRowCount(0);

            while (rs.next()) {

                Date loanDate = rs.getDate("LoanDate");
                Date dueDate = rs.getDate("DueDate");
                Date returnDate = rs.getDate("ReturnDate");

                tableModel.addRow(new Object[]{

                        rs.getInt("LoanDetailId"),

                        rs.getInt("LoanId"),

                        rs.getString("ReaderName"),

                        rs.getString("StaffName"),

                        rs.getString("BookTitle"),

                        loanDate == null ? "" : loanDate.toString(),

                        dueDate == null ? "" : dueDate.toString(),

                        rs.getString("DisplayStatus"),

                        returnDate == null ? "" : returnDate.toString()
                });
            }

        } catch (SQLException e) {

            showError("Không thể tải lịch sử mượn / trả.", e);
        }
    }

    // =========================
    // LẬP PHIẾU MƯỢN
    // =========================

    private void borrowBooks() {

        Reader reader = (Reader) readerComboBox.getSelectedItem();

        Staff staff = (Staff) staffComboBox.getSelectedItem();

        if (reader == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn độc giả."
            );

            return;
        }

        if (staff == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn nhân viên."
            );

            return;
        }

        List<Book> selectedBooks =
                bookList.getSelectedValuesList();

        if (selectedBooks.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn ít nhất một cuốn sách."
            );

            return;
        }

        try {

            LocalDate dueDate = LocalDate.parse(
                    dueDateField.getText().trim()
            );

            List<Integer> bookIds = selectedBooks.stream()
                    .map(Book::getBookId)
                    .toList();

            String notes = notesField.getText().trim();

            int loanId = loanDAO.borrowBooks(
                    reader.getReaderId(),
                    staff.getStaffId(),
                    dueDate,
                    bookIds,
                    notes.isEmpty() ? null : notes
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Lập phiếu mượn thành công.\n"
                            + "Mã phiếu mượn: " + loanId
            );

            bookList.clearSelection();

            notesField.setText("");

            refreshAll();

        } catch (java.time.format.DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ngày hẹn trả không hợp lệ. "
                            + "Định dạng đúng: yyyy-MM-dd.",
                    "Lỗi dữ liệu",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException e) {

            showError("Không thể lập phiếu mượn.", e);
        }
    }

    // =========================
    // TRẢ SÁCH
    // =========================

    private void returnSelectedBook() {

        int selectedRow = loanTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn chi tiết phiếu mượn cần trả."
            );

            return;
        }

        String status = tableModel.getValueAt(
                selectedRow, 7
        ).toString();

        if (!status.equals("Đang mượn")
                && !status.equals("Quá hạn")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Sách này đã được trả hoặc không thể trả."
            );

            return;
        }

        int loanDetailId = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString()
        );

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận trả sách này?",
                "Xác nhận trả sách",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean success = loanDAO.returnBook(loanDetailId);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Trả sách thành công."
                );

                refreshAll();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Sách này đã được trả hoặc không tồn tại."
                );
            }

        } catch (SQLException e) {

            showError("Không thể trả sách.", e);
        }
    }

    // =========================
    // LÀM MỚI TOÀN BỘ DỮ LIỆU
    // =========================

    private void refreshAll() {

        loadReaders();
        loadStaffs();
        loadBooks();
        loadLoans();
    }

    // =========================
    // HIỂN THỊ LỖI
    // =========================

    private void showError(String message, Exception e) {

        JOptionPane.showMessageDialog(
                this,
                message + "\nChi tiết: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }
}