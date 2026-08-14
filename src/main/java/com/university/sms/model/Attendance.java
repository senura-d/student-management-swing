package com.university.sms.model;

import java.time.LocalDate;

/**
 * Mirrors a row in the attendance table. courseCode/courseName are only
 * populated when the row comes from a query that joins in the courses
 * table (see AttendanceDAO.findByStudentAndCourse) - they are not stored
 * on the attendance table itself.
 */
public class Attendance {

    public static final String PRESENT = "PRESENT";
    public static final String ABSENT = "ABSENT";

    private int id;
    private int studentId;
    private int courseId;
    private LocalDate attendanceDate;
    private String status;

    private String courseCode;
    private String courseName;

    public Attendance() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
