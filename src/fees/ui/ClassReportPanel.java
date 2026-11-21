package fees.ui;

import fees.db.FeeDAO;
import fees.model.Fee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Module 5: Class-Wise Collection Report
 * Unit 4 Syllabus Concept: JDBC CallableStatement on stored procedure 'sp_class_collection_report'
 * Unit 5 Syllabus Concept: Swing BorderLayout, GridLayout forms; ActionListener
 */
public class ClassReportPanel extends JPanel implements ActionListener {

    // Metric stat card labels
    private final JLabel lblTotalStudents;
    private final JLabel lblTotalBudget;
    private final JLabel lblTotalCollected;
    private final JLabel lblTotalPending;
    private final JLabel lblEfficiencyRate;

    // Report table
    private final JTable reportTable;
    private final DefaultTableModel tableModel;

    // Buttons
    private final JButton btnRefreshReport;
    private final JButton btnExportReport;

    private List<Map<String, Object>> currentReportData;

    public ClassReportPanel() {
        // Unit 5: Swing BorderLayout
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setBackground(new Color(248, 250, 252));

        // -----------------------------------------------------------------
        // NORTH: Top Stat Cards (Unit 5: GridLayout form for cards)
        // -----------------------------------------------------------------
        JPanel cardsGrid = new JPanel(new GridLayout(1, 5, 12, 12));
        cardsGrid.setOpaque(false);

        lblTotalStudents = new JLabel("0", SwingConstants.CENTER);
        lblTotalBudget = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        lblTotalCollected = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        lblTotalPending = new JLabel("Rs. 0.00", SwingConstants.CENTER);
        lblEfficiencyRate = new JLabel("0.0%", SwingConstants.CENTER);

        cardsGrid.add(createStatCard("Enrolled Students", lblTotalStudents, new Color(30, 58, 138), Color.WHITE));
        cardsGrid.add(createStatCard("Total Budgeted Fees", lblTotalBudget, new Color(15, 23, 42), Color.WHITE));
        cardsGrid.add(createStatCard("Total Collections", lblTotalCollected, new Color(5, 150, 105), Color.WHITE));
        cardsGrid.add(createStatCard("Total Overdue Dues", lblTotalPending, new Color(220, 38, 38), Color.WHITE));
        cardsGrid.add(createStatCard("Collection Rate", lblEfficiencyRate, new Color(124, 58, 237), Color.WHITE));

        add(cardsGrid, BorderLayout.NORTH);

        // -----------------------------------------------------------------
        // CENTER: Class Breakdown JTable
        // -----------------------------------------------------------------
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblTableTitle = new JLabel("Class-by-Class Fee Collection Analytics (sp_class_collection_report)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(new Color(30, 58, 138));
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] columnNames = {"Class / Grade", "Student Count", "Total Scheduled Fee", "Total Collected", "Total Pending Dues", "Collection Efficiency %"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(26);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scrollTable = new JScrollPane(reportTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        tableContainer.add(scrollTable, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        // -----------------------------------------------------------------
        // SOUTH: Action Buttons (Unit 5: GridLayout for buttons)
        // -----------------------------------------------------------------
        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomButtons.setOpaque(false);

        btnRefreshReport = UIUtils.createPrimaryButton("Refresh Analytics (Execute CallableStatement)", 12);
        btnRefreshReport.addActionListener(this);

        btnExportReport = UIUtils.createSecondaryButton("Export Report Summary (.txt)", 12);
        btnExportReport.addActionListener(this);

        bottomButtons.add(btnRefreshReport);
        bottomButtons.add(btnExportReport);
        add(bottomButtons, BorderLayout.SOUTH);

        refreshReportData();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color valueColor, Color bgColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 4, 4));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)
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

    public void refreshReportData() {
        tableModel.setRowCount(0);
        int totalStudents = 0;
        double totalBudget = 0.0;
        double totalCollected = 0.0;
        double totalPending = 0.0;

        try {
            // Unit 4: CallableStatement execution of 'sp_class_collection_report'
            FeeDAO dao = FeeDAO.getInstance();
            currentReportData = dao.getClassCollectionReportCallable();

            for (Map<String, Object> row : currentReportData) {
                int count = ((Number) row.get("student_count")).intValue();
                double tFee = ((Number) row.get("total_fee")).doubleValue();
                double cFee = ((Number) row.get("total_collected")).doubleValue();
                double pFee = ((Number) row.get("total_pending")).doubleValue();
                double pct = ((Number) row.get("collection_percentage")).doubleValue();

                totalStudents += count;
                totalBudget += tFee;
                totalCollected += cFee;
                totalPending += pFee;

                tableModel.addRow(new Object[]{
                        row.get("class_grade"),
                        count,
                        String.format("Rs. %.2f", tFee),
                        String.format("Rs. %.2f", cFee),
                        String.format("Rs. %.2f", pFee),
                        String.format("%.2f %%", pct)
                });
            }

            // Update top stat cards
            lblTotalStudents.setText(String.valueOf(totalStudents));
            lblTotalBudget.setText(String.format("Rs. %.2f", totalBudget));
            lblTotalCollected.setText(String.format("Rs. %.2f", totalCollected));
            lblTotalPending.setText(String.format("Rs. %.2f", totalPending));

            double overallPct = totalBudget > 0.0 ? Math.round((totalCollected / totalBudget) * 10000.0) / 100.0 : 0.0;
            lblEfficiencyRate.setText(String.format("%.2f %%", overallPct));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load class report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefreshReport) {
            refreshReportData();
        } else if (e.getSource() == btnExportReport) {
            handleExportReport();
        }
    }

    private void handleExportReport() {
        if (currentReportData == null || currentReportData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No report data available to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String fileName = "Class_Fee_Collection_Report.txt";
            File file = new File(fileName);

            // Unit 1 Concept: StringBuffer for report formatting
            StringBuffer sb = new StringBuffer(1024);
            sb.append("======================================================================\n");
            sb.append("                 SCHOOL FEE MANAGEMENT SYSTEM - GROUP 15              \n");
            sb.append("                     CLASS-WISE FEE COLLECTION REPORT                 \n");
            sb.append("                     Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
            sb.append("======================================================================\n\n");
            sb.append(String.format("%-15s | %10s | %18s | %18s | %18s | %10s\n",
                    "CLASS/GRADE", "STUDENTS", "SCHEDULED (RS)", "COLLECTED (RS)", "PENDING (RS)", "RATE (%)"));
            sb.append("----------------------------------------------------------------------------------------------------\n");

            for (Map<String, Object> r : currentReportData) {
                sb.append(String.format("%-15s | %10d | %18.2f | %18.2f | %18.2f | %9.2f%%\n",
                        r.get("class_grade"),
                        ((Number) r.get("student_count")).intValue(),
                        ((Number) r.get("total_fee")).doubleValue(),
                        ((Number) r.get("total_collected")).doubleValue(),
                        ((Number) r.get("total_pending")).doubleValue(),
                        ((Number) r.get("collection_percentage")).doubleValue()
                ));
            }
            sb.append("====================================================================================================\n");

            try (FileWriter fw = new FileWriter(file)) {
                fw.write(sb.toString());
            }

            JOptionPane.showMessageDialog(this,
                    "Report exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Confirmed", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
