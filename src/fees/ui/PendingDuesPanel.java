package fees.ui;

import fees.db.FeeDAO;
import fees.thread.DueReminderThread;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Module 4: Pending Dues List
 * Unit 4 Syllabus Concept: JDBC CallableStatement on stored procedure 'sp_get_pending_dues'
 * Unit 3 Syllabus Concept: Multi-threading background DueReminderThread
 * Unit 5 Syllabus Concept: Swing BorderLayout, GridLayout forms; ActionListener
 */
public class PendingDuesPanel extends JPanel implements ActionListener, DueReminderThread.ReminderListener {

    private final JTable duesTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> cmbClassFilter;
    private final JButton btnFilter;
    private final JButton btnTriggerReminder;
    private final JButton btnTogglePause;

    // Live Reminder Thread Dispatch Feed
    private final DefaultListModel<String> threadLogModel;
    private final JList<String> threadLogList;
    private final JLabel lblThreadStatus;

    private final DueReminderThread reminderThread;

    public PendingDuesPanel(DueReminderThread reminderThread) {
        this.reminderThread = reminderThread;
        if (this.reminderThread != null) {
            this.reminderThread.addReminderListener(this);
        }

        // Unit 5: Swing BorderLayout
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setBackground(new Color(248, 250, 252));

        // -------------------------------------------------------------
        // NORTH: Control & Filter Toolbar
        // -------------------------------------------------------------
        JPanel toolBar = new JPanel(new BorderLayout(10, 10));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilter.setOpaque(false);
        leftFilter.add(new JLabel("Filter by Class:"));
        cmbClassFilter = new JComboBox<>(new String[]{"All Classes", "Class 10A", "Class 10B", "Class 11A", "Class 11B", "Class 12A", "Class 12B"});
        cmbClassFilter.addActionListener(this);
        leftFilter.add(cmbClassFilter);

        btnFilter = UIUtils.createDarkButton("Apply Filter", 12);
        btnFilter.addActionListener(this);
        leftFilter.add(btnFilter);

        toolBar.add(leftFilter, BorderLayout.WEST);

        // Right side thread controls (Unit 5: GridLayout for buttons)
        JPanel rightControls = new JPanel(new GridLayout(1, 2, 8, 0));
        rightControls.setOpaque(false);

        btnTriggerReminder = UIUtils.createPrimaryButton("Run Scan Now", 12);
        btnTriggerReminder.addActionListener(this);

        btnTogglePause = UIUtils.createSecondaryButton("Pause Thread", 12);
        btnTogglePause.addActionListener(this);

        rightControls.add(btnTriggerReminder);
        rightControls.add(btnTogglePause);
        toolBar.add(rightControls, BorderLayout.EAST);

        add(toolBar, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // CENTER: Pending Dues Table (Executed via CallableStatement)
        // -------------------------------------------------------------
        String[] columnNames = {"Student UID", "Roll No", "Student Name", "Class", "Contact", "Total Scheduled", "Total Paid", "Outstanding Pending Due", "Last Payment Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        duesTable = new JTable(tableModel);
        duesTable.setRowHeight(25);
        duesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        duesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        duesTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scrollTable = new JScrollPane(duesTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        add(scrollTable, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // SOUTH: Live Background DueReminderThread Dispatch Feed
        // -------------------------------------------------------------
        JPanel logContainer = new JPanel(new BorderLayout(6, 6));
        logContainer.setPreferredSize(new Dimension(0, 160));
        logContainer.setBackground(Color.WHITE);
        logContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        JLabel lblLogTitle = new JLabel("Live Reminder Service Feed (Unit 3: Background DueReminderThread)");
        lblLogTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLogTitle.setForeground(new Color(30, 58, 138));

        lblThreadStatus = new JLabel("Status: Active (Scanning every 20s)");
        lblThreadStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblThreadStatus.setForeground(new Color(5, 150, 105));

        logHeader.add(lblLogTitle, BorderLayout.WEST);
        logHeader.add(lblThreadStatus, BorderLayout.EAST);
        logContainer.add(logHeader, BorderLayout.NORTH);

        threadLogModel = new DefaultListModel<>();
        threadLogModel.addElement("System initialized. Background DueReminderThread running as daemon service.");

        threadLogList = new JList<>(threadLogModel);
        threadLogList.setFont(new Font("Consolas", Font.PLAIN, 11));
        threadLogList.setBackground(new Color(250, 250, 252));

        JScrollPane scrollLog = new JScrollPane(threadLogList);
        scrollLog.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        logContainer.add(scrollLog, BorderLayout.CENTER);

        add(logContainer, BorderLayout.SOUTH);

        refreshPendingDues();
    }

    public void refreshPendingDues() {
        tableModel.setRowCount(0);
        String selectedClass = (String) cmbClassFilter.getSelectedItem();
        boolean filterAll = selectedClass == null || "All Classes".equalsIgnoreCase(selectedClass);

        try {
            // Unit 4: CallableStatement execution of 'sp_get_pending_dues'
            FeeDAO dao = FeeDAO.getInstance();
            List<Map<String, Object>> dues = dao.getPendingDuesListCallable();

            for (Map<String, Object> d : dues) {
                String grade = String.valueOf(d.get("class_grade"));
                if (filterAll || grade.equalsIgnoreCase(selectedClass)) {
                    tableModel.addRow(new Object[]{
                            d.get("student_id"),
                            d.get("roll_no"),
                            d.get("name"),
                            grade,
                            d.get("contact"),
                            String.format("Rs. %.2f", d.get("total_fee")),
                            String.format("Rs. %.2f", d.get("paid_amount")),
                            String.format("Rs. %.2f", d.get("pending_amount")),
                            d.get("last_payment_date")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load pending dues: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnFilter || e.getSource() == cmbClassFilter) {
            refreshPendingDues();
        } else if (e.getSource() == btnTriggerReminder) {
            if (reminderThread != null) {
                reminderThread.triggerManualCheck();
                threadLogModel.addElement("Manual reminder scan triggered by user.");
                threadLogList.ensureIndexIsVisible(threadLogModel.size() - 1);
            }
        } else if (e.getSource() == btnTogglePause) {
            if (reminderThread != null) {
                if (reminderThread.isPaused()) {
                    reminderThread.resumeReminders();
                    btnTogglePause.setText("Pause Thread");
                    lblThreadStatus.setText("Status: Active (Scanning every 20s)");
                    lblThreadStatus.setForeground(new Color(5, 150, 105));
                } else {
                    reminderThread.pauseReminders();
                    btnTogglePause.setText("Resume Thread");
                    lblThreadStatus.setText("Status: Paused");
                    lblThreadStatus.setForeground(new Color(220, 38, 38));
                }
            }
        }
    }

    @Override
    public void onReminderDispatched(String timestamp, String studentId, String studentName,
                                     String classGrade, double pendingAmount, String message) {
        SwingUtilities.invokeLater(() -> {
            String entry = String.format("[%s] ALERT DISPATCHED -> Student %s (%s, %s): Overdue Pending Due = Rs. %.2f",
                    timestamp, studentName, studentId, classGrade, pendingAmount);
            threadLogModel.addElement(entry);
            // Auto scroll to latest entry
            threadLogList.ensureIndexIsVisible(threadLogModel.size() - 1);
        });
    }
}
