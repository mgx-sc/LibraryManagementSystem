package com.librarymanagement.view;

import com.librarymanagement.dao.CategoryDAO;
import com.librarymanagement.model.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class CategoryPanel extends JPanel {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField descriptionField;

    private int selectedCategoryId = -1;

    public CategoryPanel() {

        setLayout(new BorderLayout(15, 15));

        setBorder(
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        );

        setBackground(new Color(245, 247, 250));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadCategories();
    }

    // Khu vực nhập thông tin thể loại
    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(Color.WHITE);
        panel.setBorder(
                BorderFactory.createTitledBorder("Thông tin thể loại")
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Tên thể loại:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        nameField = new JTextField(25);
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        panel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        descriptionField = new JTextField(25);
        panel.add(descriptionField, gbc);

        // Khu vực nút chức năng
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("Thêm");
        JButton updateButton = new JButton("Cập nhật");
        JButton deactivateButton = new JButton("Ngừng sử dụng");
        JButton refreshButton = new JButton("Làm mới");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deactivateButton);
        buttonPanel.add(refreshButton);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;

        panel.add(buttonPanel, gbc);

        addButton.addActionListener(e -> addCategory());

        updateButton.addActionListener(e -> updateCategory());

        deactivateButton.addActionListener(
                e -> deactivateCategory()
        );

        refreshButton.addActionListener(e -> {
            clearForm();
            loadCategories();
        });

        return panel;
    }

    // Khu vực hiển thị danh sách
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder("Danh sách thể loại")
        );

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Mã thể loại",
                        "Tên thể loại",
                        "Mô tả"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        table.setRowHeight(28);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                selectCategory();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Tải danh sách thể loại từ database
    private void loadCategories() {

        try {

            tableModel.setRowCount(0);

            for (Category category : categoryDAO.findAllActive()) {

                tableModel.addRow(new Object[]{
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getDescription()
                });
            }

        } catch (SQLException e) {

            showError("Không thể tải danh sách thể loại.", e);
        }
    }

    // Chọn một thể loại trong bảng
    private void selectCategory() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        selectedCategoryId = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString()
        );

        nameField.setText(
                tableModel.getValueAt(selectedRow, 1).toString()
        );

        Object description =
                tableModel.getValueAt(selectedRow, 2);

        descriptionField.setText(
                description == null ? "" : description.toString()
        );
    }

    // Thêm thể loại
    private void addCategory() {

        String name = nameField.getText().trim();

        String description = descriptionField.getText().trim();

        if (name.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập tên thể loại.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Category category = new Category();

        category.setCategoryName(name);

        category.setDescription(
                description.isEmpty() ? null : description
        );

        try {

            if (categoryDAO.insert(category)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm thể loại thành công."
                );

                clearForm();
                loadCategories();
            }

        } catch (SQLException e) {

            showError(
                    "Không thể thêm thể loại. Có thể tên thể loại đã tồn tại.",
                    e
            );
        }
    }

    // Cập nhật thể loại
    private void updateCategory() {

        if (selectedCategoryId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn thể loại cần cập nhật."
            );

            return;
        }

        String name = nameField.getText().trim();

        String description = descriptionField.getText().trim();

        if (name.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Tên thể loại không được để trống."
            );

            return;
        }

        Category category = new Category();

        category.setCategoryId(selectedCategoryId);
        category.setCategoryName(name);

        category.setDescription(
                description.isEmpty() ? null : description
        );

        try {

            if (categoryDAO.update(category)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật thể loại thành công."
                );

                clearForm();
                loadCategories();
            }

        } catch (SQLException e) {

            showError("Không thể cập nhật thể loại.", e);
        }
    }

    // Ngừng sử dụng thể loại
    private void deactivateCategory() {

        if (selectedCategoryId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn thể loại cần ngừng sử dụng."
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn ngừng sử dụng thể loại này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (categoryDAO.deactivate(selectedCategoryId)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Đã ngừng sử dụng thể loại."
                );

                clearForm();
                loadCategories();
            }

        } catch (SQLException e) {

            showError("Không thể ngừng sử dụng thể loại.", e);
        }
    }

    // Xóa nội dung các ô nhập
    private void clearForm() {

        selectedCategoryId = -1;

        nameField.setText("");
        descriptionField.setText("");

        if (table != null) {
            table.clearSelection();
        }
    }

    // Hiển thị lỗi
    private void showError(String message, Exception e) {

        JOptionPane.showMessageDialog(
                this,
                message + "\nChi tiết: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }
}