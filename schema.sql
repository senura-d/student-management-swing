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
    enrollment_date   DATE NOT NULL
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
    grade           VARCHAR(2),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)
);
