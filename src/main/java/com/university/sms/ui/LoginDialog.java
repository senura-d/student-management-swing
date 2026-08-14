package com.university.sms.ui;

import com.university.sms.dao.AdminDAO;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

/** Modal admin login screen shown before the main application window. */
public class LoginDialog extends JDialog {

    private static final String DEMO_USERNAME = "admin";
    private static final String DEMO_PASSWORD = "admin123";

    private final AdminDAO adminDAO = new AdminDAO();
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final char defaultEchoChar = passwordField.getEchoChar();

    private boolean authenticated = false;

    public LoginDialog(JFrame owner) {
        super(owner, "Admin Login", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel title = new JLabel("Student Management System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel demoHint = new JLabel("Demo login: " + DEMO_USERNAME + " / " + DEMO_PASSWORD, SwingConstants.CENTER);
        demoHint.setForeground(Color.GRAY);
        demoHint.setFont(demoHint.getFont().deriveFont(Font.ITALIC, 11f));
        demoHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        titlePanel.add(title);
        titlePanel.add(demoHint);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JCheckBox showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.addActionListener(e ->
                passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : defaultEchoChar));
        JPanel showPasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showPasswordPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        showPasswordPanel.add(showPasswordBox);

        // Pre-filled with the demo credentials so the app is immediately runnable/testable.
        usernameField.setText(DEMO_USERNAME);
        passwordField.setText(DEMO_PASSWORD);

        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");
        loginButton.addActionListener(e -> attemptLogin());
        cancelButton.addActionListener(e -> dispose());
        passwordField.addActionListener(e -> attemptLogin()); // Enter submits

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        buttons.add(cancelButton);
        buttons.add(loginButton);

        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(showPasswordPanel, BorderLayout.NORTH);
        southPanel.add(buttons, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Missing Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (adminDAO.authenticate(username, password)) {
                authenticated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Login failed due to a database error.", e);
        }
    }

    /** True once the entered credentials have been successfully verified. */
    public boolean isAuthenticated() {
        return authenticated;
    }
}
