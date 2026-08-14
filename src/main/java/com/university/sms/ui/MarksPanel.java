package com.university.sms.ui;

import com.university.sms.dao.EnrollmentDAO;
import com.university.sms.dao.StudentDAO;
import com.university.sms.model.Enrollment;
import com.university.sms.model.Student;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

/** Lets exam marks be entered for a student's enrolled courses; the letter grade is derived automatically. */
public class MarksPanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS = {"Course Code", "Course Name", "Marks", "Grade"};

    private final StudentDAO studentDAO = new StudentDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final JComboBox<Student> studentBox = new JComboBox<>();
    private final JComboBox<Enrollment> courseBox = new JComboBox<>();
    private final JSpinner marksSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JLabel gradePreviewLabel = new JLabel("Grade: -");
    private final DefaultTableModel tableModel;
    private final JTable table;

    public MarksPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        courseBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : value.getCourseCode() + " - " + value.getCourseName());
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });

        gradePreviewLabel.setFont(gradePreviewLabel.getFont().deriveFont(Font.BOLD));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Student:"));
        topPanel.add(studentBox);
        topPanel.add(new JLabel("Course:"));
        topPanel.add(courseBox);
        topPanel.add(new JLabel("Marks (0-100):"));
        topPanel.add(marksSpinner);
        topPanel.add(gradePreviewLabel);
        JButton saveButton = new JButton("Save Marks");
        topPanel.add(saveButton);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton exportButton = new JButton("Export to CSV");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(exportButton);
        add(bottomPanel, BorderLayout.SOUTH);

        studentBox.addActionListener(e -> reloadEnrollmentsForSelectedStudent());
        courseBox.addActionListener(e -> syncFormToSelectedCourse());
        marksSpinner.addChangeListener(e -> updateGradePreview());
        saveButton.addActionListener(e -> saveMarks());
        exportButton.addActionListener(e -> UiUtils.exportToCsv(this, table, "marks.csv"));

        reloadStudents();
    }

    private void reloadStudents() {
        try {
            Student previouslySelected = (Student) studentBox.getSelectedItem();
            studentBox.removeAllItems();
            for (Student s : studentDAO.findAll()) {
                studentBox.addItem(s);
            }
            if (previouslySelected != null) {
                selectStudentById(previouslySelected.getId());
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load students.", e);
        }
        reloadEnrollmentsForSelectedStudent();
    }

    private void selectStudentById(int id) {
        for (int i = 0; i < studentBox.getItemCount(); i++) {
            if (studentBox.getItemAt(i).getId() == id) {
                studentBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void reloadEnrollmentsForSelectedStudent() {
        Student student = (Student) studentBox.getSelectedItem();
        courseBox.removeAllItems();
        tableModel.setRowCount(0);
        if (student == null) {
            updateGradePreview();
            return;
        }
        try {
            List<Enrollment> enrollments = enrollmentDAO.findByStudent(student.getId());
            for (Enrollment en : enrollments) {
                courseBox.addItem(en);
                tableModel.addRow(new Object[]{en.getCourseCode(), en.getCourseName(), en.getMarks(), en.getGrade()});
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load this student's enrolled courses.", e);
        }
        syncFormToSelectedCourse();
    }

    private void syncFormToSelectedCourse() {
        Enrollment selected = (Enrollment) courseBox.getSelectedItem();
        marksSpinner.setValue(selected != null && selected.getMarks() != null ? selected.getMarks() : 0);
        updateGradePreview();
    }

    private void updateGradePreview() {
        Enrollment selected = (Enrollment) courseBox.getSelectedItem();
        if (selected == null) {
            gradePreviewLabel.setText("Grade: -");
            return;
        }
        int marks = (int) marksSpinner.getValue();
        gradePreviewLabel.setText("Grade: " + gradeForMarks(marks));
    }

    private void saveMarks() {
        Student student = (Student) studentBox.getSelectedItem();
        Enrollment selected = (Enrollment) courseBox.getSelectedItem();
        if (student == null || selected == null) {
            JOptionPane.showMessageDialog(this, "Please select both a student and a course.",
                    "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int marks = (int) marksSpinner.getValue();
        String grade = gradeForMarks(marks);
        try {
            enrollmentDAO.updateMarks(selected.getId(), marks, grade);
            reloadEnrollmentsForSelectedStudent();
            JOptionPane.showMessageDialog(this,
                    "Saved: " + selected.getCourseCode() + " - " + marks + " marks (" + grade + ").",
                    "Marks Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to save marks.", e);
        }
    }

    /** Standard grading scale: derives a letter grade from a 0-100 mark. */
    private static String gradeForMarks(int marks) {
        if (marks >= 70) {
            return "A";
        } else if (marks >= 60) {
            return "B";
        } else if (marks >= 50) {
            return "C";
        } else if (marks >= 40) {
            return "D";
        }
        return "F";
    }

    @Override
    public void refresh() {
        reloadStudents();
    }
}
