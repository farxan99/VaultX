package com.bank.brewdreamwelcome;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Customer dashboard for VaultX Bank.
 * Limited access - customers can only view their own transactions.
 */
public class CustomerDashboardFrame extends JFrame {

    private final BankDatabaseService bankService = BankDatabaseService.getInstance();
    private final Integer customerId;
    private final String customerAccountId;

    private JPanel root;
    private JPanel sidebar;
    private JPanel contentCards;
    private CardLayout cardLayout;

    // Metrics labels (Dashboard)
    private JLabel myBalanceLabel;
    private JLabel accountTypesLabel;
    private JLabel todayTxLabel;

    // Tables
    private DefaultTableModel txModel;
    private JTable txTable;

    // Animation
    private float fadeIn = 0f;
    private Timer fadeTimer;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public CustomerDashboardFrame(Integer customerId) {
        super("VaultX Bank – Customer Dashboard");
        this.customerId = customerId;
        com.bank.brewdreamwelcome.Customer c = CustomerDatabaseService.getInstance().getCustomerById(customerId);
        this.customerAccountId = (c != null) ? c.getAccountId() : "UNKNOWN";

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 650));

        root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // VaultX image background plus soft diagonal gradient overlay
                VaultXTheme.paintBackgroundImage(g2, this);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0, 0, 0, 180),
                        getWidth(), getHeight(), new Color(15, 23, 42, 220));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Apply global fade-in
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeIn));

                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        createSidebar();
        createContent();

        setContentPane(root);
        startFadeIn();
        refreshAllData();
    }

    /* ============================ SIDEBAR ============================ */

    private void createSidebar() {
        sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(7, 17, 43),
                        0, getHeight(), new Color(16, 42, 84));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Logo
        JPanel logoPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = 54;
                int h = 54;
                int x = 0;
                int y = (getHeight() - h) / 2;

                // Outer circle
                g2.setColor(new Color(254, 215, 102));
                g2.fillOval(x, y, w, h);

                // Inner circle
                g2.setColor(new Color(17, 24, 39));
                g2.fillOval(x + 5, y + 5, w - 10, h - 10);

                // Stylized bank columns
                int baseY = y + h - 12;
                int colWidth = 6;
                int colHeight = 22;
                g2.setColor(new Color(248, 250, 252));
                for (int i = 0; i < 3; i++) {
                    int cx = x + 11 + i * (colWidth + 5);
                    g2.fillRoundRect(cx, baseY - colHeight, colWidth, colHeight, 4, 4);
                }

                // Roof triangle
                Polygon roof = new Polygon();
                roof.addPoint(x + 11, baseY - colHeight - 4);
                roof.addPoint(x + w / 2, y + 8);
                roof.addPoint(x + w - 11, baseY - colHeight - 4);
                g2.fillPolygon(roof);

                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(220, 60));

        JLabel title = new JLabel("VaultX Bank");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(248, 250, 252));

        JLabel subtitle = new JLabel("Customer Portal");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(148, 163, 184));

        // Account ID with reveal button
        JPanel accountIdPanel = new JPanel(new BorderLayout());
        accountIdPanel.setOpaque(false);

        JLabel accountIdLabel = new JLabel("Account ID: " + (customerAccountId != null ? "******" : "N/A"));
        accountIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        accountIdLabel.setForeground(new Color(148, 163, 184));

        JButton revealAccountIdBtn = new JButton("👁");
        revealAccountIdBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        revealAccountIdBtn.setForeground(new Color(148, 163, 184));
        revealAccountIdBtn.setOpaque(false);
        revealAccountIdBtn.setContentAreaFilled(false);
        revealAccountIdBtn.setBorderPainted(false);
        revealAccountIdBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        revealAccountIdBtn.setToolTipText("Reveal Account ID");

        boolean[] accountIdRevealed = { false };
        revealAccountIdBtn.addActionListener(e -> {
            if (!accountIdRevealed[0]) {
                accountIdLabel.setText("Account ID: " + (customerAccountId != null ? customerAccountId : "N/A"));
                revealAccountIdBtn.setText("🙈");
                accountIdRevealed[0] = true;
            } else {
                accountIdLabel.setText("Account ID: " + (customerAccountId != null ? "******" : "N/A"));
                revealAccountIdBtn.setText("👁");
                accountIdRevealed[0] = false;
            }
        });

        accountIdPanel.add(accountIdLabel, BorderLayout.WEST);
        accountIdPanel.add(revealAccountIdBtn, BorderLayout.EAST);

        JPanel textHolder = new JPanel();
        textHolder.setOpaque(false);
        textHolder.setLayout(new BoxLayout(textHolder, BoxLayout.Y_AXIS));
        textHolder.add(title);
        textHolder.add(Box.createVerticalStrut(3));
        textHolder.add(subtitle);
        textHolder.add(Box.createVerticalStrut(5));
        textHolder.add(accountIdPanel);

        logoPanel.add(textHolder, BorderLayout.CENTER);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(30));

        // Navigation - Limited options for customers
        sidebar.add(createNavItem("Dashboard", "My account overview", "DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavItem("Transactions", "My transaction history", "TRANSACTIONS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavItem("Make Transaction", "Deposit, Withdraw, Transfer", "MAKE_TRANSACTION"));

        sidebar.add(Box.createVerticalGlue());

        // Logout
        JButton logoutBtn = new JButton("← Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutBtn.setForeground(new Color(248, 250, 252));
        logoutBtn.setOpaque(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(148, 163, 184), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                logoutBtn.setBorder(new LineBorder(new Color(248, 250, 252), 1, true));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                logoutBtn.setBorder(new LineBorder(new Color(148, 163, 184), 1, true));
            }
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });

        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(logoutBtn, BorderLayout.EAST);

        sidebar.add(logoutWrapper);

        root.add(sidebar, BorderLayout.WEST);
    }

    private JPanel createNavItem(String title, String helper, String cardName) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(226, 232, 240));

        JLabel helperLabel = new JLabel(helper);
        helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helperLabel.setForeground(new Color(148, 163, 184));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(titleLabel);
        text.add(helperLabel);

        nav.add(text, BorderLayout.CENTER);

        nav.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nav.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                nav.setBackground(new Color(37, 99, 235));
                nav.setOpaque(true);
                nav.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                nav.setOpaque(false);
                nav.repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cardLayout.show(contentCards, cardName);
            }
        });

        return nav;
    }

    /* ============================ CONTENT ============================ */

    private void createContent() {
        contentCards = new JPanel();
        cardLayout = new CardLayout();
        contentCards.setLayout(cardLayout);
        contentCards.setOpaque(false);

        contentCards.add(createDashboardPanel(), "DASHBOARD");
        contentCards.add(createTransactionsPanel(), "TRANSACTIONS");
        contentCards.add(createMakeTransactionPanel(), "MAKE_TRANSACTION");

        root.add(contentCards, BorderLayout.CENTER);
    }

    private JPanel createCardContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(new Color(248, 250, 252));
        inner.setBorder(new EmptyBorder(18, 18, 18, 18));

        container.add(inner, BorderLayout.CENTER);
        return container;
    }

    /* ============================ DASHBOARD ============================ */

    private JPanel createDashboardPanel() {
        JPanel container = createCardContainer();
        JPanel card = (JPanel) container.getComponent(0);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("My Account Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Your personal banking summary");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 116, 139));

        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(title);
        t.add(subtitle);

        header.add(t, BorderLayout.WEST);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        card.add(header, BorderLayout.NORTH);

        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsRow.setOpaque(false);

        myBalanceLabel = createMetricTile("My Balance", "Total balance across all accounts", new Color(59, 130, 246));
        accountTypesLabel = createMetricTile("Account Types", "Types of accounts you have", new Color(45, 212, 191));
        todayTxLabel = createMetricTile("Today's Activity", "My transactions today", new Color(248, 113, 113));

        metricsRow.add(myBalanceLabel.getParent().getParent());
        metricsRow.add(accountTypesLabel.getParent().getParent());
        metricsRow.add(todayTxLabel.getParent().getParent());

        card.add(metricsRow, BorderLayout.CENTER);

        // Account Number Display with Reveal Button
        JPanel accountInfoPanel = new JPanel(new BorderLayout());
        accountInfoPanel.setOpaque(false);
        accountInfoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        accountInfoPanel.setBackground(Color.WHITE);

        JLabel accountInfoTitle = new JLabel("Account Information");
        accountInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        accountInfoTitle.setForeground(new Color(15, 23, 42));

        JPanel accountDetailsPanel = new JPanel();
        accountDetailsPanel.setLayout(new BoxLayout(accountDetailsPanel, BoxLayout.Y_AXIS));
        accountDetailsPanel.setOpaque(false);
        accountDetailsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Account ID with reveal button
        JLabel accountIdLabel = new JLabel("Account ID: ******");
        accountIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        accountIdLabel.setForeground(new Color(15, 23, 42));

        JButton revealAccountIdBtn = createEyeIconButton();
        revealAccountIdBtn.setToolTipText("Reveal Account ID");

        JPanel accountIdRow = new JPanel(new BorderLayout());
        accountIdRow.setOpaque(false);
        accountIdRow.add(accountIdLabel, BorderLayout.WEST);
        accountIdRow.add(revealAccountIdBtn, BorderLayout.EAST);

        // Account Number with reveal button
        JLabel accountNumberLabel = new JLabel("Account Number: ************");
        accountNumberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        accountNumberLabel.setForeground(new Color(15, 23, 42));

        JLabel balanceLabel = new JLabel("Balance: PKR 0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        balanceLabel.setForeground(new Color(15, 23, 42));
        balanceLabel.setVisible(false);

        JButton revealAccountNumberBtn = createEyeIconButton();
        revealAccountNumberBtn.setToolTipText("Reveal Account Number");

        JPanel accountNumberRow = new JPanel(new BorderLayout());
        accountNumberRow.setOpaque(false);
        accountNumberRow.add(accountNumberLabel, BorderLayout.WEST);
        accountNumberRow.add(revealAccountNumberBtn, BorderLayout.EAST);

        final boolean[] accountIdRevealed = { false };
        final boolean[] accountNumberRevealed = { false };
        final JLabel[] accountIdLabelRef = { accountIdLabel };
        final JLabel[] accountNumberLabelRef = { accountNumberLabel };
        final JLabel[] balanceLabelRef = { balanceLabel };

        // Account ID reveal button action
        revealAccountIdBtn.addActionListener(e -> {
            if (!accountIdRevealed[0]) {
                accountIdLabelRef[0].setText("Account ID: " + (customerAccountId != null ? customerAccountId : "N/A"));
                revealAccountIdBtn.setIcon(createHideIcon());
                accountIdRevealed[0] = true;
            } else {
                accountIdLabelRef[0].setText("Account ID: ******");
                revealAccountIdBtn.setIcon(createEyeIcon());
                accountIdRevealed[0] = false;
            }
        });

        // Account Number reveal button action
        revealAccountNumberBtn.addActionListener(e -> {
            List<BankAccount> accounts = getMyAccounts();
            if (accounts.isEmpty()) {
                accountNumberLabelRef[0].setText("Account Number: No account found");
                return;
            }

            String firstAccountNumber = accounts.get(0).getAccountNumber();
            double firstAccountBalance = accounts.get(0).getBalance();

            if (!accountNumberRevealed[0]) {
                accountNumberLabelRef[0].setText("Account Number: " + firstAccountNumber);
                balanceLabelRef[0].setText("Balance: PKR " + moneyFormat.format(firstAccountBalance));
                balanceLabelRef[0].setVisible(true);
                revealAccountNumberBtn.setIcon(createHideIcon());
                accountNumberRevealed[0] = true;
            } else {
                accountNumberLabelRef[0].setText("Account Number: ************");
                balanceLabelRef[0].setVisible(false);
                revealAccountNumberBtn.setIcon(createEyeIcon());
                accountNumberRevealed[0] = false;
            }
        });

        accountDetailsPanel.add(accountIdRow);
        accountDetailsPanel.add(Box.createVerticalStrut(10));
        accountDetailsPanel.add(accountNumberRow);
        accountDetailsPanel.add(Box.createVerticalStrut(5));
        accountDetailsPanel.add(balanceLabel);

        accountInfoPanel.add(accountInfoTitle, BorderLayout.NORTH);
        accountInfoPanel.add(accountDetailsPanel, BorderLayout.CENTER);

        card.add(accountInfoPanel, BorderLayout.SOUTH);

        return container;
    }

    private JLabel createMetricTile(String title, String helper, Color accent) {
        JPanel tile = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 32));
                g2.fillRoundRect(0, getHeight() - 22, getWidth(), 22, 20, 20);

                g2.dispose();
            }
        };
        tile.setOpaque(false);
        tile.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel helperLabel = new JLabel(helper);
        helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helperLabel.setForeground(new Color(100, 116, 139));

        JLabel valueLabel = new JLabel("–");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleLabel, BorderLayout.WEST);

        tile.add(top, BorderLayout.NORTH);
        tile.add(valueLabel, BorderLayout.CENTER);
        tile.add(helperLabel, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(tile, BorderLayout.CENTER);

        return valueLabel;
    }

    /* ============================ TRANSACTIONS ============================ */

    private JPanel createTransactionsPanel() {
        JPanel container = createCardContainer();
        JPanel card = (JPanel) container.getComponent(0);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("My Transactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Your complete transaction history");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));

        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(title);
        t.add(subtitle);

        header.add(t, BorderLayout.WEST);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        card.add(header, BorderLayout.NORTH);

        String[] cols = { "Txn ID", "Date / Time", "Type", "From", "To", "Amount", "Description" };
        txModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        txTable = new JTable(txModel);
        txTable.setRowHeight(24);
        txTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(txTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));

        card.add(scroll, BorderLayout.CENTER);

        return container;
    }

    /* ============================ DATA REFRESH ============================ */

    private void refreshAllData() {
        refreshMetrics();
        refreshTransactionsTable();
    }

    private void refreshMetrics() {
        // Get real-time balance from database
        double myBalance = bankService.getCustomerTotalBalance(customerId);
        myBalanceLabel.setText("PKR " + moneyFormat.format(myBalance));

        // Get account types
        String accountTypes = bankService.getCustomerAccountTypes(customerId);
        accountTypesLabel.setText(accountTypes);

        // Count today's transactions for this customer
        long todayCount = bankService.getCustomerTransactionsTodayCount(customerId);
        todayTxLabel.setText(todayCount + " transactions");
    }

    private List<BankAccount> getMyAccounts() {
        return bankService.getCustomerAccounts(customerId);
    }

    private void refreshTransactionsTable() {
        txModel.setRowCount(0);

        // Get all transactions from database
        List<BankTransaction> myTransactions = bankService.getCustomerTransactions(customerId);

        for (BankTransaction tx : myTransactions) {
            txModel.addRow(new Object[] {
                    tx.getId(),
                    tx.getTimestamp().format(dateTimeFormat),
                    tx.getType(),
                    tx.getFromAccount() == null ? "—" : tx.getFromAccount(),
                    tx.getToAccount() == null ? "—" : tx.getToAccount(),
                    "PKR " + moneyFormat.format(tx.getAmount()),
                    tx.getDescription()
            });
        }
    }

    /* ============================ MAKE TRANSACTION ============================ */

    private JPanel createMakeTransactionPanel() {
        JPanel container = createCardContainer();
        JPanel card = (JPanel) container.getComponent(0);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Make Transaction");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Deposit, withdraw, or transfer money");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));

        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(title);
        t.add(subtitle);

        header.add(t, BorderLayout.WEST);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        card.add(header, BorderLayout.NORTH);

        // Transaction type selection
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typePanel.setOpaque(false);

        ButtonGroup typeGroup = new ButtonGroup();
        JRadioButton depositBtn = new JRadioButton("Deposit", true);
        JRadioButton withdrawBtn = new JRadioButton("Withdraw");
        JRadioButton transferBtn = new JRadioButton("Transfer");

        depositBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        withdrawBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transferBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        typeGroup.add(depositBtn);
        typeGroup.add(withdrawBtn);
        typeGroup.add(transferBtn);

        typePanel.add(depositBtn);
        typePanel.add(Box.createHorizontalStrut(20));
        typePanel.add(withdrawBtn);
        typePanel.add(Box.createHorizontalStrut(20));
        typePanel.add(transferBtn);

        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Amount field
        JLabel amountLabel = new JLabel("Amount (PKR):");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        amountLabel.setForeground(new Color(15, 23, 42));

        JTextField amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // To account (for transfers)
        JLabel toAccountLabel = new JLabel("To Account Number:");
        toAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toAccountLabel.setForeground(new Color(15, 23, 42));
        toAccountLabel.setVisible(false);

        JTextField toAccountField = new JTextField();
        toAccountField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toAccountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        toAccountField.setVisible(false);

        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(15, 23, 42));

        JTextField descField = new JTextField();
        descField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Error label
        JLabel errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(239, 68, 68));
        errorLabel.setVisible(false);

        // Success label
        JLabel successLabel = new JLabel("");
        successLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        successLabel.setForeground(new Color(34, 197, 94));
        successLabel.setVisible(false);

        // Show/hide to account field based on transaction type
        depositBtn.addActionListener(e -> {
            toAccountLabel.setVisible(false);
            toAccountField.setVisible(false);
        });
        withdrawBtn.addActionListener(e -> {
            toAccountLabel.setVisible(false);
            toAccountField.setVisible(false);
        });
        transferBtn.addActionListener(e -> {
            toAccountLabel.setVisible(true);
            toAccountField.setVisible(true);
        });

        formPanel.add(toAccountLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(toAccountField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(amountLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(amountField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(descLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(descField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(errorLabel);
        formPanel.add(successLabel);

        // Submit button
        JButton submitBtn = new JButton("Process Transaction");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(new Color(59, 130, 246));
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        submitBtn.setPreferredSize(new Dimension(0, 40));

        submitBtn.addActionListener(e -> {
            errorLabel.setVisible(false);
            successLabel.setVisible(false);

            // Get first active account automatically
            List<BankAccount> accounts = getMyAccounts();
            if (accounts.isEmpty()) {
                showError(errorLabel, "No accounts available. Please contact admin.");
                return;
            }

            // Use first active account
            BankAccount firstAccount = accounts.stream()
                    .filter(BankAccount::isActive)
                    .findFirst()
                    .orElse(null);

            if (firstAccount == null) {
                showError(errorLabel, "No active accounts available.");
                return;
            }

            String accountNumber = firstAccount.getAccountNumber();

            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                showError(errorLabel, "Please enter an amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    showError(errorLabel, "Amount must be greater than 0");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError(errorLabel, "Please enter a valid amount");
                return;
            }

            String description = descField.getText().trim();
            if (description.isEmpty()) {
                description = depositBtn.isSelected() ? "Deposit"
                        : withdrawBtn.isSelected() ? "Withdrawal" : "Transfer";
            }

            boolean success = false;
            if (depositBtn.isSelected()) {
                success = bankService.deposit(accountNumber, amount, description);
                if (success) {
                    showSuccess(successLabel, "Deposit of PKR " + moneyFormat.format(amount) + " successful!");
                } else {
                    showError(errorLabel, "Deposit failed. Please try again.");
                }
            } else if (withdrawBtn.isSelected()) {
                success = bankService.withdraw(accountNumber, amount, description);
                if (success) {
                    showSuccess(successLabel, "Withdrawal of PKR " + moneyFormat.format(amount) + " successful!");
                } else {
                    showError(errorLabel, "Withdrawal failed. Insufficient balance or invalid account.");
                }
            } else if (transferBtn.isSelected()) {
                String toAccount = toAccountField.getText().trim();
                if (toAccount.isEmpty()) {
                    showError(errorLabel, "Please enter destination account number");
                    return;
                }

                // Check if to account exists
                BankAccount toAccountObj = bankService.findAccount(toAccount);
                if (toAccountObj == null) {
                    showError(errorLabel, "Destination account not found");
                    return;
                }

                success = bankService.transfer(accountNumber, toAccount, amount, description);
                if (success) {
                    showSuccess(successLabel, "Transfer of PKR " + moneyFormat.format(amount) + " successful!");
                } else {
                    showError(errorLabel, "Transfer failed. Insufficient balance or invalid account.");
                }
            }

            if (success) {
                // Clear fields
                amountField.setText("");
                descField.setText("");
                toAccountField.setText("");

                // Refresh all data
                refreshAllData();
            }
        });

        formPanel.add(submitBtn);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(typePanel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);

        card.add(centerPanel, BorderLayout.CENTER);

        return container;
    }

    private void showError(JLabel label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    private void showSuccess(JLabel label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    /**
     * Creates an eye icon button for revealing/hiding sensitive information.
     */
    private JButton createEyeIconButton() {
        JButton btn = new JButton(createEyeIcon());
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(24, 24));
        return btn;
    }

    /**
     * Creates an eye icon (show).
     */
    private Icon createEyeIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                // Draw eye shape
                g2.drawOval(x + 2, y + 4, 8, 6);
                g2.fillOval(x + 5, y + 6, 2, 2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    /**
     * Creates a hide icon (eye with slash).
     */
    private Icon createHideIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 116, 139));
                // Draw eye shape
                g2.drawOval(x + 2, y + 4, 8, 6);
                g2.fillOval(x + 5, y + 6, 2, 2);
                // Draw slash line
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(x + 1, y + 1, x + 15, y + 15);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    /* ============================ ANIMATION ============================ */

    private void startFadeIn() {
        fadeIn = 0f;
        fadeTimer = new Timer(16, e -> {
            fadeIn = Math.min(1f, fadeIn + 0.03f);
            root.repaint();
            if (fadeIn >= 1f) {
                fadeTimer.stop();
            }
        });
        fadeTimer.start();
    }
}
