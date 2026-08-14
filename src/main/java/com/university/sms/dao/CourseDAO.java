package com.university.sms.dao;

import com.university.sms.db.DBConnection;
import com.university.sms.model.Course;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the courses table lives here. */
public class CourseDAO {

    public List<Course> findAll() throws SQLException {
        String sql = "SELECT * FROM courses ORDER BY course_code";
        List<Course> courses = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(mapRow(rs));
            }
        }
        return courses;
    }

    public List<Course> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_code LIKE ? OR course_name LIKE ? ORDER BY course_code";
        List<Course> courses = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapRow(rs));
                }
            }
        }
        return courses;
    }

    public void insert(Course c) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_name, credits) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            bindParams(ps, c);
            ps.executeUpdate();
        }
    }

    public void update(Course c) throws SQLException {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, credits = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            bindParams(ps, c);
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bindParams(PreparedStatement ps, Course c) throws SQLException {
        ps.setString(1, c.getCourseCode());
        ps.setString(2, c.getCourseName());
        ps.setInt(3, c.getCredits());
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setCourseName(rs.getString("course_name"));
        c.setCredits(rs.getInt("credits"));
        return c;
    }
}
