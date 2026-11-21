package fees.ui;

import fees.model.Receipt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;

/**
 * ReceiptDialog - Modal popup to view, print, or save generated fee receipts.
 * Unit 5 Syllabus Concept: Swing BorderLayout, GridLayout forms; ActionListener
 */
public class ReceiptDialog extends JDialog implements ActionListener {

    private final JTextArea txtReceiptArea;
    private final JButton btnSaveFile;
    private final JButton btnClose;
    private final Receipt receipt;

    public ReceiptDialog(Frame parent, Receipt receipt) {
        super(parent, "Fee Payment Receipt - " + receipt.getReceiptNo(), true);
        this.receipt = receipt;

        setSize(650, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        headerPanel.setBackground(new Color(30, 58, 138));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("Official Payment Receipt", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Receipt No: " + receipt.getReceiptNo() + " | Student: " + receipt.getStudentName(), SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(220, 235, 255));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        // Center Receipt Text Area (Monospaced for aligned StringBuffer display)
        txtReceiptArea = new JTextArea();
        txtReceiptArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtReceiptArea.setEditable(false);
        txtReceiptArea.setBackground(new Color(250, 250, 252));
        txtReceiptArea.setForeground(new Color(20, 25, 40));
        txtReceiptArea.setMargin(new Insets(15, 20, 15, 20));
        txtReceiptArea.setText(receipt.getReceiptText());

        JScrollPane scrollPane = new JScrollPane(txtReceiptArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 5, 15),
                BorderFactory.createLineBorder(new Color(200, 210, 225), 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons using GridLayout & ActionListener
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        btnSaveFile = UIUtils.createPrimaryButton("Save Receipt to File (.txt)", 13);
        btnSaveFile.addActionListener(this);

        btnClose = UIUtils.createSecondaryButton("Close", 13);
        btnClose.addActionListener(this);

        bottomPanel.add(btnSaveFile);
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSaveFile) {
            saveReceiptToFile();
        } else if (e.getSource() == btnClose) {
            dispose();
        }
    }

    private void saveReceiptToFile() {
        try {
            String fileName = "Receipt_" + receipt.getReceiptNo() + ".txt";
            File file = new File(fileName);
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(receipt.getReceiptText());
            }
            JOptionPane.showMessageDialog(this,
                    "Receipt successfully saved as:\n" + file.getAbsolutePath(),
                    "Receipt Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save receipt file: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
