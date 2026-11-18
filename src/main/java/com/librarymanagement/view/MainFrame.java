package com.librarymanagement.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private static final Color PRIMARY_COLOR =
            new Color(35, 55, 80);

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    public MainFrame() {

        setTitle("Library Management System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1200, 750);

        setMinimumSize(new Dimension(1000, 650));

        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        tabbedPane.addTab(
                "Tổng quan",
                new DashboardPanel()
        );

        tabbedPane.addTab(
                "Thể loại",
                new CategoryPanel()
        );

        tabbedPane.addTab(
                "Sách",
                new BookPanel()
        );

        tabbedPane.addTab(
                "Độc giả",
                new ReaderPanel()
        );

        tabbedPane.addTab(
                "Mượn / Trả",
                new LoanPanel()
        );

        add(tabbedPane, BorderLayout.CENTER);

        getContentPane().setBackground(BACKGROUND_COLOR);

        setLocationRelativeTo(null);
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(PRIMARY_COLOR);

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        18, 25, 18, 25
                )
        );

        JLabel title = new JLabel(
                "HỆ THỐNG QUẢN LÝ THƯ VIỆN"
        );

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Library Management System"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(
                new Color(210, 220, 235)
        );

        JPanel titlePanel = new JPanel();

        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        titlePanel.setOpaque(false);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        return header;
    }
}