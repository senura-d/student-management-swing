package com.university.sms.model;

import java.time.LocalDate;

/** One point on the dashboard's attendance-rate-over-time line chart. */
public record AttendanceTrendPoint(LocalDate date, double presentRate) {
}
