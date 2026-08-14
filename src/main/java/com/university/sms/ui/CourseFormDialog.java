package com.university.sms.ui;

import com.university.sms.model.Course;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** Modal add/edit form for a single course. */
public class CourseFormDialog extends JDialog {

    private final JTextField courseCodeField = new JTextField(15);
    private final JTextField courseNameField = new JTextField(20);
    private final JTextField creditsField = new JTextField(5);

    private final int existingId;
    private Course result;

    public CourseFormDialog(JFrame owner, Course existing) {
        super(owner, existing == null ? "Add Course" : "Edit Course", true);
        this.existingId = existing == null ? 0 : existing.getId();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        form.add(new JLabel("Course Code:"));
        form.add(courseCodeField);
        form.add(new JLabel("Course Name:"));
        form.add(courseNameField);
        form.add(new JLabel("Credits:"));
        form.add(creditsField);

        if (existing != null) {
            courseCodeField.setText(existing.getCourseCode());
            courseNameField.setText(existing.getCourseName());
            creditsField.setText(String.valueOf(existing.getCredits()));
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
        String courseCode = courseCodeField.getText().trim();
        String courseName = courseNameField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (courseCode.isEmpty()) {
            showValidationError("Course code is required.");
            return;
        }
        if (courseName.isEmpty()) {
            showValidationError("Course name is required.");
            return;
        }
        int credits;
        try {
            credits = Integer.parseInt(creditsText);
        } catch (NumberFormatException e) {
            showValidationError("Credits must be a whole number.");
            return;
        }
        if (credits <= 0) {
            showValidationError("Credits must be greater than zero.");
            return;
        }

        Course course = new Course();
        course.setId(existingId);
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCredits(credits);

        this.result = course;
        dispose();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    /** Returns the entered course, or null if the dialog was cancelled. */
    public Course getResult() {
        return result;
    }
}
