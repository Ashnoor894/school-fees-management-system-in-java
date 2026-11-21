package fees.ui;

import fees.db.FeeDAO;
import fees.exception.InvalidAmountException;
import fees.model.Receipt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Module 2: Fee Payment
 * Unit 5 Syllabus Concept: Swing BorderLayout, GridLayout forms; ActionListener
 * Unit 3 Syllabus Concept: InvalidAmountException validation
 * Unit 4 Syllabus Concept: JDBC CallableStatement execution for fee processing
 */
public class FeePaymentPanel extends JPanel implements ActionListener {

    // Student selector & balance lookup
    private final JComboBox<String> cmbStudents;
    private final JLabel lblStudentName;
    private final JLabel lblTotalFee;
    private final JLabel lblPaidFee;
    private final JLabel lblPendingDue;
    private final JLabel lblLastDate;

    // Payment form fields (Unit 5: GridLayout form)
    private final JComboBox<String> cmbPaymentType;
    private final JComboBox<String> cmbPaymentMode;
    private final JTextField txtAmount;
    private final JTextField txtRemarks;

    // Buttons
    private final JButton btnProcessPayment;
    private final JButton btnClear;
    private final JButton btnRefreshStudents;

    public FeePaymentPanel() {
        // Unit 5: Swing BorderLayout
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setBackground(new Color(248, 250, 252));

        // -------------------------------------------------------------
        // NORTH: Student Selection Bar
        // -------------------------------------------------------------
        JPanel selectBar = new JPanel(new BorderLayout(10, 10));
        selectBar.setBackground(Color.WHITE);
        selectBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblSelect = new JLabel("Select Student Account:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelect.setForeground(new Color(30, 58, 138));

        cmbStudents = new JComboBox<>();
        cmbStudents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStudents.addActionListener(this);

        btnRefreshStudents = UIUtils.createSecondaryButton("Refresh Student List", 12);
        btnRefreshStudents.addActionListener(this);

        selectBar.add(lblSelect, BorderLayout.WEST);
        selectBar.add(cmbStudents, BorderLayout.CENTER);
        selectBar.add(btnRefreshStudents, BorderLayout.EAST);
        add(selectBar, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // CENTER: Account Summary & Payment Input Form (using GridLayout)
        // -------------------------------------------------------------
        JPanel centerContainer = new JPanel(new GridLayout(2, 1, 14, 14));
        centerContainer.setOpaque(false);

        // Sub-panel 1: Financial Ledger Stat Cards (Unit 5: GridLayout)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 12));
        statsPanel.setOpaque(false);

        lblStudentName = new JLabel("-", SwingConstants.CENTER);
        lblTotalFee = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        lblPaidFee = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        lblPendingDue = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        lblLastDate = new JLabel("None", SwingConstants.CENTER);

        statsPanel.add(createCard("Selected Student", lblStudentName, new Color(30, 58, 138), new Color(239, 246, 255)));
        statsPanel.add(createCard("Total Scheduled Fee", lblTotalFee, new Color(15, 23, 42), Color.WHITE));
        statsPanel.add(createCard("Total Paid to Date", lblPaidFee, new Color(5, 150, 105), Color.WHITE));
        statsPanel.add(createCard("Outstanding Pending Due", lblPendingDue, new Color(220, 38, 38), new Color(254, 242, 242)));
        centerContainer.add(statsPanel);

        // Sub-panel 2: Payment Entry Form (Unit 5: GridLayout form)
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblFormHeading = new JLabel("Enter Payment Installment Details");
        lblFormHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFormHeading.setForeground(new Color(30, 58, 138));
        formCard.add(lblFormHeading, BorderLayout.NORTH);

        JPanel paymentFields = new JPanel(new GridLayout(4, 2, 10, 10));
        paymentFields.setOpaque(false);

        cmbPaymentType = new JComboBox<>(new String[]{"Tuition Fee", "Transport Fee", "Exam Fee", "Lump-Sum Balance"});
        cmbPaymentMode = new JComboBox<>(new String[]{"Cash", "UPI", "Credit/Debit Card", "NetBanking", "Cheque"});
        txtAmount = new JTextField();
        txtAmount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtRemarks = new JTextField("Term fee installment deposited.");

        paymentFields.add(new JLabel("Payment Category:"));
        paymentFields.add(cmbPaymentType);
        paymentFields.add(new JLabel("Payment Mode:"));
        paymentFields.add(cmbPaymentMode);
        paymentFields.add(new JLabel("Payment Amount (Rs):"));
        paymentFields.add(txtAmount);
        paymentFields.add(new JLabel("Transaction Remarks:"));
        paymentFields.add(txtRemarks);

        formCard.add(paymentFields, BorderLayout.CENTER);

        // Action Buttons at Bottom of Form (Unit 5: GridLayout)
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        btnProcessPayment = UIUtils.createSuccessButton("Process Payment (Execute CallableStatement)", 13);
        btnProcessPayment.addActionListener(this);

        btnClear = UIUtils.createSecondaryButton("Clear Payment Form", 13);
        btnClear.addActionListener(this);

        btnPanel.add(btnProcessPayment);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        centerContainer.add(formCard);
        add(centerContainer, BorderLayout.CENTER);

        // Load initial student list
        refreshStudentDropdown();
    }

    private JPanel createCard(String title, JLabel valueLabel, Color valueColor, Color bgColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 4, 4));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblT = new JLabel(title, SwingConstants.CENTER);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblT.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(valueColor);

        card.add(lblT);
        card.add(valueLabel);
        return card;
    }

