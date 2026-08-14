package com.university.sms.dao;

import com.university.sms.db.DBConnection;
import com.university.sms.model.Enrollment;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the enrollments table lives here. */
public class EnrollmentDAO {

    /** Returns a student's enrollments joined with course details for display. */
    public List<Enrollment> findByStudent(int studentId) throws SQLException {
        String sql = "SELECT e.id, e.student_id, e.course_id, e.enrolled_date, e.marks, e.grade, "
                + "c.course_code, c.course_name, c.credits "
                + "FROM enrollments e JOIN courses c ON e.course_id = c.id "
                + "WHERE e.student_id = ? ORDER BY e.enrolled_date DESC";
        List<Enrollment> enrollments = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRow(rs));
                }
            }
        }
        return enrollments;
    }

    public void enroll(Enrollment e) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrolled_date, grade) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getCourseId());
            ps.setDate(3, Date.valueOf(e.getEnrolledDate()));
            ps.setString(4, e.getGrade());
            ps.executeUpdate();
        }
    }

    /** Records exam marks for an enrollment and the letter grade derived from them. */
    public void updateMarks(int enrollmentId, Integer marks, String grade) throws SQLException {
        String sql = "UPDATE enrollments SET marks = ?, grade = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            if (marks == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, marks);
            }
            ps.setString(2, grade);
            ps.setInt(3, enrollmentId);
            ps.executeUpdate();
        }
    }

    public void drop(int enrollmentId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
    }

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getInt("id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setCourseId(rs.getInt("course_id"));
        e.setEnrolledDate(rs.getDate("enrolled_date").toLocalDate());
        int marks = rs.getInt("marks");
        e.setMarks(rs.wasNull() ? null : marks);
        e.setGrade(rs.getString("grade"));
        e.setCourseCode(rs.getString("course_code"));
        e.setCourseName(rs.getString("course_name"));
        e.setCredits(rs.getInt("credits"));
        return e;
    }
}
