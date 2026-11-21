package fees.ui;

import fees.db.FeeDAO;
import fees.model.Receipt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * Module 3: Receipt Generation & History
 * Unit 1 Syllabus Concept: Class Receipt; Math class; StringBuffer for receipts
 * Unit 4 Syllabus Concept: JDBC PreparedStatement on 'receipts' table
 * Unit 5 Syllabus Concept: Swing BorderLayout, GridLayout forms; ActionListener
 */
public class ReceiptPanel extends JPanel implements ActionListener {

    private final JTable receiptTable;
    private final DefaultTableModel tableModel;
    private final JTextArea txtReceiptView;
    private final JButton btnViewPopup;
    private final JButton btnExportTxt;
    private final JButton btnRefresh;
    private List<Receipt> currentReceipts;

    public ReceiptPanel() {
        // Unit 5: Swing BorderLayout
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setBackground(new Color(248, 250, 252));

        // -----------------------------------------------------------------
        // NORTH: Header bar
        // -----------------------------------------------------------------
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblTitle = new JLabel("Audit Trail of Issued Fee Receipts (StringBuffer Formatted)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(30, 58, 138));

        btnRefresh = UIUtils.createSecondaryButton("Refresh Receipts", 12);
        btnRefresh.addActionListener(this);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // -----------------------------------------------------------------
        // CENTER: Split View (Table on Left, Formatted Preview on Right)
        // -----------------------------------------------------------------
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);

        // Right Panel: Live StringBuffer Receipt Display
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblPreviewTitle = new JLabel("Receipt Document Preview (java.lang.StringBuffer)");
        lblPreviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPreviewTitle.setForeground(new Color(71, 85, 105));
        rightPanel.add(lblPreviewTitle, BorderLayout.NORTH);

        txtReceiptView = new JTextArea("Select a receipt row to view the full formatted document.");
        txtReceiptView.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtReceiptView.setEditable(false);
        txtReceiptView.setBackground(new Color(250, 250, 252));
        txtReceiptView.setMargin(new Insets(10, 12, 10, 12));

        JScrollPane scrollText = new JScrollPane(txtReceiptView);
        scrollText.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        rightPanel.add(scrollText, BorderLayout.CENTER);

        // Action Buttons at Bottom of Right Panel (Unit 5: GridLayout)
        JPanel btnBar = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBar.setOpaque(false);
        btnBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        btnViewPopup = UIUtils.createPrimaryButton("Open in Dialog", 12);
        btnViewPopup.addActionListener(this);

        btnExportTxt = UIUtils.createSecondaryButton("Export to .txt File", 12);
        btnExportTxt.addActionListener(this);

        btnBar.add(btnViewPopup);
        btnBar.add(btnExportTxt);
        rightPanel.add(btnBar, BorderLayout.SOUTH);

        // Left Panel: Receipts JTable
        String[] columnNames = {"Receipt No", "Student ID", "Student Name", "Amount Paid", "Payment Mode", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        receiptTable = new JTable(tableModel);
        receiptTable.setRowHeight(25);
        receiptTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        receiptTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        receiptTable.getTableHeader().setBackground(new Color(241, 245, 249));
        receiptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        receiptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && receiptTable.getSelectedRow() != -1) {
                int row = receiptTable.getSelectedRow();
                if (currentReceipts != null && row < currentReceipts.size()) {
                    Receipt r = currentReceipts.get(row);
                    txtReceiptView.setText(r.getReceiptText());
                    txtReceiptView.setCaretPosition(0);
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(receiptTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        splitPane.setLeftComponent(scrollTable);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        refreshReceipts();
    }

    public void refreshReceipts() {
        tableModel.setRowCount(0);
        try {
            FeeDAO dao = FeeDAO.getInstance();
            currentReceipts = dao.getAllReceipts();

            for (Receipt r : currentReceipts) {
                tableModel.addRow(new Object[]{
                        r.getReceiptNo(),
                        r.getStudentId(),
                        r.getStudentName(),
                        String.format("Rs. %.2f", r.getAmountPaid()),
                        r.getPaymentMode(),
                        r.getPaymentDate()
                });
            }

            if (!currentReceipts.isEmpty()) {
                receiptTable.setRowSelectionInterval(0, 0);
            } else {
                txtReceiptView.setText("No receipts generated yet.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load receipts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            refreshReceipts();
        } else if (e.getSource() == btnViewPopup) {
            int row = receiptTable.getSelectedRow();
            if (row != -1 && currentReceipts != null && row < currentReceipts.size()) {
                Receipt r = currentReceipts.get(row);
                Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                ReceiptDialog dialog = new ReceiptDialog(topFrame, r);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a receipt first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (e.getSource() == btnExportTxt) {
            handleExportTxt();
        }
    }

    private void handleExportTxt() {
        int row = receiptTable.getSelectedRow();
        if (row != -1 && currentReceipts != null && row < currentReceipts.size()) {
            Receipt r = currentReceipts.get(row);
            try {
                String fileName = "Receipt_" + r.getReceiptNo() + ".txt";
                File file = new File(fileName);
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(r.getReceiptText());
                }
                JOptionPane.showMessageDialog(this,
                        "Receipt exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Confirmed", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a receipt to export.", "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }
}
