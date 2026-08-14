package com.university.sms.dao;

import com.university.sms.db.DBConnection;
import com.university.sms.model.Student;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the students table lives here. */
public class StudentDAO {

    public List<Student> findAll() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY full_name";
        List<Student> students = new ArrayList<>();
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        }
        return students;
    }

    public List<Student> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM students WHERE full_name LIKE ? OR email LIKE ? ORDER BY full_name";
        List<Student> students = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }
        }
        return students;
    }

    public void insert(Student s) throws SQLException {
        String sql = "INSERT INTO students (full_name, email, dob, gender, contact_no, enrollment_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            bindParams(ps, s);
            ps.executeUpdate();
        }
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, email = ?, dob = ?, gender = ?, "
                + "contact_no = ?, enrollment_date = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            bindParams(ps, s);
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bindParams(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getFullName());
        ps.setString(2, s.getEmail());
        ps.setDate(3, Date.valueOf(s.getDob()));
        ps.setString(4, s.getGender());
        ps.setString(5, s.getContactNo());
        ps.setDate(6, Date.valueOf(s.getEnrollmentDate()));
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setDob(rs.getDate("dob").toLocalDate());
        s.setGender(rs.getString("gender"));
        s.setContactNo(rs.getString("contact_no"));
        s.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());
        return s;
    }
}
