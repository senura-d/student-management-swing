-- Student Management System - database schema
-- Recreates the database and all tables from scratch.
-- Usage: mysql -u root -p < schema.sql

DROP DATABASE IF EXISTS student_management_system_swing;
CREATE DATABASE student_management_system_swing;
USE student_management_system_swing;

-- Students enrolled at the university
CREATE TABLE students (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    full_name         VARCHAR(100) NOT NULL,
    email             VARCHAR(100) NOT NULL UNIQUE,
    dob               DATE NOT NULL,
    gender            VARCHAR(20) NOT NULL,
    contact_no        VARCHAR(20),
    enrollment_date   DATE NOT NULL,
    photo_path        VARCHAR(255)
);

-- Courses offered by the university
CREATE TABLE courses (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    course_code   VARCHAR(20) NOT NULL UNIQUE,
    course_name   VARCHAR(100) NOT NULL,
    credits       INT NOT NULL CHECK (credits > 0)
);

-- Links students to the courses they are enrolled in
CREATE TABLE enrollments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT NOT NULL,
    course_id       INT NOT NULL,
    enrolled_date   DATE NOT NULL,
    marks           INT CHECK (marks BETWEEN 0 AND 100),
    grade           VARCHAR(2),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)
);

-- Per-course attendance records for enrolled students
CREATE TABLE attendance (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    student_id        INT NOT NULL,
    course_id         INT NOT NULL,
    attendance_date   DATE NOT NULL,
    status            VARCHAR(10) NOT NULL,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_attendance_record UNIQUE (student_id, course_id, attendance_date)
);

-- Admin accounts allowed to log in and use the system
CREATE TABLE admins (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(64) NOT NULL,
    password_salt   VARCHAR(32) NOT NULL
);

-- Default admin account - username: admin, password: admin123
-- (password_hash is SHA-256 of password_salt + password; change this after first login)
INSERT INTO admins (username, password_hash, password_salt) VALUES (
    'admin',
    '3e744ce2cc21370f4ad5c14448943f783b6b28103c903bd00481d1d84c920612',
    '7cd103870316bdb08b14d727ebd3e9da'
);
