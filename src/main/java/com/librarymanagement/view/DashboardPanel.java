package com.librarymanagement.view;

import com.librarymanagement.dao.DashboardDAO;
import com.librarymanagement.dao.DashboardDAO.Statistics;
import com.librarymanagement.dao.DashboardDAO.RecentLoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private static final Color BACKGROUND =
            new Color(245, 247, 250);

    private static final Color PRIMARY =
            new Color(35, 55, 80);

    private JLabel bookTitlesValue;
    private JLabel readersValue;
    private JLabel borrowingValue;
    private JLabel overdueValue;

    private JLabel totalCopiesValue;
    private JLabel availableCopiesValue;
    private JLabel borrowedCopiesValue;
    private JLabel categoriesValue;

    private DefaultTableModel recentTableModel;

    public DashboardPanel() {

        setLayout(new BorderLayout(15, 15));

        setBackground(BACKGROUND);

        setBorder(
                new EmptyBorder(22, 25, 22, 25)
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadDashboard();
    }

    // =========================
    // HEADER
    // =========================

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);

        JLabel title = new JLabel(
                "TỔNG QUAN THƯ VIỆN"
        );

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        title.setForeground(PRIMARY);

        JLabel subtitle = new JLabel(
                "Theo dõi hoạt động và tình hình thư viện"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton refreshButton = new JButton("Làm mới");

        refreshButton.addActionListener(
                e -> loadDashboard()
        );

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    // =========================
    // NỘI DUNG DASHBOARD
    // =========================

    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout(15, 15));

        content.setOpaque(false);

        // Bốn thẻ thống kê
        JPanel statisticsPanel = new JPanel(
                new GridLayout(1, 4, 15, 15)
        );

        statisticsPanel.setOpaque(false);

        bookTitlesValue = new JLabel("0");
        readersValue = new JLabel("0");
        borrowingValue = new JLabel("0");
        overdueValue = new JLabel("0");

        statisticsPanel.add(
                createMetricCard(
                        "ĐẦU SÁCH",
                        bookTitlesValue,
                        "Đang hoạt động",
                        new Color(50, 110, 190)
                )
        );

        statisticsPanel.add(
                createMetricCard(
                        "ĐỘC GIẢ",
                        readersValue,
                        "Đang hoạt động",
                        new Color(40, 145, 100)
                )
        );

        statisticsPanel.add(
                createMetricCard(
                        "ĐANG MƯỢN",
                        borrowingValue,
                        "Số cuốn chưa trả",
                        new Color(205, 145, 35)
                )
        );

        statisticsPanel.add(
                createMetricCard(
                        "QUÁ HẠN",
                        overdueValue,
                        "Cần theo dõi",
                        new Color(195, 65, 65)
                )
        );

        // Khu vực bên dưới
        JPanel lowerPanel = new JPanel(
                new GridLayout(1, 2, 15, 15)
        );

        lowerPanel.setOpaque(false);

        lowerPanel.add(createInventoryPanel());
        lowerPanel.add(createRecentLoansPanel());

        content.add(statisticsPanel, BorderLayout.NORTH);
        content.add(lowerPanel, BorderLayout.CENTER);

        return content;
    }

    // =========================
    // THẺ THỐNG KÊ
    // =========================

    private JPanel createMetricCard(
            String title,
            JLabel valueLabel,
            String description,
            Color accent
    ) {

        JPanel card = new JPanel();

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 4, 0, 0, accent
                        ),
                        new EmptyBorder(14, 16, 14, 14)
                )
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 32)
        );

        valueLabel.setForeground(accent);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        descriptionLabel.setForeground(Color.GRAY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    // =========================
    // TÌNH HÌNH KHO SÁCH
    // =========================

    private JPanel createInventoryPanel() {

        JPanel panel = createSectionPanel(
                "TÌNH HÌNH KHO SÁCH"
        );

        JPanel rows = new JPanel(
                new GridLayout(4, 1, 5, 5)
        );

        rows.setOpaque(false);

        totalCopiesValue = createInventoryValue();
        availableCopiesValue = createInventoryValue();
        borrowedCopiesValue = createInventoryValue();
        categoriesValue = createInventoryValue();

        rows.add(
                createInventoryRow(
                        "Tổng số bản sách",
                        totalCopiesValue
                )
        );

        rows.add(
                createInventoryRow(
                        "Còn có thể cho mượn",
                        availableCopiesValue
                )
        );

        rows.add(
                createInventoryRow(
                        "Đang được mượn",
                        borrowedCopiesValue
                )
        );

        rows.add(
                createInventoryRow(
                        "Thể loại hoạt động",
                        categoriesValue
                )
        );

        panel.add(rows, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createInventoryValue() {

        JLabel label = new JLabel("0");

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 20)
        );

        label.setForeground(PRIMARY);

        return label;
    }

    private JPanel createInventoryRow(
            String title,
            JLabel value
    ) {

        JPanel row = new JPanel(new BorderLayout());

        row.setOpaque(false);

        row.setBorder(
                new EmptyBorder(8, 10, 8, 10)
        );

        JLabel label = new JLabel(title);

        label.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    // =========================
    // PHIẾU MƯỢN GẦN ĐÂY
    // =========================

    private JPanel createRecentLoansPanel() {

        JPanel panel = createSectionPanel(
                "PHIẾU MƯỢN GẦN ĐÂY"
        );

        recentTableModel = new DefaultTableModel(
                new Object[]{
                        "Mã phiếu",
                        "Độc giả",
                        "Ngày mượn",
                        "Hẹn trả",
                        "Số cuốn"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(recentTableModel);

        table.setRowHeight(30);

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        int[] widths = {
                75, 150, 105, 105, 70
        };

        for (int i = 0; i < widths.length; i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================
    // KHUNG CHUNG CHO CÁC KHU VỰC
    // =========================

    private JPanel createSectionPanel(String title) {

        JPanel panel = new JPanel(new BorderLayout(10, 12));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                new Color(225, 230, 237)
                        ),
                        new EmptyBorder(14, 14, 14, 14)
                )
        );

        JLabel heading = new JLabel(title);

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        heading.setForeground(PRIMARY);

        panel.add(heading, BorderLayout.NORTH);

        return panel;
    }

    // =========================
    // TẢI DỮ LIỆU DASHBOARD
    // =========================

    private void loadDashboard() {

        try {

            Statistics stats = dashboardDAO.getStatistics();

            bookTitlesValue.setText(
                    String.valueOf(stats.activeBookTitles())
            );

            readersValue.setText(
                    String.valueOf(stats.activeReaders())
            );

            borrowingValue.setText(
                    String.valueOf(stats.borrowingCount())
            );

            overdueValue.setText(
                    String.valueOf(stats.overdueCount())
            );

            totalCopiesValue.setText(
                    String.valueOf(stats.totalCopies())
            );

            availableCopiesValue.setText(
                    String.valueOf(stats.availableCopies())
            );

            borrowedCopiesValue.setText(
                    String.valueOf(
                            stats.totalCopies() - stats.availableCopies()
                    )
            );

            categoriesValue.setText(
                    String.valueOf(stats.activeCategories())
            );

            loadRecentLoans();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải dữ liệu tổng quan.\n"
                            + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadRecentLoans() throws SQLException {

        List<RecentLoan> loans =
                dashboardDAO.findRecentLoans();

        recentTableModel.setRowCount(0);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (RecentLoan loan : loans) {

            recentTableModel.addRow(new Object[]{

                    loan.loanId(),

                    loan.readerName(),

                    loan.loanDate().format(formatter),

                    loan.dueDate().format(formatter),

                    loan.bookCount()
            });
        }
    }
}