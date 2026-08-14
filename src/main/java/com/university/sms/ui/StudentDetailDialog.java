package com.university.sms.ui;

import com.university.sms.dao.AttendanceDAO;
import com.university.sms.dao.EnrollmentDAO;
import com.university.sms.model.Attendance;
import com.university.sms.model.Enrollment;
import com.university.sms.model.Student;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Read-only view of everything known about one student: profile, courses, and attendance. */
public class StudentDetailDialog extends JDialog {

    private static final int PHOTO_SIZE = 150;

    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public StudentDetailDialog(JFrame owner, Student student) {
        super(owner, "Student Details - " + student.getFullName(), true);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildProfilePanel(student), BorderLayout.NORTH);

        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        tablesPanel.add(buildEnrollmentsPanel(student));
        tablesPanel.add(buildAttendancePanel(student));
        root.add(tablesPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeButton);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(650, 600);
        setLocationRelativeTo(owner);
    }

    private JPanel buildProfilePanel(Student student) {
        JLabel photoLabel = new JLabel("No Photo", SwingConstants.CENTER);
        photoLabel.setPreferredSize(new Dimension(PHOTO_SIZE, PHOTO_SIZE));
        photoLabel.setBorder(BorderFactory.createLineBorder(photoLabel.getForeground().brighter()));
        ImageIcon icon = PhotoUtil.loadThumbnail(student.getPhotoPath(), PHOTO_SIZE);
        if (icon != null) {
            photoLabel.setIcon(icon);
            photoLabel.setText(null);
        }

        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 8, 6));
        infoGrid.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        addField(infoGrid, "Full Name:", student.getFullName());
        addField(infoGrid, "Email:", student.getEmail());
        addField(infoGrid, "Date of Birth:", student.getDob().toString());
        addField(infoGrid, "Gender:", student.getGender());
        addField(infoGrid, "Contact No:", student.getContactNo());
        addField(infoGrid, "Enrollment Date:", student.getEnrollmentDate().toString());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(photoLabel, BorderLayout.WEST);
        panel.add(infoGrid, BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel grid, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(java.awt.Font.BOLD));
        grid.add(labelComponent);
        grid.add(new JLabel(value == null ? "-" : value));
    }

    private JPanel buildEnrollmentsPanel(Student student) {
        DefaultTableModel model = readOnlyModel("Course Code", "Course Name", "Credits", "Enrolled Date", "Grade");
        try {
            for (Enrollment e : enrollmentDAO.findByStudent(student.getId())) {
                model.addRow(new Object[]{
                        e.getCourseCode(), e.getCourseName(), e.getCredits(), e.getEnrolledDate(), e.getGrade()
                });
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load enrolled courses.", e);
        }
        return wrapWithTitle("Enrolled Courses", model);
    }

    private JPanel buildAttendancePanel(Student student) {
        DefaultTableModel model = readOnlyModel("Course", "Present", "Absent", "Total", "Attendance Rate");
        try {
            Map<String, int[]> perCourse = new LinkedHashMap<>(); // [present, absent]
            for (Attendance a : attendanceDAO.findByStudent(student.getId())) {
                String key = a.getCourseCode() + " - " + a.getCourseName();
                int[] counts = perCourse.computeIfAbsent(key, k -> new int[2]);
                if (Attendance.PRESENT.equals(a.getStatus())) {
                    counts[0]++;
                } else {
                    counts[1]++;
                }
            }
            for (Map.Entry<String, int[]> entry : perCourse.entrySet()) {
                int present = entry.getValue()[0];
                int absent = entry.getValue()[1];
                int total = present + absent;
                String rate = total == 0 ? "-" : String.format("%.0f%%", present * 100.0 / total);
                model.addRow(new Object[]{entry.getKey(), present, absent, total, rate});
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load attendance.", e);
        }
        return wrapWithTitle("Attendance Summary", model);
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel wrapWithTitle(String title, DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        JTable table = new JTable(model);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}
