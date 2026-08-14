package com.university.sms.dao;

import com.university.sms.db.DBConnection;
import com.university.sms.model.AttendanceOverview;
import com.university.sms.model.AttendanceTrendPoint;
import com.university.sms.model.CourseEnrollmentCount;
import com.university.sms.model.GradeCount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Aggregate queries backing the Dashboard tab - counts and breakdowns, no single-row CRUD. */
public class DashboardDAO {

    public int countStudents() throws SQLException {
        return countRows("SELECT COUNT(*) FROM students");
    }

    public int countCourses() throws SQLException {
        return countRows("SELECT COUNT(*) FROM courses");
    }

    public int countEnrollments() throws SQLException {
        return countRows("SELECT COUNT(*) FROM enrollments");
    }

    private int countRows(String sql) throws SQLException {
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Every course with how many students are enrolled in it (0 if none). */
    public List<CourseEnrollmentCount> enrollmentsPerCourse() throws SQLException {
        String sql = "SELECT c.course_code, c.course_name, COUNT(e.id) AS enrolled "
                + "FROM courses c LEFT JOIN enrollments e ON e.course_id = c.id "
                + "GROUP BY c.id, c.course_code, c.course_name "
                + "ORDER BY c.course_code";
        List<CourseEnrollmentCount> results = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(new CourseEnrollmentCount(
                        rs.getString("course_code"), rs.getString("course_name"), rs.getInt("enrolled")));
            }
        }
        return results;
    }

    /** How many enrollments have been assigned each grade (ungraded enrollments are excluded). */
    public List<GradeCount> gradeDistribution() throws SQLException {
        String sql = "SELECT grade, COUNT(*) AS cnt FROM enrollments "
                + "WHERE grade IS NOT NULL GROUP BY grade ORDER BY grade";
        List<GradeCount> results = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(new GradeCount(rs.getString("grade"), rs.getInt("cnt")));
            }
        }
        return results;
    }

    /** Total present vs. absent counts across every attendance record in the system. */
    public AttendanceOverview attendanceOverview() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS cnt FROM attendance GROUP BY status";
        int present = 0;
        int absent = 0;
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if ("PRESENT".equals(rs.getString("status"))) {
                    present = rs.getInt("cnt");
                } else {
                    absent = rs.getInt("cnt");
                }
            }
        }
        return new AttendanceOverview(present, absent);
    }

    /** System-wide attendance rate for each date that has attendance recorded, oldest first. */
    public List<AttendanceTrendPoint> attendanceTrend() throws SQLException {
        String sql = "SELECT attendance_date, "
                + "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS present, COUNT(*) AS total "
                + "FROM attendance GROUP BY attendance_date ORDER BY attendance_date";
        List<AttendanceTrendPoint> results = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int present = rs.getInt("present");
                int total = rs.getInt("total");
                double rate = total == 0 ? 0 : present * 100.0 / total;
                results.add(new AttendanceTrendPoint(rs.getDate("attendance_date").toLocalDate(), rate));
            }
        }
        return results;
    }
}
