package fees.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UIUtils - UI styling helpers for High-Contrast, Clearly Visible Buttons and Controls.
 * Fixes Windows LookAndFeel color-stripping issue by enforcing BasicButtonUI and custom borders.
 */
public class UIUtils {

    // Palette Colors
    public static final Color COLOR_PRIMARY = new Color(29, 78, 216);    // Deep Blue
    public static final Color COLOR_SUCCESS = new Color(4, 120, 87);     // Emerald Green
    public static final Color COLOR_DANGER = new Color(220, 38, 38);     // Crimson Red
    public static final Color COLOR_DARK = new Color(30, 41, 59);        // Dark Slate Navy
    public static final Color COLOR_SECONDARY_BG = new Color(241, 245, 249); // Clean Light Slate
    public static final Color COLOR_SECONDARY_FG = new Color(15, 23, 42);   // High-contrast Dark Charcoal
    public static final Color COLOR_SECONDARY_BORDER = new Color(148, 163, 184); // Visible Border

    /**
     * Styles any JButton to ensure its background and text colors are 100% visible
     * across all Windows versions and LookAndFeels.
     */
    public static void styleButton(JButton btn, Color bgColor, Color fgColor, int fontSize) {
        btn.setUI(new BasicButtonUI()); // Overrides Windows L&F native styling that ignores setBackground
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color borderColor = (bgColor.equals(COLOR_SECONDARY_BG)) ? COLOR_SECONDARY_BORDER : bgColor.darker();
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Hover Effect for interactive feel
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgColor.equals(COLOR_SECONDARY_BG) ? new Color(226, 232, 240) : bgColor.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgColor);
                }
            }
        });
    }

    public static JButton createPrimaryButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        styleButton(btn, COLOR_PRIMARY, Color.WHITE, fontSize);
        return btn;
    }

    public static JButton createSuccessButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        styleButton(btn, COLOR_SUCCESS, Color.WHITE, fontSize);
        return btn;
    }

    public static JButton createDangerButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        styleButton(btn, COLOR_DANGER, Color.WHITE, fontSize);
        return btn;
    }

    public static JButton createSecondaryButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        styleButton(btn, COLOR_SECONDARY_BG, COLOR_SECONDARY_FG, fontSize);
        return btn;
    }

    public static JButton createDarkButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        styleButton(btn, COLOR_DARK, Color.WHITE, fontSize);
        return btn;
    }
}
