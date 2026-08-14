package com.university.sms.ui;

import com.university.sms.dao.CourseDAO;
import com.university.sms.model.Course;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD screen for courses: search/filter table plus add/edit/delete. */
public class CoursePanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS = {"ID", "Course Code", "Course Name", "Credits"};

    private final CourseDAO courseDAO = new CourseDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    private List<Course> currentCourses = new ArrayList<>();

    public CoursePanel() {
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
        setColumnWidths(table, 40, 110, 240, 70);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        searchPanel.add(new JLabel("Search (code/name):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Export to CSV");
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        buttonPanel.add(exportButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        searchField.addActionListener(e -> search()); // Enter in the search box triggers a search
        searchButton.addActionListener(e -> search());
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            loadCourses();
        });
        exportButton.addActionListener(e -> UiUtils.exportToCsv(this, table, "courses.csv"));
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelected());
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        loadCourses();
    }

    private static void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void loadCourses() {
        try {
            currentCourses = courseDAO.findAll();
            populateTable(currentCourses);
        } catch (SQLException e) {
            UiUtils.showError(this, "Failed to load courses.", e);
        }
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadCourses();
            return;
        }
        try {
            currentCourses = courseDAO.search(keyword);
            populateTable(currentCourses);
        } catch (SQLException e) {
            UiUtils.showError(this, "Search failed.", e);
        }
    }

    private void populateTable(List<Course> courses) {
        tableModel.setRowCount(0);
        for (Course c : courses) {
            tableModel.addRow(new Object[]{c.getId(), c.getCourseCode(), c.getCourseName(), c.getCredits()});
        }
    }

    private Course getSelectedCourse() {
        int row = table.getSelectedRow();
        return row == -1 ? null : currentCourses.get(row);
    }

    private void showAddDialog() {
        CourseFormDialog dialog =
                new CourseFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        Course result = dialog.getResult();
        if (result == null) {
            return;
        }
        try {
            courseDAO.insert(result);
            loadCourses();
        } catch (SQLException e) {
            if (UiUtils.isDuplicateEntry(e)) {
                JOptionPane.showMessageDialog(this,
                        "A course with that code already exists.",
                        "Duplicate Course Code", JOptionPane.WARNING_MESSAGE);
            } else {
                UiUtils.showError(this, "Failed to add course.", e);
            }
        }
    }

    private void showEditDialog() {
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CourseFormDialog dialog =
                new CourseFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        Course result = dialog.getResult();
        if (result == null) {
            return;
        }
        try {
            courseDAO.update(result);
            loadCourses();
        } catch (SQLException e) {
            if (UiUtils.isDuplicateEntry(e)) {
                JOptionPane.showMessageDialog(this,
                        "Another course with that code already exists.",
                        "Duplicate Course Code", JOptionPane.WARNING_MESSAGE);
            } else {
                UiUtils.showError(this, "Failed to update course.", e);
            }
        }
    }

    private void deleteSelected() {
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete course \"" + selected.getCourseName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            courseDAO.delete(selected.getId());
            loadCourses();
        } catch (SQLException e) {
            if (UiUtils.isForeignKeyViolation(e)) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete this course because students are enrolled in it.\n"
                                + "Drop those enrollments first from the Enrollments tab.",
                        "Delete Blocked", JOptionPane.ERROR_MESSAGE);
            } else {
                UiUtils.showError(this, "Failed to delete course.", e);
            }
        }
    }

    @Override
    public void refresh() {
        loadCourses();
    }
}
