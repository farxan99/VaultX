package com.bank.brewdreamwelcome.core;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import javax.swing.*;
import java.awt.*;

/**
 * Modern Theme Engine supporting Dark/Light mode and global UI scaling.
 */
public class ThemeManager {
    
    public enum ThemeMode { LIGHT, DARK }
    private static ThemeMode currentMode = ThemeMode.DARK;

    // Standard Banking Palette
    public static final Color ACCENT_BLUE = new Color(37, 99, 235);
    public static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    public static final Color DANGER_RED = new Color(220, 38, 38);
    public static final Color WARNING_AMBER = new Color(245, 158, 11);

    public static void initialize() {
        // Set global scaling for High-DPI screens
        System.setProperty("flatlaf.uiScale", "1.2");
        applyTheme(currentMode);
    }

    public static void toggleTheme() {
        currentMode = (currentMode == ThemeMode.DARK) ? ThemeMode.LIGHT : ThemeMode.DARK;
        applyTheme(currentMode);
    }

    private static void applyTheme(ThemeMode mode) {
        try {
            FlatAnimatedLafChange.showSnapshot();
            if (mode == ThemeMode.DARK) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            
            // Customize global defaults
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
            
            // Update all windows
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Color getBackground() {
        return currentMode == ThemeMode.DARK ? new Color(15, 23, 42) : new Color(248, 250, 252);
    }
}
