package com.bank.brewdreamwelcome.ui.views;

import net.miginfocom.swing.MigLayout;
import com.bank.brewdreamwelcome.core.ThemeManager;
import javax.swing.*;
import java.awt.*;

/**
 * Modern, Responsive Dashboard using MigLayout.
 * Eliminates absolute positioning and hardcoded sizes.
 */
public class ModernDashboardView extends JFrame {

    public ModernDashboardView() {
        setTitle("VaultX | Professional Banking");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        
        // Root Panel with Theme Support
        JPanel root = new JPanel(new MigLayout("fill, insets 0", "[260!]0[fill, grow]", "fill"));
        
        // 1. Sidebar (Fixed Width)
        JPanel sidebar = createSidebar();
        
        // 2. Main Content (Fluent & Responsive)
        JPanel content = createMainContent();
        
        root.add(sidebar, "growy");
        root.add(content, "grow");
        
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel(new MigLayout("fillx, insets 20", "[fill]"));
        p.setBackground(new Color(30, 41, 59));
        
        JLabel brand = new JLabel("VaultX");
        brand.setFont(new Font("Inter", Font.BOLD, 24));
        brand.setForeground(Color.WHITE);
        
        p.add(brand, "wrap, gapbottom 40");
        p.add(createNavButton("Overview", true), "wrap, height 45!");
        p.add(createNavButton("Accounts", false), "wrap, height 45!");
        p.add(createNavButton("Transfers", false), "wrap, height 45!");
        
        return p;
    }

    private JPanel createMainContent() {
        JPanel p = new JPanel(new MigLayout("fillx, insets 30", "[grow, fill]"));
        p.setBackground(ThemeManager.getBackground());

        JLabel welcome = new JLabel("Good Morning, User");
        welcome.setFont(new Font("Inter", Font.BOLD, 28));
        
        // Responsive Stat Grid (Auto-wrapping columns)
        JPanel statGrid = new JPanel(new MigLayout("fillx, insets 0", "[grow, fill][grow, fill][grow, fill]"));
        statGrid.setOpaque(false);
        
        statGrid.add(createStatCard("Total Balance", "$124,500.00"));
        statGrid.add(createStatCard("Monthly Income", "$12,300.20"));
        statGrid.add(createStatCard("Active Cards", "3"));

        p.add(welcome, "wrap, gapbottom 20");
        p.add(statGrid, "growx, wrap, gapbottom 30");
        p.add(new JSeparator(), "growx, wrap, gapbottom 30");
        
        return p;
    }

    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(active ? new Color(59, 130, 246) : new Color(30, 41, 59));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new MigLayout("insets 20", "[fill]"));
        card.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        card.setBackground(Color.WHITE);
        
        JLabel t = new JLabel(title);
        t.setForeground(new Color(100, 116, 139));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Inter", Font.BOLD, 22));
        
        card.add(t, "wrap");
        card.add(v);
        return card;
    }
}
