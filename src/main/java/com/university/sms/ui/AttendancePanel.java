package com.university.sms.ui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.university.sms.dao.AttendanceDAO;
import com.university.sms.dao.EnrollmentDAO;
import com.university.sms.dao.StudentDAO;
import com.university.sms.model.Attendance;
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
import java.util.List;

/** Marks per-course attendance for a student on a chosen date and shows their attendance history. */
public class AttendancePanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS = {"Date", "Status"};
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private final StudentDAO studentDAO = new StudentDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private final JComboBox<Student> studentBox = new JComboBox<>();
    private final JComboBox<Course> courseBox = new JComboBox<>();
    private final DatePicker datePicker;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel summaryLabel = new JLabel(" ");

    public AttendancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra(DATE_PATTERN);
        datePicker = new DatePicker(settings);
        datePicker.setDateToToday();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Student:"));
        topPanel.add(studentBox);
        topPanel.add(new JLabel("Course:"));
        topPanel.add(courseBox);
        topPanel.add(new JLabel("Date:"));
        topPanel.add(datePicker);
        JButton presentButton = new JButton("Mark Present");
        JButton absentButton = new JButton("Mark Absent");
        topPanel.add(presentButton);
        topPanel.add(absentButton);
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
        JPanel exportWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportWrap.add(exportButton);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(summaryLabel, BorderLayout.WEST);
        bottomPanel.add(exportWrap, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        studentBox.addActionListener(e -> reloadCoursesForSelectedStudent());
        courseBox.addActionListener(e -> loadAttendanceForSelection());
        presentButton.addActionListener(e -> markSelected(Attendance.PRESENT));
        absentButton.addActionListener(e -> markSelected(Attendance.ABSENT));
        exportButton.addActionListener(e -> UiUtils.exportToCsv(this, table, "attendance.csv"));

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
        reloadCoursesForSelectedStudent();
    }

    private void selectStudentById(int id) {
        for (int i = 0; i < studentBox.getItemCount(); i++) {
            if (studentBox.getItemAt(i).getId() == id) {
                studentBox.setSelectedIndex(i);
                return;
            }
        }
    }

    /** Only courses the selected student is actually enrolled in can have attendance marked. */
    private void reloadCoursesForSelectedStudent() {
        Student student = (Student) studentBox.getSelectedItem();
        Course previouslySelected = (Course) courseBox.getSelectedItem();
        courseBox.removeAllItems();
        if (student == null) {
            loadAttendanceForSelection();
            return;
        }
        try {
            for (Enrollment en : enrollmentDAO.findByStudent(student.getId())) {
                Course course = new Course();
                course.setId(en.getCourseId());
                course.setCourseCode(en.getCourseCode());
                course.setCourseName(en.getCourseName());
                course.setCredits(en.getCredits());
                courseBox.addItem(course);
            }
            if (previouslySelected != null) {
                selectCourseById(previouslySelected.getId());
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load this student's enrolled courses.", e);
        }
        loadAttendanceForSelection();
    }

    private void selectCourseById(int id) {
        for (int i = 0; i < courseBox.getItemCount(); i++) {
            if (courseBox.getItemAt(i).getId() == id) {
                courseBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadAttendanceForSelection() {
        Student student = (Student) studentBox.getSelectedItem();
        Course course = (Course) courseBox.getSelectedItem();
        tableModel.setRowCount(0);
        if (student == null) {
            summaryLabel.setText(" ");
            return;
        }
        if (course == null) {
            summaryLabel.setText("This student is not enrolled in any courses yet.");
            return;
        }
        try {
            List<Attendance> records = attendanceDAO.findByStudentAndCourse(student.getId(), course.getId());
            int present = 0;
            for (Attendance a : records) {
                tableModel.addRow(new Object[]{a.getAttendanceDate(), a.getStatus()});
                if (Attendance.PRESENT.equals(a.getStatus())) {
                    present++;
                }
            }
            int total = records.size();
            String rate = total == 0
                    ? "no attendance recorded yet"
                    : String.format("%d/%d present (%.0f%%)", present, total, present * 100.0 / total);
            summaryLabel.setText(course.getCourseCode() + " attendance: " + rate);
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load attendance.", e);
        }
    }

    private void markSelected(String status) {
        Student student = (Student) studentBox.getSelectedItem();
        Course course = (Course) courseBox.getSelectedItem();
        LocalDate date = datePicker.getDate();
        if (student == null || course == null) {
            JOptionPane.showMessageDialog(this, "Please select both a student and a course.",
                    "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Please choose a date.",
                    "Missing Date", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Attendance attendance = new Attendance();
        attendance.setStudentId(student.getId());
        attendance.setCourseId(course.getId());
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        try {
            attendanceDAO.markAttendance(attendance);
            loadAttendanceForSelection();
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to mark attendance.", e);
        }
    }

    @Override
    public void refresh() {
        reloadStudents();
    }
}
