package com.university.sms.ui;

import com.university.sms.model.Student;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Modal add/edit form for a single student. */
public class StudentFormDialog extends JDialog {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JTextField fullNameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField dobField = new JTextField(10);
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JTextField contactField = new JTextField(15);
    private final JTextField enrollmentDateField = new JTextField(10);

    private final int existingId;
    private Student result;

    public StudentFormDialog(JFrame owner, Student existing) {
        super(owner, existing == null ? "Add Student" : "Edit Student", true);
        this.existingId = existing == null ? 0 : existing.getId();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        form.add(new JLabel("Full Name:"));
        form.add(fullNameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Date of Birth (yyyy-MM-dd):"));
        form.add(dobField);
        form.add(new JLabel("Gender:"));
        form.add(genderBox);
        form.add(new JLabel("Contact No:"));
        form.add(contactField);
        form.add(new JLabel("Enrollment Date (yyyy-MM-dd):"));
        form.add(enrollmentDateField);

        if (existing != null) {
            fullNameField.setText(existing.getFullName());
            emailField.setText(existing.getEmail());
            dobField.setText(existing.getDob().toString());
            genderBox.setSelectedItem(existing.getGender());
            contactField.setText(existing.getContactNo());
            enrollmentDateField.setText(existing.getEnrollmentDate().toString());
        } else {
            enrollmentDateField.setText(LocalDate.now().toString());
        }

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String contact = contactField.getText().trim();

        if (fullName.isEmpty()) {
            showValidationError("Full name is required.");
            return;
        }
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            showValidationError("Please enter a valid email address.");
            return;
        }
        LocalDate dob = parseDate(dobField.getText().trim(), "Date of birth");
        if (dob == null) {
            return;
        }
        LocalDate enrollmentDate = parseDate(enrollmentDateField.getText().trim(), "Enrollment date");
        if (enrollmentDate == null) {
            return;
        }

        Student student = new Student();
        student.setId(existingId);
        student.setFullName(fullName);
        student.setEmail(email);
        student.setDob(dob);
        student.setGender(gender);
        student.setContactNo(contact.isEmpty() ? null : contact);
        student.setEnrollmentDate(enrollmentDate);

        this.result = student;
        dispose();
    }

    private LocalDate parseDate(String text, String fieldLabel) {
        if (text.isEmpty()) {
            showValidationError(fieldLabel + " is required (format yyyy-MM-dd).");
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            showValidationError(fieldLabel + " must be in yyyy-MM-dd format.");
            return null;
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    /** Returns the entered student, or null if the dialog was cancelled. */
    public Student getResult() {
        return result;
    }
}
