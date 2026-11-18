package com.librarymanagement.view;

import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.dao.CategoryDAO;
import com.librarymanagement.model.Book;
import com.librarymanagement.model.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField yearField;
    private JTextField isbnField;
    private JTextField quantityField;
    private JTextField searchField;

    private JComboBox<Category> categoryComboBox;

    private List<Book> currentBooks = new ArrayList<>();

    private final Map<Integer, String> categoryNames = new HashMap<>();

    private int selectedBookId = -1;

    public BookPanel() {

        setLayout(new BorderLayout(12, 12));

        setBackground(new Color(245, 247, 250));

        setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadCategories();
        loadBooks();
    }

    // =========================
    // FORM NHẬP THÔNG TIN SÁCH
    // =========================

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder("Thông tin sách")
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(7, 10, 7, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        titleField = new JTextField(20);
        authorField = new JTextField(20);
        publisherField = new JTextField(20);
        yearField = new JTextField(20);
        isbnField = new JTextField(20);
        quantityField = new JTextField(20);

        categoryComboBox = new JComboBox<>();

        // Hàng 1
        addField(panel, gbc, 0, 0, "Tên sách:", titleField);
        addField(panel, gbc, 2, 0, "Tác giả:", authorField);

        // Hàng 2
        addField(panel, gbc, 0, 1, "Thể loại:", categoryComboBox);
        addField(panel, gbc, 2, 1, "Nhà xuất bản:", publisherField);

        // Hàng 3
        addField(panel, gbc, 0, 2, "Năm xuất bản:", yearField);
        addField(panel, gbc, 2, 2, "ISBN:", isbnField);

        // Hàng 4: Số lượng
        addField(panel, gbc, 0, 3, "Tổng số lượng:", quantityField);

        // Hàng 5: Các nút chức năng
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 3)
        );

        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("Thêm");
        JButton updateButton = new JButton("Cập nhật");
        JButton deactivateButton = new JButton("Ngừng sử dụng");
        JButton clearButton = new JButton("Làm mới");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deactivateButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(buttonPanel, gbc);

        // Sự kiện nút
        addButton.addActionListener(e -> addBook());

        updateButton.addActionListener(e -> updateBook());

        deactivateButton.addActionListener(
                e -> deactivateBook()
        );

        clearButton.addActionListener(e -> clearForm());

        return panel;
    }

    // Tạo một cặp nhãn và ô nhập liệu
    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            String label,
            JComponent component
    ) {

        gbc.gridy = y;
        gbc.gridwidth = 1;

        gbc.gridx = x;
        gbc.weightx = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;

        panel.add(component, gbc);
    }

    // =========================
    // BẢNG DANH SÁCH SÁCH
    // =========================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder("Danh sách sách")
        );

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5)
        );

        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField(30);

        JButton searchButton = new JButton("Tìm kiếm");
        JButton refreshButton = new JButton("Tải lại");

        searchPanel.add(new JLabel("Từ khóa:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        // Bảng dữ liệu
        tableModel = new DefaultTableModel(
                new Object[]{
                        "Mã sách",
                        "Tên sách",
                        "Tác giả",
                        "Thể loại",
                        "Nhà xuất bản",
                        "Năm XB",
                        "ISBN",
                        "Tổng SL",
                        "Còn lại"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        table.setRowHeight(30);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        // Điều chỉnh độ rộng cột
        int[] columnWidths = {
                75, 230, 160, 160, 160, 85, 140, 85, 85
        };

        for (int i = 0; i < columnWidths.length; i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(columnWidths[i]);
        }

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        // Chọn sách trên bảng
        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                selectBook();
            }
        });

        searchButton.addActionListener(e -> searchBooks());

        searchField.addActionListener(e -> searchBooks());

        refreshButton.addActionListener(e -> {

            searchField.setText("");

            loadBooks();
        });

        panel.add(searchPanel, BorderLayout.NORTH);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================
    // TẢI THỂ LOẠI
    // =========================

    private void loadCategories() {

        try {

            categoryComboBox.removeAllItems();

            categoryNames.clear();

            List<Category> categories =
                    categoryDAO.findAllActive();

            for (Category category : categories) {

                categoryComboBox.addItem(category);

                categoryNames.put(
                        category.getCategoryId(),
                        category.getCategoryName()
                );
            }

        } catch (SQLException e) {

            showError("Không thể tải danh sách thể loại.", e);
        }
    }

    // =========================
    // TẢI DANH SÁCH SÁCH
    // =========================

    private void loadBooks() {

        try {

            List<Book> books = bookDAO.findAll();

            books.removeIf(book -> !book.isActive());

            displayBooks(books);

        } catch (SQLException e) {

            showError("Không thể tải danh sách sách.", e);
        }
    }

    private void displayBooks(List<Book> books) {

        tableModel.setRowCount(0);

        currentBooks = new ArrayList<>(books);

        for (Book book : books) {

            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),

                    categoryNames.getOrDefault(
                            book.getCategoryId(),
                            "Không xác định"
                    ),

                    book.getPublisher(),
                    book.getPublicationYear(),
                    book.getIsbn(),
                    book.getTotalQuantity(),
                    book.getAvailableQuantity()
            });
        }
    }

    // =========================
    // TÌM KIẾM SÁCH
    // =========================

    private void searchBooks() {

        String keyword = searchField.getText().trim();

        try {

            List<Book> results = bookDAO.search(keyword);

            results.removeIf(book -> !book.isActive());

            displayBooks(results);

        } catch (SQLException e) {

            showError("Không thể tìm kiếm sách.", e);
        }
    }

    // =========================
    // CHỌN SÁCH TRÊN BẢNG
    // =========================

    private void selectBook() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0 || selectedRow >= currentBooks.size()) {
            return;
        }

        Book book = currentBooks.get(selectedRow);

        selectedBookId = book.getBookId();

        titleField.setText(book.getTitle());

        authorField.setText(book.getAuthor());

        publisherField.setText(
                book.getPublisher() == null
                        ? ""
                        : book.getPublisher()
        );

        yearField.setText(
                book.getPublicationYear() == null
                        ? ""
                        : book.getPublicationYear().toString()
        );

        isbnField.setText(
                book.getIsbn() == null
                        ? ""
                        : book.getIsbn()
        );

        quantityField.setText(
                String.valueOf(book.getTotalQuantity())
        );

        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {

            Category category = categoryComboBox.getItemAt(i);

            if (category.getCategoryId() == book.getCategoryId()) {

                categoryComboBox.setSelectedIndex(i);

                break;
            }
        }
    }

    // =========================
    // LẤY DỮ LIỆU TỪ FORM
    // =========================

    private Book getBookFromForm() {

        String title = titleField.getText().trim();

        String author = authorField.getText().trim();

        String publisher = publisherField.getText().trim();

        String yearText = yearField.getText().trim();

        String isbn = isbnField.getText().trim();

        String quantityText = quantityField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {

            throw new IllegalArgumentException(
                    "Tên sách và tác giả không được để trống."
            );
        }

        Category category =
                (Category) categoryComboBox.getSelectedItem();

        if (category == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn thể loại sách."
            );
        }

        if (title.length() > 200
                || author.length() > 150
                || publisher.length() > 150
                || isbn.length() > 20) {

            throw new IllegalArgumentException(
                    "Thông tin nhập vượt quá độ dài cho phép."
            );
        }

        Short publicationYear = null;

        if (!yearText.isEmpty()) {

            try {

                short year = Short.parseShort(yearText);

                if (year < 1000 || year > 2100) {

                    throw new IllegalArgumentException(
                            "Năm xuất bản phải từ 1000 đến 2100."
                    );
                }

                publicationYear = year;

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Năm xuất bản phải là số hợp lệ."
                );
            }
        }

        int quantity;

        try {

            quantity = Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Số lượng phải là số nguyên."
            );
        }

        if (quantity < 0) {

            throw new IllegalArgumentException(
                    "Số lượng không được âm."
            );
        }

        return new Book(
                category.getCategoryId(),
                title,
                author,
                publisher.isEmpty() ? null : publisher,
                publicationYear,
                isbn.isEmpty() ? null : isbn,
                quantity
        );
    }

    // =========================
    // THÊM SÁCH
    // =========================

    private void addBook() {

        try {

            Book book = getBookFromForm();

            if (bookDAO.insert(book)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm sách thành công."
                );

                clearForm();

                loadBooks();
            }

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException e) {

            showError(
                    "Không thể thêm sách. ISBN có thể đã tồn tại.",
                    e
            );
        }
    }

    // =========================
    // CẬP NHẬT SÁCH
    // =========================

    private void updateBook() {

        if (selectedBookId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn sách cần cập nhật."
            );

            return;
        }

        try {

            Book book = getBookFromForm();

            book.setBookId(selectedBookId);

            if (bookDAO.update(book)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật sách thành công."
                );

                clearForm();

                loadBooks();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật thất bại. "
                                + "Tổng số lượng không được thấp hơn "
                                + "số sách đang được mượn."
                );
            }

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException e) {

            showError("Không thể cập nhật sách.", e);
        }
    }

    // =========================
    // NGỪNG SỬ DỤNG SÁCH
    // =========================

    private void deactivateBook() {

        if (selectedBookId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn sách cần ngừng sử dụng."
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn ngừng sử dụng sách này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (bookDAO.deactivate(selectedBookId)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Đã ngừng sử dụng sách."
                );

                clearForm();

                loadBooks();
            }

        } catch (SQLException e) {

            showError("Không thể ngừng sử dụng sách.", e);
        }
    }

    // =========================
    // LÀM MỚI FORM
    // =========================

    private void clearForm() {

        selectedBookId = -1;

        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        yearField.setText("");
        isbnField.setText("");
        quantityField.setText("");

        table.clearSelection();

        if (categoryComboBox.getItemCount() > 0) {

            categoryComboBox.setSelectedIndex(0);
        }
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