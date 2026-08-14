package com.university.sms.model;

/** Plain data holder mirroring a row in the courses table. */
public class Course {

    private int id;
    private String courseCode;
    private String courseName;
    private int credits;

    public Course() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        // Used whenever a Course appears in a combo box (e.g. EnrollmentPanel)
        return courseCode + " - " + courseName;
    }
}
