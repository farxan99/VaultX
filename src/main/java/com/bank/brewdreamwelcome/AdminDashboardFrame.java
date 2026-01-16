package com.bank.brewdreamwelcome;

import com.bank.brewdreamwelcome.core.ThemeManager;
import com.bank.brewdreamwelcome.core.SessionManager;
import com.bank.brewdreamwelcome.service.AccountApprovalService;
import com.bank.brewdreamwelcome.service.AccountApprovalService.PendingAccount;
import com.bank.brewdreamwelcome.ui.views.ModernLoginView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Modern Admin Dashboard for VaultX.
 * Features:
 * - Account Approvals (Pending/Approve/Reject)
 * - Customer Management
 * - Transaction Monitoring
 * - System Overview
 */
public class AdminDashboardFrame extends JFrame {

    private final AccountApprovalService approvalService = AccountApprovalService.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public AdminDashboardFrame() {
        setTitle("VaultX | Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        
        // Root layout
        JPanel root = new JPanel(new MigLayout("fill, insets 0", "[260!]0[fill, grow]", "fill"));
        root.setBackground(ThemeManager.getBackground());
        
        // Sidebar
        root.add(createSidebar(), "growy");
        
        // Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ThemeManager.getBackground());
        
        contentPanel.add(createOverviewPanel(), "OVERVIEW");
        contentPanel.add(createApprovalsPanel(), "APPROVALS");
        contentPanel.add(createCustomersPanel(), "CUSTOMERS");
        contentPanel.add(createTransactionsPanel(), "TRANSACTIONS");
        
        root.add(contentPanel, "grow");
        
        setContentPane(root);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("fillx, insets 20", "[fill]"));
        sidebar.setBackground(new Color(15, 23, 42)); // Dark Slate
        
        JLabel brand = new JLabel("VaultX Admin");
        brand.setFont(new Font("Inter", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        
        sidebar.add(brand, "wrap, gapbottom 40");
        
        sidebar.add(createNavButton("Overview", "OVERVIEW"), "wrap, height 45!");
        sidebar.add(createNavButton("Pending Approvals", "APPROVALS"), "wrap, height 45!");
        sidebar.add(createNavButton("All Customers", "CUSTOMERS"), "wrap, height 45!");
        sidebar.add(createNavButton("System Transactions", "TRANSACTIONS"), "wrap, height 45!");
        
        sidebar.add(Box.createVerticalGlue(), "wrap, pushy");
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(ThemeManager.DANGER_RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFont(new Font("Inter", Font.BOLD, 14));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            SessionManager.clearSession();
            dispose();
            new ModernLoginView().setVisible(true);
        });
        sidebar.add(logoutBtn, "height 45!");
        
        return sidebar;
    }
    
    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(new Color(15, 23, 42));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.PLAIN, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            refreshData(cardName);
        });
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(30, 41, 59));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(15, 23, 42));
            }
        });
        
        return btn;
    }
    
    private void refreshData(String cardName) {
        if ("APPROVALS".equals(cardName)) {
            contentPanel.add(createApprovalsPanel(), "APPROVALS");
        } else if ("CUSTOMERS".equals(cardName)) {
            contentPanel.add(createCustomersPanel(), "CUSTOMERS");
        } else if ("TRANSACTIONS".equals(cardName)) {
            contentPanel.add(createTransactionsPanel(), "TRANSACTIONS");
        } else if ("OVERVIEW".equals(cardName)) {
            contentPanel.add(createOverviewPanel(), "OVERVIEW");
        }
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 40", "[grow, fill][grow, fill][grow, fill]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel title = new JLabel("System Overview");
        title.setFont(new Font("Inter", Font.BOLD, 28));
        panel.add(title, "span, wrap, gapbottom 30");
        
        // Stats
        int pendingCount = approvalService.getPendingAccounts().size();
        int totalCustomers = 0;
        double totalReserves = 0.0;
        
        try (Connection conn = com.bank.brewdreamwelcome.config.DatabaseConfig.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
            if (rs.next()) totalCustomers = rs.getInt(1);
            
            rs = conn.createStatement().executeQuery("SELECT SUM(balance) FROM accounts");
            if (rs.next()) totalReserves = rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }

        panel.add(createStatCard("Pending Approvals", String.valueOf(pendingCount), 
            pendingCount > 0 ? ThemeManager.DANGER_RED : ThemeManager.SUCCESS_GREEN));
            
        panel.add(createStatCard("Total Customers", String.valueOf(totalCustomers), ThemeManager.ACCENT_BLUE));
        
        panel.add(createStatCard("Total Reserves", "$" + String.format("%,.2f", totalReserves), new Color(245, 158, 11)));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new MigLayout("insets 25", "[fill]"));
        card.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        card.setBackground(Color.WHITE);
        
        JLabel t = new JLabel(title);
        t.setForeground(new Color(100, 116, 139));
        t.setFont(new Font("Inter", Font.PLAIN, 14));
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Inter", Font.BOLD, 32));
        v.setForeground(color);
        
        card.add(t, "wrap, gapbottom 5");
        card.add(v);
        return card;
    }
    
    // ================== APPROVALS PANEL ==================
    
    private JPanel createApprovalsPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 30", "[grow, fill]", "[][grow]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel title = new JLabel("Pending Account Approvals");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        panel.add(title, "wrap, gapbottom 20");
        
        // Table Config
        String[] cols = {"ID", "Name", "Email", "ID Card", "Account ID", "Requested At", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 6; } // Only actions clickable
        };
        
        List<PendingAccount> pendingList = approvalService.getPendingAccounts();
        for (PendingAccount p : pendingList) {
            model.addRow(new Object[]{
                p.customerId, p.name, p.email, p.idCard, p.accountId, dateFormat.format(p.createdAt), "Approve / Reject"
            });
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        
        // Action Button Renderer/Editor
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), table, pendingList));
        
        panel.add(new JScrollPane(table), "grow");
        
        if (pendingList.isEmpty()) {
            JLabel empty = new JLabel("No pending approvals found.", SwingConstants.CENTER);
            empty.setFont(new Font("Inter", Font.ITALIC, 16));
            empty.setForeground(Color.GRAY);
            panel.add(empty, "dock south, height 50!");
        }
        
        return panel;
    }
    
    // ================== CUSTOMERS PANEL ==================
    
    private JPanel createCustomersPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 30", "[grow, fill]", "[][grow]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel title = new JLabel("All Customers");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        panel.add(title, "wrap, gapbottom 20");
        
        String[] cols = {"ID", "Name", "Email", "Account ID", "Status", "Joined"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        String sql = "SELECT id, name, email, account_id, account_status, created_at FROM customers ORDER BY created_at DESC";
        try (Connection conn = com.bank.brewdreamwelcome.config.DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("account_id"),
                    rs.getString("account_status"),
                    rs.getTimestamp("created_at") != null ? dateFormat.format(rs.getTimestamp("created_at")) : "-"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Inter", Font.PLAIN, 12));
        
        panel.add(new JScrollPane(table), "grow");
        return panel;
    }
    
    // ================== TRANSACTIONS PANEL ==================
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 30", "[grow, fill]", "[][grow]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel title = new JLabel("System Transactions");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        panel.add(title, "wrap, gapbottom 20");
        
        String[] cols = {"ID", "Type", "From Account", "To Account", "Amount ($)", "Date", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 100";
        try (Connection conn = com.bank.brewdreamwelcome.config.DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("transaction_id"),
                    rs.getString("transaction_type"),
                    rs.getString("from_account"),
                    rs.getString("to_account"),
                    String.format("%,.2f", rs.getDouble("amount")),
                    dateFormat.format(rs.getTimestamp("timestamp")),
                    rs.getString("description")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Inter", Font.PLAIN, 12));
        
        panel.add(new JScrollPane(table), "grow");
        return panel;
    }
    
    // ================== HELPERS ==================

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("Review");
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private final JTable table;
        private final List<PendingAccount> pendingList;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox, JTable table, List<PendingAccount> pendingList) {
            super(checkBox);
            this.table = table;
            this.pendingList = pendingList;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            button.setText("Review");
            return button;
        }

        public Object getCellEditorValue() {
            return "Review";
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
            handleReview(currentRow);
        }
        
        private void handleReview(int row) {
            if (row < 0 || row >= pendingList.size()) return;
            PendingAccount p = pendingList.get(row);
            
            String[] options = {"Approve", "Reject", "Cancel"};
            int choice = JOptionPane.showOptionDialog(AdminDashboardFrame.this,
                "Action for customer: " + p.name + "\nID Card: " + p.idCard,
                "Review Account Request",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
                
            if (choice == 0) { // Approve
                 boolean success = approvalService.approveAccount(p.customerId, 
                     SessionManager.getCurrentAdminId() != null ? SessionManager.getCurrentAdminId() : 1);
                 if (success) {
                     JOptionPane.showMessageDialog(AdminDashboardFrame.this, "Account Approved!");
                     refreshData("APPROVALS");
                 }
            } else if (choice == 1) { // Reject
                String reason = JOptionPane.showInputDialog(AdminDashboardFrame.this, "Enter rejection reason:");
                if (reason != null && !reason.trim().isEmpty()) {
                    boolean success = approvalService.rejectAccount(p.customerId,
                        SessionManager.getCurrentAdminId() != null ? SessionManager.getCurrentAdminId() : 1,
                        reason);
                    if (success) {
                        JOptionPane.showMessageDialog(AdminDashboardFrame.this, "Account Rejected!");
                        refreshData("APPROVALS");
                    }
                }
            }
        }
    }
}
