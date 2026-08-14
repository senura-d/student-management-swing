package com.university.sms.model;

/** One row of the "enrollments per course" aggregate query used by the dashboard. */
public record CourseEnrollmentCount(String courseCode, String courseName, int enrolled) {
}
