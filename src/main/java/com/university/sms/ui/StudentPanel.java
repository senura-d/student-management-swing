package com.university.sms.ui;

import com.university.sms.dao.EnrollmentDAO;
import com.university.sms.dao.StudentDAO;
import com.university.sms.model.Course;
import com.university.sms.model.Enrollment;
import com.university.sms.model.Student;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CRUD screen for students: search/filter table plus add/edit/delete. */
public class StudentPanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS =
            {"ID", "Full Name", "Email", "DOB", "Gender", "Contact No", "Enrollment Date"};

    private final StudentDAO studentDAO = new StudentDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    private List<Student> currentStudents = new ArrayList<>();

    public StudentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        setColumnWidths(table, 40, 160, 180, 90, 70, 110, 130);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        searchPanel.add(new JLabel("Search (name/email):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Export to CSV");
        JButton viewButton = new JButton("View Details");
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        viewButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        buttonPanel.add(exportButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        searchField.addActionListener(e -> search()); // Enter in the search box triggers a search
        searchButton.addActionListener(e -> search());
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            loadStudents();
        });
        exportButton.addActionListener(e -> UiUtils.exportToCsv(this, table, "students.csv"));
        viewButton.addActionListener(e -> showDetailDialog());
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelected());
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            viewButton.setEnabled(hasSelection);
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showDetailDialog();
                }
            }
        });

        loadStudents();
    }

    private static void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void loadStudents() {
        try {
            currentStudents = studentDAO.findAll();
            populateTable(currentStudents);
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load students.", e);
        }
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadStudents();
            return;
        }
        try {
            currentStudents = studentDAO.search(keyword);
            populateTable(currentStudents);
        } catch (SQLException e) {
            UiUtils.showError(this, "Search failed.", e);
        }
    }

    private void populateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getId(), s.getFullName(), s.getEmail(),
                    s.getDob(), s.getGender(), s.getContactNo(), s.getEnrollmentDate()
            });
        }
    }

    private Student getSelectedStudent() {
        int row = table.getSelectedRow();
        return row == -1 ? null : currentStudents.get(row);
    }

    private void showDetailDialog() {
        Student selected = getSelectedStudent();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to view.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new StudentDetailDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected).setVisible(true);
    }

    private void showAddDialog() {
        StudentFormDialog dialog =
                new StudentFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        Student result = dialog.getResult();
        if (result == null) {
            return;
        }
        try {
            studentDAO.insert(result);
            enrollInSelectedCourseIfAny(result, dialog.getSelectedCourse());
            loadStudents();
        } catch (SQLException e) {
            if (UiUtils.isDuplicateEntry(e)) {
                JOptionPane.showMessageDialog(this,
                        "A student with that email already exists.",
                        "Duplicate Email", JOptionPane.WARNING_MESSAGE);
            } else {
                UiUtils.showError(this, "Failed to add student.", e);
            }
        }
    }

    private void enrollInSelectedCourseIfAny(Student newStudent, Course course) {
        if (course == null) {
            return;
        }
        try {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(newStudent.getId());
            enrollment.setCourseId(course.getId());
            enrollment.setEnrolledDate(LocalDate.now());
            enrollmentDAO.enroll(enrollment);
        } catch (SQLException e) {
            UiUtils.showError(this, "Student was added, but enrolling them in "
                    + course.getCourseCode() + " failed. You can enroll them manually from the Enrollments tab.", e);
        }
    }

    private void showEditDialog() {
        Student selected = getSelectedStudent();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        StudentFormDialog dialog =
                new StudentFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        Student result = dialog.getResult();
        if (result == null) {
            return;
        }
        try {
            studentDAO.update(result);
            loadStudents();
        } catch (SQLException e) {
            if (UiUtils.isDuplicateEntry(e)) {
                JOptionPane.showMessageDialog(this,
                        "Another student with that email already exists.",
                        "Duplicate Email", JOptionPane.WARNING_MESSAGE);
            } else {
                UiUtils.showError(this, "Failed to update student.", e);
            }
        }
    }

    private void deleteSelected() {
        Student selected = getSelectedStudent();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete student \"" + selected.getFullName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            studentDAO.delete(selected.getId());
            loadStudents();
        } catch (SQLException e) {
            if (UiUtils.isForeignKeyViolation(e)) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete this student because they have existing course enrollments.\n"
                                + "Drop their enrollments first from the Enrollments tab.",
                        "Delete Blocked", JOptionPane.ERROR_MESSAGE);
            } else {
                UiUtils.showError(this, "Failed to delete student.", e);
            }
        }
    }

    @Override
    public void refresh() {
        loadStudents();
    }
}
