# Calendar Date Picker for Add/Edit Student Form

## Problem

The "Add/Edit Student" dialog (`StudentFormDialog`) currently uses plain `JTextField`s for "Date of Birth" and "Enrollment Date", requiring the user to type the date in `yyyy-MM-dd` format by hand. There is no calendar to pick a date from.

## Solution

Add the [LGoodDatePicker](https://github.com/LGoodDatePicker/LGoodDatePicker) library (MIT license, `java.time.LocalDate`-based) as a Maven dependency and use its `DatePicker` component in place of the two `JTextField`s.

### Dependency

Add to `pom.xml`:

```xml
<dependency>
    <groupId>com.github.lgooddatepicker</groupId>
    <artifactId>LGoodDatePicker</artifactId>
    <version>11.2.1</version>
</dependency>
```

### `StudentFormDialog.java`

- Replace `dobField` and `enrollmentDateField` (`JTextField`) with `DatePicker dobPicker` and `DatePicker enrollmentDatePicker`.
- Configure each picker's `DatePickerSettings` with a `yyyy-MM-dd` format, matching the existing on-screen format.
- Drop the `"(yyyy-MM-dd)"` hint from the two field labels — users will normally use the calendar drop-down instead of typing.
- `onSave()`: read dates via `picker.getDate()` (returns `null` when empty) instead of the old `parseDate(...)` regex/exception-based parsing. Keep the existing "field is required" validation messages, checking for `null` instead of an empty/unparseable string.
- Edit mode: populate with `dobPicker.setDate(existing.getDob())` instead of `setText(...)`.
- Add mode: `enrollmentDatePicker` still defaults to `LocalDate.now()`, same as today.
- Remove the now-unused `parseDate()` helper and `DATE_FORMAT` constant.

### Out of scope

- `CourseFormDialog` has no date fields — untouched.
- `Student` / `StudentDAO` already operate on `LocalDate` — no changes needed.
- No changes to validation rules beyond swapping how a missing date is detected (`null` vs. empty string).

## Testing

- Manual: run the app (`mvn exec:java`), open Add Student, use the calendar to pick a DOB and enrollment date, save, and confirm the student is created with the correct dates in the students table/list. Repeat for Edit Student, confirming existing dates pre-populate correctly in the pickers.
