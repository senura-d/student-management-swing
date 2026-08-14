package com.university.sms.model;

/** One row of the "grade distribution" aggregate query used by the dashboard. */
public record GradeCount(String grade, int count) {
}
