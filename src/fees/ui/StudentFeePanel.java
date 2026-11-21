package fees.ui;

import fees.db.FeeDAO;
import fees.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Module 1: Student Fee Structure
 * Unit 5 Syllabus Concept: Swing BorderLayout, GridLayout forms; ActionListener
 * Unit 1 & Unit 2 Concepts: Classes Student, Fee, TuitionFee, TransportFee, ExamFee
 * Unit 4 Concept: JDBC PreparedStatement on tables 'students' and 'fees'
 */
public class StudentFeePanel extends JPanel implements ActionListener {

    // Form input fields
    private final JTextField txtStudentId;
    private final JTextField txtRollNo;
    private final JTextField txtName;
    private final JComboBox<String> cmbClass;
    private final JTextField txtContact;
    private final JTextField txtEmail;

    // Fee structure components
    private final JTextField txtBaseTuition;
    private final JTextField txtLabFee;
    private final JTextField txtLibraryFee;
    private final JTextField txtScholarship;

    private final JTextField txtDistanceKm;
    private final JTextField txtRatePerKm;
    private final JTextField txtRouteName;

    private final JTextField txtSubjects;
    private final JTextField txtRatePerSubject;
    private final JTextField txtPracticalFee;

    // Action buttons
    private final JButton btnCalculateAndSave;
    private final JButton btnClearForm;
    private final JButton btnDeleteStudent;
    private final JButton btnSearch;
    private final JTextField txtSearch;

    // Table
    private final JTable studentTable;
    private final DefaultTableModel tableModel;

