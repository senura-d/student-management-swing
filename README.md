# Student Management System

A desktop application for managing university students, courses, and enrollments.
Built with Java Swing (UI) and JDBC (data access) against a MySQL database — no
web framework, no Spring; just plain Java following a 3-layer DAO architecture.

## Tech Stack

- Java 17+
- Swing (UI)
- JDBC with MySQL Connector/J (data access)
- MySQL 8.0+
- Maven (build/dependency management)

## Project Structure

```
src/main/java/com/university/sms/
├── Main.java              Application entry point
├── model/                 Plain POJOs: Student, Course, Enrollment
├── dao/                   StudentDAO, CourseDAO, EnrollmentDAO — all SQL lives here
├── db/                    DBConnection — single shared JDBC connection helper
└── ui/                    MainFrame + one panel per entity, plus shared dialogs/helpers
src/main/resources/
└── db.properties          Database connection settings (edit this before running)
schema.sql                 Recreates the database and all tables from scratch
```

## Prerequisites

- **JDK 17 or newer**
- **MySQL 8.0 or newer**, running locally with a root (or other) user you can log in as
- **Maven 3.8+** (if you don't have it, open the project in an IDE that bundles its
  own Maven, such as IntelliJ IDEA or Eclipse — no separate install needed)

## Setup

### 1. Create the database

From a terminal with `mysql` on your PATH, in this project's folder:

```bash
mysql -u root -p < schema.sql
```

(On Windows PowerShell, `<` redirection isn't supported — use
`Get-Content schema.sql | mysql -u root -p` instead.)

This drops and recreates the `student_management_system` database and all three
tables (`students`, `courses`, `enrollments`) with their foreign key constraints.

### 2. Configure the connection

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/student_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_password_here
```

(`allowPublicKeyRetrieval=true` is required for MySQL 8's default `caching_sha2_password`
authentication to work without SSL - omitting it causes a "Public Key Retrieval is not
allowed" error on connect.)

Update `db.user` / `db.password` to match your MySQL login. If MySQL is running on
a different host or port, update `db.url` accordingly.

### 3. Build and run

With Maven on your PATH:

```bash
mvn compile exec:java
```

Or build a standalone runnable jar and launch it directly:

```bash
mvn package
java -jar target/student-management-system.jar
```

Without Maven installed, open this folder in IntelliJ IDEA or Eclipse — both detect
the `pom.xml`, fetch dependencies automatically, and let you run `Main.java` directly
(right-click → Run, or the green arrow next to `main`).

A window titled "Student Management System" should open with four tabs: Dashboard,
Students, Courses, and Enrollments.

## Features

- **Dashboard** — at-a-glance totals (students, courses, enrollments), a table of
  enrollments per course, and a grade distribution table, all backed by aggregate
  SQL (`COUNT`, `GROUP BY`). Opens first, as a home screen for the app.
- **Students / Courses** — searchable table, add/edit with validation (required
  fields, email format, positive credit count), delete with a confirmation dialog.
  Deleting a student/course that still has enrollments is blocked with a clear
  explanation instead of a raw database error.
- **Enrollments** — enroll a student in a course from dropdowns; view a student's
  enrollments (joined with course details) in a table; drop an individual enrollment.
  Duplicate enrollments are rejected with a friendly message.
- **CSV export** — every table (Students, Courses, a student's Enrollments) has an
  "Export to CSV" button that saves the currently displayed rows to a file you choose.
- All database errors are caught and shown as readable dialogs — no stack traces
  reach the UI (full details are still logged to the console for debugging).
