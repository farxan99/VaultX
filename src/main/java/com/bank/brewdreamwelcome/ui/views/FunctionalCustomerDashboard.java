package com.bank.brewdreamwelcome.ui.views;

import com.bank.brewdreamwelcome.core.ThemeManager;
import com.bank.brewdreamwelcome.core.SessionManager;
import com.bank.brewdreamwelcome.service.CustomerBankingService;
import com.bank.brewdreamwelcome.service.CustomerBankingService.AccountInfo;
import com.bank.brewdreamwelcome.service.CustomerBankingService.TransactionRecord;
import com.bank.brewdreamwelcome.validation.InputValidator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Functional Customer Dashboard with real data and operations.
 */
public class FunctionalCustomerDashboard extends JFrame {
    
    private final int customerId;
    private final CustomerBankingService bankingService;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private List<AccountInfo> accounts;
    private AccountInfo selectedAccount;
    
    public FunctionalCustomerDashboard(int customerId) {
        this.customerId = customerId;
        this.bankingService = CustomerBankingService.getInstance();
        
        setTitle("VaultX | Customer Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        
        loadAccounts();
        
        JPanel root = new JPanel(new MigLayout("fill, insets 0", "[260!]0[fill, grow]", "fill"));
        root.add(createSidebar(), "growy");
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(createOverviewPanel(), "OVERVIEW");
        contentPanel.add(createAccountsPanel(), "ACCOUNTS");
        contentPanel.add(createTransactionsPanel(), "TRANSACTIONS");
        contentPanel.add(createTransferPanel(), "TRANSFER");
        
        root.add(contentPanel, "grow");
        
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void loadAccounts() {
        accounts = bankingService.getCustomerAccounts(customerId);
        if (!accounts.isEmpty()) {
            selectedAccount = accounts.get(0);
        }
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("fillx, insets 20", "[fill]"));
        sidebar.setBackground(new Color(30, 41, 59));
        
        JLabel brand = new JLabel("VaultX");
        brand.setFont(new Font("Inter", Font.BOLD, 24));
        brand.setForeground(Color.WHITE);
        
        sidebar.add(brand, "wrap, gapbottom 40");
        sidebar.add(createNavButton("Overview", "OVERVIEW"), "wrap, height 45!");
        sidebar.add(createNavButton("My Accounts", "ACCOUNTS"), "wrap, height 45!");
        sidebar.add(createNavButton("Transactions", "TRANSACTIONS"), "wrap, height 45!");
        sidebar.add(createNavButton("Transfer Money", "TRANSFER"), "wrap, height 45!");
        sidebar.add(Box.createVerticalGlue(), "wrap, pushy");
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(220, 38, 38));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
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
        btn.setBackground(new Color(30, 41, 59));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.PLAIN, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(59, 130, 246));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(30, 41, 59));
            }
        });
        
        return btn;
    }
    
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 30", "[grow, fill]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel welcome = new JLabel("Welcome Back!");
        welcome.setFont(new Font("Inter", Font.BOLD, 28));
        
        // Calculate total balance
        double totalBalance = accounts.stream().mapToDouble(a -> a.balance).sum();
        
        JPanel statGrid = new JPanel(new MigLayout("fillx, insets 0", "[grow, fill][grow, fill][grow, fill]"));
        statGrid.setOpaque(false);
        
        statGrid.add(createStatCard("Total Balance", "$" + moneyFormat.format(totalBalance), new Color(59, 130, 246)));
        statGrid.add(createStatCard("Active Accounts", String.valueOf(accounts.size()), new Color(16, 185, 129)));
        statGrid.add(createStatCard("Account Status", "ACTIVE", new Color(245, 158, 11)));
        
        panel.add(welcome, "wrap, gapbottom 20");
        panel.add(statGrid, "growx, wrap, gapbottom 30");
        
        // Recent transactions
        if (selectedAccount != null) {
            JLabel txLabel = new JLabel("Recent Transactions");
            txLabel.setFont(new Font("Inter", Font.BOLD, 18));
            panel.add(txLabel, "wrap, gapbottom 10");
            panel.add(createRecentTransactionsTable(), "grow");
        }
        
        return panel;
    }
    
    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 30", "[grow, fill]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel title = new JLabel("My Accounts");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        
        panel.add(title, "wrap, gapbottom 20");
        
        for (AccountInfo account : accounts) {
            panel.add(createAccountCard(account), "wrap, gapbottom 15");
        }
        
        return panel;
    }
    
    private JPanel createAccountCard(AccountInfo account) {
        JPanel card = new JPanel(new MigLayout("fillx, insets 20", "[grow][]"));
        card.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        card.setBackground(Color.WHITE);
        
        JLabel accNo = new JLabel("Account: " + account.accountNumber);
        accNo.setFont(new Font("Inter", Font.BOLD, 16));
        
        JLabel type = new JLabel(account.accountType + " • " + account.branchName);
        type.setForeground(new Color(100, 116, 139));
        
        JLabel balance = new JLabel("$" + moneyFormat.format(account.balance));
        balance.setFont(new Font("Inter", Font.BOLD, 24));
        balance.setForeground(new Color(59, 130, 246));
        
        JButton depositBtn = new JButton("Deposit");
        depositBtn.setBackground(new Color(16, 185, 129));
        depositBtn.setForeground(Color.WHITE);
        depositBtn.setFocusPainted(false);
        depositBtn.setBorderPainted(false);
        depositBtn.addActionListener(e -> showDepositDialog(account));
        
        JButton withdrawBtn = new JButton("Withdraw");
        withdrawBtn.setBackground(new Color(245, 158, 11));
        withdrawBtn.setForeground(Color.WHITE);
        withdrawBtn.setFocusPainted(false);
        withdrawBtn.setBorderPainted(false);
        withdrawBtn.addActionListener(e -> showWithdrawDialog(account));
        
        JPanel leftPanel = new JPanel(new MigLayout("insets 0", "[]"));
        leftPanel.setOpaque(false);
        leftPanel.add(accNo, "wrap");
        leftPanel.add(type, "wrap, gapbottom 10");
        leftPanel.add(balance);
        
        JPanel rightPanel = new JPanel(new MigLayout("insets 0", "[]10[]"));
        rightPanel.setOpaque(false);
        rightPanel.add(depositBtn, "width 100!");
        rightPanel.add(withdrawBtn, "width 100!");
        
        card.add(leftPanel, "grow");
        card.add(rightPanel);
        
        return card;
    }
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 30", "[grow, fill]", "[][grow]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        
        panel.add(title, "wrap, gapbottom 20");
        panel.add(createRecentTransactionsTable(), "grow");
        
        return panel;
    }
    
    private JScrollPane createRecentTransactionsTable() {
        String[] columns = {"Date", "Type", "From", "To", "Amount", "Description"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        if (selectedAccount != null) {
            List<TransactionRecord> transactions = bankingService.getAccountTransactions(
                selectedAccount.accountNumber, 50
            );
            
            for (TransactionRecord tx : transactions) {
                model.addRow(new Object[]{
                    dateFormat.format(tx.timestamp),
                    tx.type,
                    tx.fromAccount != null ? tx.fromAccount : "-",
                    tx.toAccount != null ? tx.toAccount : "-",
                    "$" + moneyFormat.format(tx.amount),
                    tx.description != null ? tx.description : ""
                });
            }
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Inter", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        
        return new JScrollPane(table);
    }
    
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 30", "[grow, fill]"));
        panel.setBackground(ThemeManager.getBackground());
        
        JLabel title = new JLabel("Transfer Money");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        
        JPanel formPanel = new JPanel(new MigLayout("fillx, insets 20", "[grow, fill]"));
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        formPanel.setBackground(Color.WHITE);
        
        JComboBox<String> fromAccountCombo = new JComboBox<>();
        for (AccountInfo acc : accounts) {
            fromAccountCombo.addItem(acc.accountNumber + " ($" + moneyFormat.format(acc.balance) + ")");
        }
        
        JTextField toAccountField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField descField = new JTextField();
        
        JButton transferBtn = new JButton("Transfer Now");
        transferBtn.setBackground(new Color(59, 130, 246));
        transferBtn.setForeground(Color.WHITE);
        transferBtn.setFont(new Font("Inter", Font.BOLD, 14));
        transferBtn.setFocusPainted(false);
        transferBtn.setBorderPainted(false);
        transferBtn.addActionListener(e -> {
            int selectedIndex = fromAccountCombo.getSelectedIndex();
            if (selectedIndex >= 0) {
                AccountInfo fromAcc = accounts.get(selectedIndex);
                String toAcc = toAccountField.getText().trim();
                String amountStr = amountField.getText().trim();
                String desc = descField.getText().trim();
                
                // Validate inputs
                InputValidator.ValidationResult amountValid = InputValidator.validateAmount(amountStr);
                InputValidator.ValidationResult toAccValid = InputValidator.validateAccountNumber(toAcc);
                
                if (!amountValid.isValid()) {
                    JOptionPane.showMessageDialog(this, amountValid.getFirstError(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!toAccValid.isValid()) {
                    JOptionPane.showMessageDialog(this, toAccValid.getFirstError(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double amount = Double.parseDouble(amountStr);
                
                if (bankingService.transfer(fromAcc.accountNumber, toAcc, amount, desc)) {
                    JOptionPane.showMessageDialog(this, "Transfer successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    toAccountField.setText("");
                    amountField.setText("");
                    descField.setText("");
                    loadAccounts();
                    refreshContent();
                } else {
                    JOptionPane.showMessageDialog(this, "Transfer failed. Please check account details and balance.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        formPanel.add(new JLabel("From Account:"), "wrap, gapbottom 5");
        formPanel.add(fromAccountCombo, "wrap, height 40!, gapbottom 15");
        formPanel.add(new JLabel("To Account Number:"), "wrap, gapbottom 5");
        formPanel.add(toAccountField, "wrap, height 40!, gapbottom 15");
        formPanel.add(new JLabel("Amount:"), "wrap, gapbottom 5");
        formPanel.add(amountField, "wrap, height 40!, gapbottom 15");
        formPanel.add(new JLabel("Description (Optional):"), "wrap, gapbottom 5");
        formPanel.add(descField, "wrap, height 40!, gapbottom 20");
        formPanel.add(transferBtn, "height 50!");
        
        panel.add(title, "wrap, gapbottom 20");
        panel.add(formPanel, "width 600!");
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new MigLayout("insets 20", "[fill]"));
        card.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(100, 116, 139));
        titleLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, "wrap");
        card.add(valueLabel);
        
        return card;
    }
    
    private void showDepositDialog(AccountInfo account) {
        JTextField amountField = new JTextField();
        JTextField descField = new JTextField();
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Account: " + account.accountNumber));
        panel.add(new JLabel("Current Balance: $" + moneyFormat.format(account.balance)));
        panel.add(new JLabel("Amount to Deposit:"));
        panel.add(amountField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Deposit Money", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String amountStr = amountField.getText().trim();
            InputValidator.ValidationResult valid = InputValidator.validateAmount(amountStr);
            
            if (!valid.isValid()) {
                JOptionPane.showMessageDialog(this, valid.getFirstError(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double amount = Double.parseDouble(amountStr);
            if (bankingService.deposit(account.accountNumber, amount, descField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Deposit successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAccounts();
                refreshContent();
            } else {
                JOptionPane.showMessageDialog(this, "Deposit failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showWithdrawDialog(AccountInfo account) {
        JTextField amountField = new JTextField();
        JTextField descField = new JTextField();
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Account: " + account.accountNumber));
        panel.add(new JLabel("Current Balance: $" + moneyFormat.format(account.balance)));
        panel.add(new JLabel("Amount to Withdraw:"));
        panel.add(amountField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Withdraw Money", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String amountStr = amountField.getText().trim();
            InputValidator.ValidationResult valid = InputValidator.validateAmount(amountStr);
            
            if (!valid.isValid()) {
                JOptionPane.showMessageDialog(this, valid.getFirstError(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double amount = Double.parseDouble(amountStr);
            if (bankingService.withdraw(account.accountNumber, amount, descField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Withdrawal successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAccounts();
                refreshContent();
            } else {
                JOptionPane.showMessageDialog(this, "Withdrawal failed. Check balance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void refreshContent() {
        contentPanel.removeAll();
        contentPanel.add(createOverviewPanel(), "OVERVIEW");
        contentPanel.add(createAccountsPanel(), "ACCOUNTS");
        contentPanel.add(createTransactionsPanel(), "TRANSACTIONS");
        contentPanel.add(createTransferPanel(), "TRANSFER");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