    public StudentFeePanel() {
        // Unit 5: Swing BorderLayout
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setBackground(new Color(248, 250, 252));

        // -----------------------------------------------------------------
        // 1. WEST: Structured Form Panel (using GridLayout forms)
        // -----------------------------------------------------------------
        JPanel formContainer = new JPanel(new BorderLayout(8, 8));
        formContainer.setPreferredSize(new Dimension(420, 0));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblFormTitle = new JLabel("Register Student & Fee Structure");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(new Color(30, 58, 138));
        formContainer.add(lblFormTitle, BorderLayout.NORTH);

        // Nested sub-panels for forms
        JPanel formsHolder = new JPanel();
        formsHolder.setLayout(new BoxLayout(formsHolder, BoxLayout.Y_AXIS));
        formsHolder.setOpaque(false);

        // Section A: Student Information Form (Unit 5: GridLayout form)
        JPanel pnlStudentInfo = new JPanel(new GridLayout(6, 2, 6, 6));
        pnlStudentInfo.setOpaque(false);
        pnlStudentInfo.setBorder(BorderFactory.createTitledBorder("Student Personal Details"));

        txtStudentId = new JTextField();
        txtRollNo = new JTextField();
        txtName = new JTextField();
        cmbClass = new JComboBox<>(new String[]{"Class 10A", "Class 10B", "Class 11A", "Class 11B", "Class 12A", "Class 12B"});
        txtContact = new JTextField();
        txtEmail = new JTextField();

        pnlStudentInfo.add(new JLabel("Student ID / UID:"));
        pnlStudentInfo.add(txtStudentId);
        pnlStudentInfo.add(new JLabel("Roll Number:"));
        pnlStudentInfo.add(txtRollNo);
        pnlStudentInfo.add(new JLabel("Full Name:"));
        pnlStudentInfo.add(txtName);
        pnlStudentInfo.add(new JLabel("Class / Grade:"));
        pnlStudentInfo.add(cmbClass);
        pnlStudentInfo.add(new JLabel("Contact Number:"));
        pnlStudentInfo.add(txtContact);
        pnlStudentInfo.add(new JLabel("Email Address:"));
        pnlStudentInfo.add(txtEmail);
        formsHolder.add(pnlStudentInfo);
        formsHolder.add(Box.createVerticalStrut(8));

        // Section B: Tuition Fee Form (Unit 2: Inheritance Fee -> TuitionFee)
        JPanel pnlTuition = new JPanel(new GridLayout(4, 2, 6, 6));
        pnlTuition.setOpaque(false);
        pnlTuition.setBorder(BorderFactory.createTitledBorder("Tuition Fee Details"));

        txtBaseTuition = new JTextField("40000");
        txtLabFee = new JTextField("3000");
        txtLibraryFee = new JTextField("2000");
        txtScholarship = new JTextField("0");

        pnlTuition.add(new JLabel("Base Tuition (Rs):"));
        pnlTuition.add(txtBaseTuition);
        pnlTuition.add(new JLabel("Lab Charges (Rs):"));
        pnlTuition.add(txtLabFee);
        pnlTuition.add(new JLabel("Library Charges (Rs):"));
        pnlTuition.add(txtLibraryFee);
        pnlTuition.add(new JLabel("Scholarship Concession (Rs):"));
        pnlTuition.add(txtScholarship);
        formsHolder.add(pnlTuition);
        formsHolder.add(Box.createVerticalStrut(8));

        // Section C: Transport Fee Form (Unit 2: Inheritance Fee -> TransportFee)
        JPanel pnlTransport = new JPanel(new GridLayout(3, 2, 6, 6));
        pnlTransport.setOpaque(false);
        pnlTransport.setBorder(BorderFactory.createTitledBorder("Transport Fee Details"));

        txtDistanceKm = new JTextField("10");
        txtRatePerKm = new JTextField("800");
        txtRouteName = new JTextField("Route-North-4");

        pnlTransport.add(new JLabel("Route Distance (Km):"));
        pnlTransport.add(txtDistanceKm);
        pnlTransport.add(new JLabel("Rate / Km (Rs):"));
        pnlTransport.add(txtRatePerKm);
        pnlTransport.add(new JLabel("Route Code / Name:"));
        pnlTransport.add(txtRouteName);
        formsHolder.add(pnlTransport);
        formsHolder.add(Box.createVerticalStrut(8));

        // Section D: Exam Fee Form (Unit 2: Inheritance Fee -> ExamFee)
        JPanel pnlExam = new JPanel(new GridLayout(3, 2, 6, 6));
        pnlExam.setOpaque(false);
        pnlExam.setBorder(BorderFactory.createTitledBorder("Exam Fee Details"));

        txtSubjects = new JTextField("5");
        txtRatePerSubject = new JTextField("500");
        txtPracticalFee = new JTextField("1000");

        pnlExam.add(new JLabel("Subjects Count:"));
        pnlExam.add(txtSubjects);
        pnlExam.add(new JLabel("Rate / Subject (Rs):"));
        pnlExam.add(txtRatePerSubject);
        pnlExam.add(new JLabel("Practical Assessment (Rs):"));
        pnlExam.add(txtPracticalFee);
        formsHolder.add(pnlExam);

        JScrollPane formScroll = new JScrollPane(formsHolder);
        formScroll.setBorder(null);
        formContainer.add(formScroll, BorderLayout.CENTER);

        // Buttons at Bottom of Form (Unit 5: GridLayout form for buttons)
        JPanel pnlFormButtons = new JPanel(new GridLayout(1, 3, 6, 6));
        pnlFormButtons.setOpaque(false);
        pnlFormButtons.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        btnCalculateAndSave = UIUtils.createPrimaryButton("Save Structure", 12);
        btnCalculateAndSave.addActionListener(this);

        btnClearForm = UIUtils.createSecondaryButton("Clear", 12);
        btnClearForm.addActionListener(this);

        btnDeleteStudent = UIUtils.createDangerButton("Delete", 12);
        btnDeleteStudent.addActionListener(this);

        pnlFormButtons.add(btnCalculateAndSave);
        pnlFormButtons.add(btnClearForm);
        pnlFormButtons.add(btnDeleteStudent);
        formContainer.add(pnlFormButtons, BorderLayout.SOUTH);

        add(formContainer, BorderLayout.WEST);

        // -----------------------------------------------------------------
        // 2. CENTER: Student & Fee Records Table View
        // -----------------------------------------------------------------
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setOpaque(false);

        // Search bar at Top of Table
        JPanel searchBar = new JPanel(new BorderLayout(8, 8));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblSearch = new JLabel("Search Student:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearch = new JTextField();
        txtSearch.addActionListener(this);

        btnSearch = UIUtils.createDarkButton("Search", 12);
        btnSearch.addActionListener(this);

        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        tableContainer.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Student UID", "Roll No", "Full Name", "Class", "Tuition Fee", "Transport", "Exam Fee", "Total Fee", "Paid", "Pending Due"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(26);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        studentTable.getTableHeader().setBackground(new Color(241, 245, 249));
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // When a row is selected in table, auto-fill Student ID
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                int row = studentTable.getSelectedRow();
                txtStudentId.setText(String.valueOf(tableModel.getValueAt(row, 0)));
                txtRollNo.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                txtName.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                cmbClass.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
            }
        });

        JScrollPane scrollTable = new JScrollPane(studentTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        tableContainer.add(scrollTable, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        // Load initial records
        refreshTable(null);
    }

    public void refreshTable(String keyword) {
        tableModel.setRowCount(0);
        try {
            FeeDAO dao = FeeDAO.getInstance();
            List<Map<String, Object>> students = (keyword == null || keyword.trim().isEmpty()) ?
                    dao.getAllStudentsWithFees() : dao.searchStudents(keyword.trim());

            for (Map<String, Object> s : students) {
                tableModel.addRow(new Object[]{
                        s.get("student_id"),
                        s.get("roll_no"),
                        s.get("name"),
                        s.get("class_grade"),
                        String.format("Rs. %.2f", s.get("tuition_fee")),
                        String.format("Rs. %.2f", s.get("transport_fee")),
                        String.format("Rs. %.2f", s.get("exam_fee")),
                        String.format("Rs. %.2f", s.get("total_fee")),
                        String.format("Rs. %.2f", s.get("paid_amount")),
                        String.format("Rs. %.2f", s.get("pending_amount"))
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCalculateAndSave) {
            handleSaveStudent();
        } else if (e.getSource() == btnClearForm) {
            clearForm();
        } else if (e.getSource() == btnDeleteStudent) {
            handleDeleteStudent();
        } else if (e.getSource() == btnSearch || e.getSource() == txtSearch) {
            refreshTable(txtSearch.getText());
        }
    }

    private void handleSaveStudent() {
        String id = txtStudentId.getText().trim();
        String roll = txtRollNo.getText().trim();
        String name = txtName.getText().trim();
        String grade = (String) cmbClass.getSelectedItem();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();

        if (id.isEmpty() || name.isEmpty() || roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Student ID, Roll No, and Name.", "Validation Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Unit 1: Class Student
            String admissionDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Student student = new Student(id, roll, name, grade, contact, email, admissionDate);

            // Unit 2: Inheritance Fee -> TuitionFee, TransportFee, ExamFee
            double baseTuition = parseDoubleSafe(txtBaseTuition.getText(), 40000.0);
            double labFee = parseDoubleSafe(txtLabFee.getText(), 0.0);
            double libFee = parseDoubleSafe(txtLibraryFee.getText(), 0.0);
            double discount = parseDoubleSafe(txtScholarship.getText(), 0.0);
            TuitionFee tuition = new TuitionFee(0, id, baseTuition, labFee, libFee, discount, 0.0);

            double dist = parseDoubleSafe(txtDistanceKm.getText(), 0.0);
            double rateDist = parseDoubleSafe(txtRatePerKm.getText(), 0.0);
            String route = txtRouteName.getText().trim();
            TransportFee transport = new TransportFee(0, id, 0.0, dist, rateDist, route, 0.0);

            int subjects = parseIntSafe(txtSubjects.getText(), 5);
            double rateSub = parseDoubleSafe(txtRatePerSubject.getText(), 500.0);
            double practical = parseDoubleSafe(txtPracticalFee.getText(), 0.0);
            ExamFee exam = new ExamFee(0, id, 0.0, subjects, rateSub, practical, 0.0);

            // Unit 4: JDBC PreparedStatement executed via FeeDAO
            FeeDAO dao = FeeDAO.getInstance();
            boolean success = dao.addStudentWithFeeStructure(student, tuition, transport, exam);

            if (success) {
                double total = tuition.calculateTotalFee() + transport.calculateTotalFee() + exam.calculateTotalFee();
                JOptionPane.showMessageDialog(this,
                        String.format("Student %s successfully registered!\nComputed Total Scheduled Fees: Rs. %.2f", name, total),
                        "Student Registered", JOptionPane.INFORMATION_MESSAGE);
                refreshTable(null);
                clearForm();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        String studentId = (selectedRow != -1) ? String.valueOf(tableModel.getValueAt(selectedRow, 0)) : txtStudentId.getText().trim();

        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a student row or enter a Student ID to delete.", "Delete Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student ID: " + studentId + " and all fee records?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Unit 4: PreparedStatement deletion
                FeeDAO.getInstance().deleteStudent(studentId);
                JOptionPane.showMessageDialog(this, "Student deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable(null);
                clearForm();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtStudentId.setText("");
        txtRollNo.setText("");
        txtName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtBaseTuition.setText("40000");
        txtLabFee.setText("3000");
        txtLibraryFee.setText("2000");
        txtScholarship.setText("0");
        txtDistanceKm.setText("10");
        txtRatePerKm.setText("800");
        txtRouteName.setText("Route-North-4");
        txtSubjects.setText("5");
        txtRatePerSubject.setText("500");
        txtPracticalFee.setText("1000");
        studentTable.clearSelection();
    }

    private double parseDoubleSafe(String val, double def) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private int parseIntSafe(String val, int def) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