    public void refreshStudentDropdown() {
        cmbStudents.removeActionListener(this);
        cmbStudents.removeAllItems();
        try {
            FeeDAO dao = FeeDAO.getInstance();
            List<Map<String, Object>> students = dao.getAllStudentsWithFees();
            for (Map<String, Object> s : students) {
                String item = s.get("student_id") + " - " + s.get("name") + " (" + s.get("class_grade") + ")";
                cmbStudents.addItem(item);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        cmbStudents.addActionListener(this);
        updateSelectedStudentLedger();
    }

    private String getSelectedStudentId() {
        String item = (String) cmbStudents.getSelectedItem();
        if (item != null && item.contains(" - ")) {
            return item.split(" - ")[0].trim();
        }
        return null;
    }

    private void updateSelectedStudentLedger() {
        String studentId = getSelectedStudentId();
        if (studentId == null) {
            lblStudentName.setText("-");
            lblTotalFee.setText("Rs. 0.00");
            lblPaidFee.setText("Rs. 0.00");
            lblPendingDue.setText("Rs. 0.00");
            return;
        }

        try {
            FeeDAO dao = FeeDAO.getInstance();
            Map<String, Object> details = dao.getStudentFeeDetails(studentId);
            if (details != null) {
                lblStudentName.setText(String.valueOf(details.get("name")));
                lblTotalFee.setText(String.format("Rs. %.2f", details.get("total_fee")));
                lblPaidFee.setText(String.format("Rs. %.2f", details.get("paid_amount")));
                lblPendingDue.setText(String.format("Rs. %.2f", details.get("pending_amount")));
            }
        } catch (SQLException ex) {
            lblStudentName.setText("Error loading details");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cmbStudents) {
            updateSelectedStudentLedger();
        } else if (e.getSource() == btnRefreshStudents) {
            refreshStudentDropdown();
        } else if (e.getSource() == btnProcessPayment) {
            handleProcessPayment();
        } else if (e.getSource() == btnClear) {
            txtAmount.setText("");
            txtRemarks.setText("Term fee installment deposited.");
        }
    }

    private void handleProcessPayment() {
        String studentId = getSelectedStudentId();
        if (studentId == null) {
            JOptionPane.showMessageDialog(this, "Please select a student first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String amountStr = txtAmount.getText().trim();
        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter payment amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for amount: " + amountStr, "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String paymentType = (String) cmbPaymentType.getSelectedItem();
        String paymentMode = (String) cmbPaymentMode.getSelectedItem();
        String remarks = txtRemarks.getText().trim();

        try {
            // Unit 4: Execute stored procedure using JDBC CallableStatement
            // Unit 3: InvalidAmountException thrown and caught
            FeeDAO dao = FeeDAO.getInstance();

            // First check pending balance for explicit validation
            Map<String, Object> feeInfo = dao.getStudentFeeDetails(studentId);
            double pending = feeInfo != null ? ((Number) feeInfo.get("pending_amount")).doubleValue() : 0.0;

            if (amount <= 0) {
                throw new InvalidAmountException("Payment amount must be greater than zero.", amount);
            }
            if (amount > pending + 0.01) {
                throw new InvalidAmountException("Payment amount exceeds current outstanding pending dues of Rs. " + pending, amount);
            }

            // Execute CallableStatement
            String receiptNo = dao.recordFeePaymentCallable(studentId, amount, paymentType, paymentMode, remarks);

            if (receiptNo != null) {
                Receipt receipt = dao.getReceiptByNo(receiptNo);
                JOptionPane.showMessageDialog(this,
                        "Payment processed successfully via CallableStatement!\nGenerated Receipt No: " + receiptNo,
                        "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);

                updateSelectedStudentLedger();
                txtAmount.setText("");

                // Immediately open Receipt Dialog
                if (receipt != null) {
                    Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                    ReceiptDialog dialog = new ReceiptDialog(topFrame, receipt);
                    dialog.setVisible(true);
                }
            }
        } catch (InvalidAmountException ex) {
            // Unit 3 Syllabus Concept: Catching custom InvalidAmountException
            JOptionPane.showMessageDialog(this,
                    "Invalid Amount Exception:\n" + ex.getMessage(),
                    "Invalid Amount Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database Procedure Error: " + ex.getMessage(),
                    "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
