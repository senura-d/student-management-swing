package com.university.sms.ui;

import com.university.sms.dao.DashboardDAO;
import com.university.sms.model.CourseEnrollmentCount;
import com.university.sms.model.GradeCount;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

/** Home tab: at-a-glance counts plus enrollment/grade breakdowns, all via aggregate SQL. */
public class DashboardPanel extends JPanel implements Refreshable {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private final JLabel studentCountLabel = new JLabel("-", SwingConstants.CENTER);
    private final JLabel courseCountLabel = new JLabel("-", SwingConstants.CENTER);
    private final JLabel enrollmentCountLabel = new JLabel("-", SwingConstants.CENTER);

    private final DefaultTableModel enrollmentsPerCourseModel;
    private final DefaultTableModel gradeDistributionModel;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.add(createStatCard("Total Students", studentCountLabel));
        statsPanel.add(createStatCard("Total Courses", courseCountLabel));
        statsPanel.add(createStatCard("Total Enrollments", enrollmentCountLabel));
        add(statsPanel, BorderLayout.NORTH);

        enrollmentsPerCourseModel = readOnlyModel("Course Code", "Course Name", "Enrolled");
        JTable enrollmentsPerCourseTable = new JTable(enrollmentsPerCourseModel);
        enrollmentsPerCourseTable.setRowHeight(24);

        gradeDistributionModel = readOnlyModel("Grade", "Count");
        JTable gradeDistributionTable = new JTable(gradeDistributionModel);
        gradeDistributionTable.setRowHeight(24);

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        tablesPanel.add(wrapWithTitle("Enrollments per Course", enrollmentsPerCourseTable));
        tablesPanel.add(wrapWithTitle("Grade Distribution", gradeDistributionTable));
        add(tablesPanel, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStats());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadStats();
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(10, 10, 15, 10)));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 32f));
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel wrapWithTitle(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadStats() {
        try {
            studentCountLabel.setText(String.valueOf(dashboardDAO.countStudents()));
            courseCountLabel.setText(String.valueOf(dashboardDAO.countCourses()));
            enrollmentCountLabel.setText(String.valueOf(dashboardDAO.countEnrollments()));

            enrollmentsPerCourseModel.setRowCount(0);
            for (CourseEnrollmentCount c : dashboardDAO.enrollmentsPerCourse()) {
                enrollmentsPerCourseModel.addRow(new Object[]{c.courseCode(), c.courseName(), c.enrolled()});
            }

            gradeDistributionModel.setRowCount(0);
            for (GradeCount g : dashboardDAO.gradeDistribution()) {
                gradeDistributionModel.addRow(new Object[]{g.grade(), g.count()});
            }
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load dashboard statistics.", e);
        }
    }

    @Override
    public void refresh() {
        loadStats();
    }
}
