package com.university.sms.ui;

import com.university.sms.dao.DashboardDAO;
import com.university.sms.model.AttendanceOverview;
import com.university.sms.model.AttendanceTrendPoint;
import com.university.sms.model.CourseEnrollmentCount;
import com.university.sms.model.GradeCount;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

/** Home tab: at-a-glance counts plus enrollment/grade/attendance charts, all via aggregate SQL. */
public class DashboardPanel extends JPanel implements Refreshable {

    // Soft pastel palette used across every dashboard chart instead of JFreeChart's saturated defaults.
    private static final Color LIGHT_BLUE = new Color(0x93C5FD);
    private static final Color LIGHT_GREEN = new Color(0x86EFAC);
    private static final Color LIGHT_PURPLE = new Color(0xC4B5FD);
    private static final Color LIGHT_ORANGE = new Color(0xFDBA74);
    private static final Color LIGHT_PINK = new Color(0xF9A8D4);
    private static final Color LIGHT_YELLOW = new Color(0xFDE68A);
    private static final Color LIGHT_RED = new Color(0xFCA5A5);
    private static final Color LIGHT_TEAL = new Color(0x5EEAD4);
    private static final Color[] PALETTE =
            {LIGHT_BLUE, LIGHT_GREEN, LIGHT_PURPLE, LIGHT_ORANGE, LIGHT_PINK, LIGHT_YELLOW, LIGHT_RED, LIGHT_TEAL};
    private static final Color PLOT_BACKGROUND = new Color(0xF8FAFC);

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private final JLabel studentCountLabel = new JLabel("-", SwingConstants.CENTER);
    private final JLabel courseCountLabel = new JLabel("-", SwingConstants.CENTER);
    private final JLabel enrollmentCountLabel = new JLabel("-", SwingConstants.CENTER);

    private final DefaultCategoryDataset enrollmentsPerCourseDataset = new DefaultCategoryDataset();
    private final DefaultPieDataset<String> gradeDistributionDataset = new DefaultPieDataset<>();
    private final DefaultPieDataset<String> attendanceDataset = new DefaultPieDataset<>();
    private final DefaultCategoryDataset attendanceTrendDataset = new DefaultCategoryDataset();

    private final PiePlot<String> gradePiePlot;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.add(createStatCard("Total Students", studentCountLabel));
        statsPanel.add(createStatCard("Total Courses", courseCountLabel));
        statsPanel.add(createStatCard("Total Enrollments", enrollmentCountLabel));
        add(statsPanel, BorderLayout.NORTH);

        JFreeChart enrollmentChart = ChartFactory.createBarChart(
                "Enrollments per Course", "Course", "Students",
                enrollmentsPerCourseDataset, PlotOrientation.VERTICAL, false, true, false);
        lightenChart(enrollmentChart);
        CategoryPlot enrollmentPlot = enrollmentChart.getCategoryPlot();
        BarRenderer barRenderer = (BarRenderer) enrollmentPlot.getRenderer();
        barRenderer.setSeriesPaint(0, LIGHT_BLUE);
        barRenderer.setShadowVisible(false);

        JFreeChart gradeChart = ChartFactory.createPieChart(
                "Grade Distribution", gradeDistributionDataset, true, true, false);
        lightenChart(gradeChart);
        @SuppressWarnings("unchecked")
        PiePlot<String> castGradePlot = (PiePlot<String>) gradeChart.getPlot();
        gradePiePlot = castGradePlot;

        JFreeChart attendanceChart = ChartFactory.createPieChart(
                "Attendance Overview", attendanceDataset, true, true, false);
        lightenChart(attendanceChart);
        @SuppressWarnings("unchecked")
        PiePlot<String> attendancePiePlot = (PiePlot<String>) attendanceChart.getPlot();
        attendancePiePlot.setSectionPaint("Present", LIGHT_GREEN);
        attendancePiePlot.setSectionPaint("Absent", LIGHT_RED);

        JFreeChart trendChart = ChartFactory.createLineChart(
                "Attendance Rate Trend", "Date", "Attendance Rate (%)",
                attendanceTrendDataset, PlotOrientation.VERTICAL, false, true, false);
        lightenChart(trendChart);
        CategoryPlot trendPlot = trendChart.getCategoryPlot();
        LineAndShapeRenderer trendRenderer = (LineAndShapeRenderer) trendPlot.getRenderer();
        trendRenderer.setSeriesPaint(0, LIGHT_PURPLE);
        trendRenderer.setSeriesStroke(0, new BasicStroke(3f));

        JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsPanel.add(new ChartPanel(enrollmentChart));
        chartsPanel.add(new ChartPanel(gradeChart));
        chartsPanel.add(new ChartPanel(attendanceChart));
        chartsPanel.add(new ChartPanel(trendChart));
        add(chartsPanel, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStats());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadStats();
    }

    private static void lightenChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(PLOT_BACKGROUND);
        chart.getPlot().setOutlineVisible(false);
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

    private void loadStats() {
        try {
            studentCountLabel.setText(String.valueOf(dashboardDAO.countStudents()));
            courseCountLabel.setText(String.valueOf(dashboardDAO.countCourses()));
            enrollmentCountLabel.setText(String.valueOf(dashboardDAO.countEnrollments()));

            enrollmentsPerCourseDataset.clear();
            for (CourseEnrollmentCount c : dashboardDAO.enrollmentsPerCourse()) {
                enrollmentsPerCourseDataset.addValue(c.enrolled(), "Enrolled", c.courseCode());
            }

            gradeDistributionDataset.clear();
            int colorIndex = 0;
            for (GradeCount g : dashboardDAO.gradeDistribution()) {
                gradeDistributionDataset.setValue(g.grade(), g.count());
                gradePiePlot.setSectionPaint(g.grade(), PALETTE[colorIndex % PALETTE.length]);
                colorIndex++;
            }

            AttendanceOverview overview = dashboardDAO.attendanceOverview();
            attendanceDataset.clear();
            attendanceDataset.setValue("Present", overview.present());
            attendanceDataset.setValue("Absent", overview.absent());

            attendanceTrendDataset.clear();
            for (AttendanceTrendPoint point : dashboardDAO.attendanceTrend()) {
                attendanceTrendDataset.addValue(point.presentRate(), "Attendance Rate", point.date().toString());
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
