package com.university.sms;

import com.formdev.flatlaf.FlatLightLaf;
import com.university.sms.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Application entry point: launches the Swing UI. */
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
            new MainFrame().setVisible(true);
        });
    }
}
