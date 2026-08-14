package com.university.sms.dao;

import com.university.sms.db.DBConnection;
import com.university.sms.model.Attendance;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the attendance table lives here. */
public class AttendanceDAO {

    /** Marks attendance for a student/course/date. Re-marking the same date just updates the status. */
    public void markAttendance(Attendance a) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status) "
                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getCourseId());
            ps.setDate(3, Date.valueOf(a.getAttendanceDate()));
            ps.setString(4, a.getStatus());
            ps.executeUpdate();
        }
    }

    /** Attendance history for one student in one course, joined with course details, most recent first. */
    public List<Attendance> findByStudentAndCourse(int studentId, int courseId) throws SQLException {
        String sql = "SELECT a.id, a.student_id, a.course_id, a.attendance_date, a.status, "
                + "c.course_code, c.course_name "
                + "FROM attendance a JOIN courses c ON a.course_id = c.id "
                + "WHERE a.student_id = ? AND a.course_id = ? ORDER BY a.attendance_date DESC";
        List<Attendance> records = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        }
        return records;
    }

    /** All attendance records for a student across every course, joined with course details. */
    public List<Attendance> findByStudent(int studentId) throws SQLException {
        String sql = "SELECT a.id, a.student_id, a.course_id, a.attendance_date, a.status, "
                + "c.course_code, c.course_name "
                + "FROM attendance a JOIN courses c ON a.course_id = c.id "
                + "WHERE a.student_id = ? ORDER BY c.course_code, a.attendance_date DESC";
        List<Attendance> records = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        }
        return records;
    }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());
        a.setStatus(rs.getString("status"));
        a.setCourseCode(rs.getString("course_code"));
        a.setCourseName(rs.getString("course_name"));
        return a;
    }
}
