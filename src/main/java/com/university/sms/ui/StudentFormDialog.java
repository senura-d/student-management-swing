package com.university.sms.ui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.university.sms.dao.CourseDAO;
import com.university.sms.model.Course;
import com.university.sms.model.Student;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;

/** Modal add/edit form for a single student. */
public class StudentFormDialog extends JDialog {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final int PHOTO_SIZE = 100;

    private final CourseDAO courseDAO = new CourseDAO();

    private final JLabel photoPreview = new JLabel("No Photo", SwingConstants.CENTER);
    private final JTextField fullNameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final DatePicker dobPicker = new DatePicker(dateSettings());
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JTextField contactField = new JTextField(15);
    private final DatePicker enrollmentDatePicker = new DatePicker(dateSettings());
    private final JComboBox<Course> courseBox = new JComboBox<>();

    private final int existingId;
    private String photoPath;
    private Student result;

    public StudentFormDialog(JFrame owner, Student existing) {
        super(owner, existing == null ? "Add Student" : "Edit Student", true);
        this.existingId = existing == null ? 0 : existing.getId();

        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPreview.setPreferredSize(new Dimension(PHOTO_SIZE, PHOTO_SIZE));
        photoPreview.setBorder(BorderFactory.createLineBorder(photoPreview.getForeground().brighter()));
        photoPanel.add(photoPreview);
        JButton uploadPhotoButton = new JButton("Upload Photo");
        photoPanel.add(uploadPhotoButton);

        JLabel hint = new JLabel("* All fields are required", SwingConstants.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(photoPanel);
        topPanel.add(hint);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        form.add(new JLabel("Full Name: *"));
        form.add(fullNameField);
        form.add(new JLabel("Email: *"));
        form.add(emailField);
        form.add(new JLabel("Date of Birth: *"));
        form.add(dobPicker);
        form.add(new JLabel("Gender: *"));
        form.add(genderBox);
        form.add(new JLabel("Contact No: *"));
        form.add(contactField);
        form.add(new JLabel("Enrollment Date: *"));
        form.add(enrollmentDatePicker);

        if (existing == null) {
            // Enrolling in a course is only offered when creating a new student -
            // for existing students, enrollment is managed from the Enrollments tab.
            populateCourseBox();
            form.add(new JLabel("Enroll in Course:"));
            form.add(courseBox);
        }

        if (existing != null) {
            fullNameField.setText(existing.getFullName());
            emailField.setText(existing.getEmail());
            dobPicker.setDate(existing.getDob());
            genderBox.setSelectedItem(existing.getGender());
            contactField.setText(existing.getContactNo());
            enrollmentDatePicker.setDate(existing.getEnrollmentDate());
            photoPath = existing.getPhotoPath();
            updatePhotoPreview();
        } else {
            enrollmentDatePicker.setDateToToday();
        }

        uploadPhotoButton.addActionListener(e -> chooseAndCopyPhoto());

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private static DatePickerSettings dateSettings() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra(DATE_PATTERN);
        return settings;
    }

    private void populateCourseBox() {
        courseBox.addItem(null);
        courseBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "— None —" : value.toString());
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        try {
            for (Course c : courseDAO.findAll()) {
                courseBox.addItem(c);
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load courses.", e);
        }
    }

    private void chooseAndCopyPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            photoPath = PhotoUtil.copyToPhotosFolder(chooser.getSelectedFile());
            updatePhotoPreview();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load photo: " + e.getMessage(),
                    "Upload Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePhotoPreview() {
        ImageIcon icon = PhotoUtil.loadThumbnail(photoPath, PHOTO_SIZE);
        photoPreview.setIcon(icon);
        photoPreview.setText(icon == null ? "No Photo" : null);
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
        if (contact.isEmpty()) {
            showValidationError("Contact number is required.");
            return;
        }
        LocalDate dob = dobPicker.getDate();
        if (dob == null) {
            showValidationError("Please choose a date of birth.");
            return;
        }
        LocalDate enrollmentDate = enrollmentDatePicker.getDate();
        if (enrollmentDate == null) {
            showValidationError("Please choose an enrollment date.");
            return;
        }

        Student student = new Student();
        student.setId(existingId);
        student.setFullName(fullName);
        student.setEmail(email);
        student.setDob(dob);
        student.setGender(gender);
        student.setContactNo(contact);
        student.setEnrollmentDate(enrollmentDate);
        student.setPhotoPath(photoPath);

        this.result = student;
        dispose();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    /** Returns the entered student, or null if the dialog was cancelled. */
    public Student getResult() {
        return result;
    }

    /** Returns the course selected for immediate enrollment, or null if none was chosen. */
    public Course getSelectedCourse() {
        return (Course) courseBox.getSelectedItem();
    }
}
