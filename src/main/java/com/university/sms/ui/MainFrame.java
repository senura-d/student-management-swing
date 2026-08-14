package com.university.sms.ui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.Dimension;

/** Top-level application window: one tab per entity. */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 860);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(14f));
        tabs.addTab("Dashboard", new DashboardPanel());
        tabs.addTab("Students", new StudentPanel());
        tabs.addTab("Courses", new CoursePanel());
        tabs.addTab("Enrollments", new EnrollmentPanel());
        tabs.addTab("Marks", new MarksPanel());
        tabs.addTab("Attendance", new AttendancePanel());

        // Reload each tab's data when it's selected, so changes made in one
        // tab (e.g. a new student) show up immediately in another (e.g. the
        // student dropdown on the Enrollments tab).
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected instanceof Refreshable refreshable) {
                refreshable.refresh();
            }
        });

        add(tabs);
    }
}
