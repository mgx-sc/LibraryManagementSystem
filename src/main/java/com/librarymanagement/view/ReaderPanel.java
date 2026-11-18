package com.librarymanagement.view;

import com.librarymanagement.dao.ReaderDAO;
import com.librarymanagement.model.Reader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReaderPanel extends JPanel {

    private final ReaderDAO readerDAO = new ReaderDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;
    private JTextField searchField;

    private List<Reader> currentReaders = new ArrayList<>();

    private int selectedReaderId = -1;

    public ReaderPanel() {

        setLayout(new BorderLayout(12, 12));

        setBackground(new Color(245, 247, 250));

        setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadReaders();
    }

    // =========================
    // FORM THÔNG TIN ĐỘC GIẢ
    // =========================

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Thông tin độc giả"
                )
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(7, 10, 7, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        birthDateField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);

        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        // Hàng 1
        addField(panel, gbc, 0, 0,
                "Họ và tên:", nameField);

        addField(panel, gbc, 2, 0,
                "Ngày sinh:", birthDateField);

        // Hàng 2
        addField(panel, gbc, 0, 1,
                "Điện thoại:", phoneField);

        addField(panel, gbc, 2, 1,
                "Email:", emailField);

        // Hàng 3: Địa chỉ
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(new JLabel("Địa chỉ:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;

        panel.add(
                new JScrollPane(addressArea),
                gbc
        );

        // Hàng 4: Các nút chức năng
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 3)
        );

        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("Thêm");
        JButton updateButton = new JButton("Cập nhật");
        JButton deactivateButton = new JButton("Ngừng hoạt động");
        JButton clearButton = new JButton("Làm mới");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deactivateButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;

        panel.add(buttonPanel, gbc);

        addButton.addActionListener(e -> addReader());

        updateButton.addActionListener(e -> updateReader());

        deactivateButton.addActionListener(
                e -> deactivateReader()
        );

        clearButton.addActionListener(e -> {
            clearForm();
            loadReaders();
        });

        return panel;
    }

    // Tạo cặp nhãn và ô nhập liệu
    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            String label,
            JComponent component
    ) {

        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;

        panel.add(component, gbc);
    }

    // =========================
    // BẢNG DANH SÁCH ĐỘC GIẢ
    // =========================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách độc giả"
                )
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
                        "Mã độc giả",
                        "Họ và tên",
                        "Ngày sinh",
                        "Điện thoại",
                        "Email",
                        "Địa chỉ",
                        "Ngày đăng ký"
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

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        int[] widths = {
                85, 180, 110, 120, 180, 200, 150
        };

        for (int i = 0; i < widths.length; i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                selectReader();
            }
        });

        searchButton.addActionListener(e -> searchReaders());

        searchField.addActionListener(e -> searchReaders());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadReaders();
        });

        panel.add(searchPanel, BorderLayout.NORTH);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================
    // TẢI DANH SÁCH ĐỘC GIẢ
    // =========================

    private void loadReaders() {

        try {

            List<Reader> readers = readerDAO.findAllActive();

            displayReaders(readers);

        } catch (SQLException e) {

            showError("Không thể tải danh sách độc giả.", e);
        }
    }

    private void displayReaders(List<Reader> readers) {

        tableModel.setRowCount(0);

        currentReaders = new ArrayList<>(readers);

        for (Reader reader : readers) {

            tableModel.addRow(new Object[]{

                    reader.getReaderId(),

                    reader.getFullName(),

                    reader.getDateOfBirth() == null
                            ? ""
                            : reader.getDateOfBirth().toString(),

                    reader.getPhone(),

                    reader.getEmail(),

                    reader.getAddress(),

                    reader.getRegisteredAt() == null
                            ? ""
                            : reader.getRegisteredAt().toString()
            });
        }
    }

    // =========================
    // TÌM KIẾM ĐỘC GIẢ
    // =========================

    private void searchReaders() {

        String keyword = searchField.getText().trim();

        try {

            List<Reader> readers = readerDAO.search(keyword);

            // Chỉ hiển thị độc giả đang hoạt động
            readers.removeIf(reader -> !reader.isActive());

            displayReaders(readers);

        } catch (SQLException e) {

            showError("Không thể tìm kiếm độc giả.", e);
        }
    }

    // =========================
    // CHỌN ĐỘC GIẢ TRÊN BẢNG
    // =========================

    private void selectReader() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0 || selectedRow >= currentReaders.size()) {
            return;
        }

        Reader reader = currentReaders.get(selectedRow);

        selectedReaderId = reader.getReaderId();

        nameField.setText(reader.getFullName());

        birthDateField.setText(
                reader.getDateOfBirth() == null
                        ? ""
                        : reader.getDateOfBirth().toString()
        );

        phoneField.setText(
                reader.getPhone() == null
                        ? ""
                        : reader.getPhone()
        );

        emailField.setText(
                reader.getEmail() == null
                        ? ""
                        : reader.getEmail()
        );

        addressArea.setText(
                reader.getAddress() == null
                        ? ""
                        : reader.getAddress()
        );
    }

    // =========================
    // LẤY VÀ KIỂM TRA DỮ LIỆU FORM
    // =========================

    private Reader getReaderFromForm() {

        String fullName = nameField.getText().trim();

        String birthDateText = birthDateField.getText().trim();

        String phone = phoneField.getText().trim();

        String email = emailField.getText().trim();

        String address = addressArea.getText().trim();

        if (fullName.isEmpty()) {

            throw new IllegalArgumentException(
                    "Họ và tên không được để trống."
            );
        }

        if (fullName.length() > 100) {

            throw new IllegalArgumentException(
                    "Họ và tên không được vượt quá 100 ký tự."
            );
        }

        if (phone.length() > 15) {

            throw new IllegalArgumentException(
                    "Số điện thoại không được vượt quá 15 ký tự."
            );
        }

        if (email.length() > 100) {

            throw new IllegalArgumentException(
                    "Email không được vượt quá 100 ký tự."
            );
        }

        if (address.length() > 255) {

            throw new IllegalArgumentException(
                    "Địa chỉ không được vượt quá 255 ký tự."
            );
        }

        LocalDate dateOfBirth = null;

        if (!birthDateText.isEmpty()) {

            try {

                dateOfBirth = LocalDate.parse(birthDateText);

                if (dateOfBirth.isAfter(LocalDate.now())) {

                    throw new IllegalArgumentException(
                            "Ngày sinh không được nằm trong tương lai."
                    );
                }

            } catch (java.time.format.DateTimeParseException e) {

                throw new IllegalArgumentException(
                        "Ngày sinh phải theo định dạng yyyy-MM-dd. "
                                + "Ví dụ: 2002-04-10."
                );
            }
        }

        Reader reader = new Reader();

        reader.setFullName(fullName);
        reader.setDateOfBirth(dateOfBirth);

        reader.setPhone(
                phone.isEmpty() ? null : phone
        );

        reader.setEmail(
                email.isEmpty() ? null : email
        );

        reader.setAddress(
                address.isEmpty() ? null : address
        );

        return reader;
    }

    // =========================
    // THÊM ĐỘC GIẢ
    // =========================

    private void addReader() {

        try {

            Reader reader = getReaderFromForm();

            if (readerDAO.insert(reader)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm độc giả thành công."
                );

                clearForm();

                loadReaders();
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
                    "Không thể thêm độc giả. "
                            + "Số điện thoại hoặc email có thể đã tồn tại.",
                    e
            );
        }
    }

    // =========================
    // CẬP NHẬT ĐỘC GIẢ
    // =========================

    private void updateReader() {

        if (selectedReaderId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn độc giả cần cập nhật."
            );

            return;
        }

        try {

            Reader reader = getReaderFromForm();

            reader.setReaderId(selectedReaderId);

            if (readerDAO.update(reader)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật độc giả thành công."
                );

                clearForm();

                loadReaders();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy độc giả cần cập nhật."
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

            showError(
                    "Không thể cập nhật độc giả. "
                            + "Số điện thoại hoặc email có thể đã tồn tại.",
                    e
            );
        }
    }

    // =========================
    // NGỪNG HOẠT ĐỘNG ĐỘC GIẢ
    // =========================

    private void deactivateReader() {

        if (selectedReaderId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn độc giả cần ngừng hoạt động."
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn ngừng hoạt động độc giả này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (readerDAO.deactivate(selectedReaderId)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Đã ngừng hoạt động độc giả."
                );

                clearForm();

                loadReaders();
            }

        } catch (SQLException e) {

            showError(
                    "Không thể ngừng hoạt động độc giả.",
                    e
            );
        }
    }

    // =========================
    // LÀM MỚI FORM
    // =========================

    private void clearForm() {

        selectedReaderId = -1;

        nameField.setText("");
        birthDateField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressArea.setText("");

        table.clearSelection();
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