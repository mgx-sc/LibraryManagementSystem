package com.librarymanagement;

import com.librarymanagement.view.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );

            } catch (Exception e) {
                System.err.println(
                        "Không thể thiết lập giao diện hệ thống: "
                                + e.getMessage()
                );
            }

            MainFrame mainFrame = new MainFrame();

            mainFrame.setVisible(true);
        });
    }
}