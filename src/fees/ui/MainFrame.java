package fees.ui;

import fees.db.DBConnection;
import fees.thread.DueReminderThread;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main Application Frame
 * Unit 5 Syllabus Concept: Swing BorderLayout, GridLayout forms; ActionListener
 * Hosts all 5 core modules:
 * 1. Student fee structure
 * 2. Fee payment
 * 3. Receipt generation
 * 4. Pending dues list
 * 5. Class-wise collection report
 */
public class MainFrame extends JFrame implements ActionListener, ChangeListener {

    private final JTabbedPane tabbedPane;
    private final StudentFeePanel studentPanel;
    private final FeePaymentPanel paymentPanel;
    private final ReceiptPanel receiptPanel;
    private final PendingDuesPanel pendingDuesPanel;
    private final ClassReportPanel reportPanel;

    private final DueReminderThread reminderThread;

    // Menu Items
    private JMenuItem mnuExit;
    private JMenuItem mnuRefreshAll;
    private JMenuItem mnuTriggerReminders;
    private JMenuItem mnuAbout;
    private JMenuItem mnuSyllabus;

    private JLabel lblStatus;
    private JLabel lblClock;

    public MainFrame() {
        super(" School Fee Management System ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        // Unit 3 Concept: Start background Due Reminder Thread
        reminderThread = new DueReminderThread();
        reminderThread.start();

        // Unit 5 Concept: Swing BorderLayout on main frame
        setLayout(new BorderLayout());

        // -------------------------------------------------------------
        // 1. Menu Bar
        // -------------------------------------------------------------
        setJMenuBar(createMenuBar());

        // -------------------------------------------------------------
        // 2. NORTH: Header Banner
        // -------------------------------------------------------------
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(new Color(20, 50, 95));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBlock.setOpaque(false);

        JLabel lblTitle = new JLabel("School Fee Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("A system to record student fees, generate receipts and track pending dues. | Group 15");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(210, 230, 255));

        titleBlock.add(lblTitle);
        titleBlock.add(lblSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Team info pill on Header East (Unit 5: GridLayout)
        JPanel teamPill = new JPanel(new GridLayout(2, 1, 2, 2));
        teamPill.setBackground(new Color(30, 70, 130));
        teamPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 110, 185), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        JLabel lblGroup = new JLabel(" B.Tech CSE Sem 3", SwingConstants.CENTER);
        lblGroup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGroup.setForeground(new Color(255, 215, 0));

        JLabel lblMembers = new JLabel(" Ashnoor| Sugandh | Bharat | Sahaz | ", SwingConstants.CENTER);
        lblMembers.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMembers.setForeground(Color.WHITE);

        teamPill.add(lblGroup);
        teamPill.add(lblMembers);
        headerPanel.add(teamPill, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // 3. CENTER: Tabbed Navigation with 5 Modules
        // -------------------------------------------------------------
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        studentPanel = new StudentFeePanel();
        paymentPanel = new FeePaymentPanel();
        receiptPanel = new ReceiptPanel();
        pendingDuesPanel = new PendingDuesPanel(reminderThread);
        reportPanel = new ClassReportPanel();

        tabbedPane.addTab("  1. Student Fee Structure  ", studentPanel);
        tabbedPane.addTab("  2. Fee Payment  ", paymentPanel);
        tabbedPane.addTab("  3. Receipt Generation  ", receiptPanel);
        tabbedPane.addTab("  4. Pending Dues List  ", pendingDuesPanel);
        tabbedPane.addTab("  5. Class-Wise Collection Report  ", reportPanel);

        tabbedPane.addChangeListener(this);
        add(tabbedPane, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // 4. SOUTH: Status Bar
        // -------------------------------------------------------------
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        lblStatus = new JLabel("Database Engine: " + DBConnection.getActiveMode() + " | JDBC Status: Connected (students, fees, receipts)");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(71, 85, 105));

        lblClock = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        lblClock.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblClock.setForeground(new Color(100, 116, 139));

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblClock, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();

        // File Menu
        JMenu mnuFile = new JMenu("File");
        mnuRefreshAll = new JMenuItem("Refresh All Data");
        mnuRefreshAll.addActionListener(this);
        mnuExit = new JMenuItem("Exit");
        mnuExit.addActionListener(this);
        mnuFile.add(mnuRefreshAll);
        mnuFile.addSeparator();
        mnuFile.add(mnuExit);
        mb.add(mnuFile);

        // Modules Menu
        JMenu mnuModules = new JMenu("Modules");
        for (int i = 0; i < 5; i++) {
            final int tabIndex = i;
            String name = (i + 1) + ". " + getModuleName(i);
            JMenuItem mi = new JMenuItem(name);
            mi.addActionListener(e -> tabbedPane.setSelectedIndex(tabIndex));
            mnuModules.add(mi);
        }
        mb.add(mnuModules);

        // Tools Menu
        JMenu mnuTools = new JMenu("Tools");
        mnuTriggerReminders = new JMenuItem("Trigger Reminder Scan (DueReminderThread)");
        mnuTriggerReminders.addActionListener(this);
        mnuTools.add(mnuTriggerReminders);
        mb.add(mnuTools);

        // Help Menu
        JMenu mnuHelp = new JMenu("Help");
        mnuAbout = new JMenuItem("About Group 15 Team");
        mnuAbout.addActionListener(this);
        mnuSyllabus = new JMenuItem("Syllabus Concepts Applied");
        mnuSyllabus.addActionListener(this);
        mnuHelp.add(mnuAbout);
        mnuHelp.add(mnuSyllabus);
        mb.add(mnuHelp);

        return mb;
    }

    private String getModuleName(int idx) {
        switch (idx) {
            case 0: return "Student Fee Structure";
            case 1: return "Fee Payment";
            case 2: return "Receipt Generation";
            case 3: return "Pending Dues List";
            case 4: return "Class-Wise Collection Report";
            default: return "Module";
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int idx = tabbedPane.getSelectedIndex();
        switch (idx) {
            case 0: studentPanel.refreshTable(null); break;
            case 1: paymentPanel.refreshStudentDropdown(); break;
            case 2: receiptPanel.refreshReceipts(); break;
            case 3: pendingDuesPanel.refreshPendingDues(); break;
            case 4: reportPanel.refreshReportData(); break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mnuExit) {
            if (reminderThread != null) {
                reminderThread.stopThread();
            }
            System.exit(0);
        } else if (e.getSource() == mnuRefreshAll) {
            studentPanel.refreshTable(null);
            paymentPanel.refreshStudentDropdown();
            receiptPanel.refreshReceipts();
            pendingDuesPanel.refreshPendingDues();
            reportPanel.refreshReportData();
            JOptionPane.showMessageDialog(this, "All module records refreshed successfully.", "Data Refreshed", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getSource() == mnuTriggerReminders) {
            if (reminderThread != null) {
                reminderThread.triggerManualCheck();
                JOptionPane.showMessageDialog(this, "Manual check triggered on DueReminderThread.\nCheck 'Pending Dues List' tab for live feed.", "Thread Triggered", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (e.getSource() == mnuAbout) {
            showAboutDialog();
        } else if (e.getSource() == mnuSyllabus) {
            showSyllabusDialog();
        }
    }

    private void showAboutDialog() {
        String msg = "=====================================================\n" +
                "               GROUP 15 - MINI PROJECT\n" +
                "        CSE2003 Object Oriented Programming using JAVA\n" +
                "                   B.Tech CSE Semester 3\n" +
                "=====================================================\n\n" +
                "Project Title: School Fee Management System\n" +
                "Purpose      : Record student fees, generate receipts and track pending dues.\n\n" +
                "Team Members:\n" +
                " • Leader   : Sugandh  (UID: 2510001376)\n" +
                " • Member 1 : Ashnoor  (UID: 2510001384)\n" +
                " • Member 2 : Sahaz    (UID: 2510001385)\n" +
                " • Member 3 : Ram      (UID: 2510001396)\n\n" +
                "Technology  : 100% Pure Java SE (Swing, JDBC, Multi-Threading, OOP)\n" +
                "Zero external JARs or JSON required.";

        JOptionPane.showMessageDialog(this, msg, "About Group 15", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSyllabusDialog() {
        String msg = "=====================================================\n" +
                "          SYLLABUS CONCEPTS APPLIED (UNITS 1 - 5)\n" +
                "=====================================================\n\n" +
                "• Unit 1:\n" +
                "   - Classes: Student, Fee, Receipt\n" +
                "   - Math class: Math.round, Math.min, Math.max, Math.random\n" +
                "   - StringBuffer: Official receipt formatting via StringBuffer.append()\n\n" +
                "• Unit 2:\n" +
                "   - Inheritance: Fee -> TuitionFee / TransportFee / ExamFee\n" +
                "   - Interface Payable (calculateTotalFee, processPayment, getPendingDue)\n" +
                "   - Package: fees.model\n\n" +
                "• Unit 3:\n" +
                "   - Custom Exception: InvalidAmountException (invalid/excess payment)\n" +
                "   - Multi-threading: DueReminderThread daemon for pending due reminders\n\n" +
                "• Unit 4:\n" +
                "   - JDBC PreparedStatement (CRUD, search, updates on students & fees)\n" +
                "   - JDBC CallableStatement (sp_record_fee_payment, sp_get_pending_dues, sp_class_collection_report)\n" +
                "   - Database Tables: students, fees, receipts\n\n" +
                "• Unit 5:\n" +
                "   - Swing Layouts: BorderLayout, GridLayout forms\n" +
                "   - Event Handling: ActionListener on interactive forms and buttons\n";

        JOptionPane.showMessageDialog(this, msg, "Syllabus Checklist", JOptionPane.INFORMATION_MESSAGE);
    }
}
