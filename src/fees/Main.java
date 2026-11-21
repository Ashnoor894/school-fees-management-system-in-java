package fees;

import fees.db.PureJavaJDBC;
import fees.ui.MainFrame;

import javax.swing.*;

/**
 * Main Entry Point
 * Group 15 - School Fee Management System
 * CSE2003 | Object Oriented Programming using JAVA | B.Tech CSE Semester 3
 */
public class Main {

    public static void main(String[] args) {
        // Initialize in-memory database and seed Group 15 demo records
        PureJavaJDBC.initializeDefaults();

        // Apply clean platform Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Launch GUI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}
