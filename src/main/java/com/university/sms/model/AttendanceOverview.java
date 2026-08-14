package com.university.sms.model;

/** Present/absent totals across every attendance record, used by the dashboard's attendance chart. */
public record AttendanceOverview(int present, int absent) {
}
