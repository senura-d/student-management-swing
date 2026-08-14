package com.university.sms.ui;

import com.university.sms.dao.CourseDAO;
import com.university.sms.dao.EnrollmentDAO;
import com.university.sms.dao.StudentDAO;
import com.university.sms.model.Course;
import com.university.sms.model.Enrollment;
import com.university.sms.model.Student;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Lets a student be enrolled in a course, shows their enrollments (via JOIN), and drops them. */
public class EnrollmentPanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS =
            {"Enrollment ID", "Course Code", "Course Name", "Credits", "Enrolled Date", "Grade"};

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final JComboBox<Student> studentBox = new JComboBox<>();
    private final JComboBox<Course> courseBox = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;

    private List<Enrollment> currentEnrollments = new ArrayList<>();

    public EnrollmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Student:"));
        topPanel.add(studentBox);
        topPanel.add(new JLabel("Course:"));
        topPanel.add(courseBox);
        JButton enrollButton = new JButton("Enroll");
        topPanel.add(enrollButton);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Export to CSV");
        JButton dropButton = new JButton("Drop Selected Enrollment");
        dropButton.setEnabled(false);
        bottomPanel.add(exportButton);
        bottomPanel.add(dropButton);
        add(bottomPanel, BorderLayout.SOUTH);

        studentBox.addActionListener(e -> loadEnrollmentsForSelectedStudent());
        enrollButton.addActionListener(e -> enrollSelected());
        exportButton.addActionListener(e -> UiUtils.exportToCsv(this, table, "enrollments.csv"));
        dropButton.addActionListener(e -> dropSelected());
        table.getSelectionModel().addListSelectionListener(e ->
                dropButton.setEnabled(table.getSelectedRow() != -1));

        reloadCombos();
    }

    private void reloadCombos() {
        try {
            Student previouslySelected = (Student) studentBox.getSelectedItem();
            studentBox.removeAllItems();
            for (Student s : studentDAO.findAll()) {
                studentBox.addItem(s);
            }
            if (previouslySelected != null) {
                selectStudentById(previouslySelected.getId());
            }

            courseBox.removeAllItems();
            for (Course c : courseDAO.findAll()) {
                courseBox.addItem(c);
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load students/courses.", e);
        }
        loadEnrollmentsForSelectedStudent();
    }

    private void selectStudentById(int id) {
        for (int i = 0; i < studentBox.getItemCount(); i++) {
            if (studentBox.getItemAt(i).getId() == id) {
                studentBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadEnrollmentsForSelectedStudent() {
        Student selected = (Student) studentBox.getSelectedItem();
        tableModel.setRowCount(0);
        currentEnrollments.clear();
        if (selected == null) {
            return;
        }
        try {
            currentEnrollments = enrollmentDAO.findByStudent(selected.getId());
            for (Enrollment en : currentEnrollments) {
                tableModel.addRow(new Object[]{
                        en.getId(), en.getCourseCode(), en.getCourseName(),
                        en.getCredits(), en.getEnrolledDate(), en.getGrade()
                });
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load enrollments.", e);
        }
    }

    private void enrollSelected() {
        Student student = (Student) studentBox.getSelectedItem();
        Course course = (Course) courseBox.getSelectedItem();
        if (student == null || course == null) {
            JOptionPane.showMessageDialog(this, "Please select both a student and a course.",
                    "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setEnrolledDate(LocalDate.now());
        try {
            enrollmentDAO.enroll(enrollment);
            loadEnrollmentsForSelectedStudent();
        } catch (SQLException e) {
            if (UiUtils.isDuplicateEntry(e)) {
                JOptionPane.showMessageDialog(this,
                        student.getFullName() + " is already enrolled in " + course.getCourseCode() + ".",
                        "Already Enrolled", JOptionPane.WARNING_MESSAGE);
            } else {
                UiUtils.showError(this, "Failed to enroll student.", e);
            }
        }
    }

    private void dropSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an enrollment to drop.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Enrollment selected = currentEnrollments.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Drop this enrollment (" + selected.getCourseCode() + ")?",
                "Confirm Drop", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            enrollmentDAO.drop(selected.getId());
            loadEnrollmentsForSelectedStudent();
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to drop enrollment.", e);
        }
    }

    @Override
    public void refresh() {
        reloadCombos();
    }
}
