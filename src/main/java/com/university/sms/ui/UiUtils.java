package com.university.sms.ui;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Shared helpers so every panel shows DB errors the same friendly way
 * instead of leaking raw stack traces into dialogs, and exports tables
 * to CSV the same way.
 */
final class UiUtils {

    // MySQL error codes we translate into friendly messages
    private static final int FOREIGN_KEY_VIOLATION = 1451;
    private static final int DUPLICATE_ENTRY = 1062;

    private UiUtils() {
    }

    static void showError(Component parent, String message, SQLException e) {
        e.printStackTrace(); // keep full detail on the console for debugging
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static boolean isForeignKeyViolation(SQLException e) {
        return e.getErrorCode() == FOREIGN_KEY_VIOLATION;
    }

    static boolean isDuplicateEntry(SQLException e) {
        return e.getErrorCode() == DUPLICATE_ENTRY;
    }

    /** Lets the user pick a save location, then writes the table's visible rows as CSV. */
    static void exportToCsv(Component parent, JTable table, String suggestedFileName) {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "There is nothing to export.",
                    "Nothing To Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(suggestedFileName));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        TableModel model = table.getModel();
        int columnCount = model.getColumnCount();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < columnCount; col++) {
                if (col > 0) {
                    line.append(',');
                }
                line.append(escapeCsv(model.getColumnName(col)));
            }
            writer.println(line);

            for (int row = 0; row < model.getRowCount(); row++) {
                line.setLength(0);
                for (int col = 0; col < columnCount; col++) {
                    if (col > 0) {
                        line.append(',');
                    }
                    Object value = model.getValueAt(row, col);
                    line.append(escapeCsv(value == null ? "" : value.toString()));
                }
                writer.println(line);
            }

            JOptionPane.showMessageDialog(parent,
                    "Exported " + model.getRowCount() + " row(s) to " + file.getName() + ".",
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Failed to export CSV: " + e.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
