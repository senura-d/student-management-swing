package com.university.sms;

import com.formdev.flatlaf.FlatLightLaf;
import com.university.sms.ui.LoginDialog;
import com.university.sms.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Application entry point: shows the admin login, then launches the Swing UI. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (!FlatLightLaf.setup()) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // fall back to whatever the default look and feel is
                }
            }

            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isAuthenticated()) {
                new MainFrame().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
