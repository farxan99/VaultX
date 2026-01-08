package com.bank.brewdreamwelcome;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Admin dashboard for the VaultX Bank Management System.
 * Full access to all customers, accounts, and transactions.
 * Uses database-backed services for data persistence.
 */
public class AdminDashboardFrame extends JFrame {

    private final BankDatabaseService bankService = BankDatabaseService.getInstance();
    private final CustomerDatabaseService customerService = CustomerDatabaseService.getInstance();

    private JPanel root;
    private JPanel sidebar;
    private JPanel contentCards;
    private CardLayout cardLayout;

    // Metrics labels (Dashboard)
    private JLabel totalCustomersLabel;
    private JLabel totalAccountsLabel;
    private JLabel totalBalanceLabel;
    private JLabel todayTxLabel;

    // Tables
    private DefaultTableModel accountsModel;
    private JTable accountsTable;

    private DefaultTableModel customersModel;
    private JTable customersTable;

    private DefaultTableModel txModel;
    private JTable txTable;
    private TableRowSorter<DefaultTableModel> txSorter;
    private JComboBox<String> txTypeFilter;
    private JComboBox<String> txAccountFilter;

    // Animation
    private float fadeIn = 0f;
    private Timer fadeTimer;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private String adminUsername;

    public AdminDashboardFrame() {
        this(null);
    }

    public AdminDashboardFrame(String adminUsername) {
        super("VaultX Bank – Admin Dashboard");
        this.adminUsername = adminUsername;
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

        JLabel subtitle = new JLabel("Admin Console");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(148, 163, 184));

        // Admin Username with reveal button
        JPanel adminUsernamePanel = new JPanel(new BorderLayout());
        adminUsernamePanel.setOpaque(false);

        JLabel adminUsernameLabel = new JLabel("Username: " + (adminUsername != null ? "******" : "N/A"));
        adminUsernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        adminUsernameLabel.setForeground(new Color(148, 163, 184));

        JButton revealAdminUsernameBtn = createEyeIconButton();
        revealAdminUsernameBtn.setToolTipText("Reveal Username");

        final boolean[] adminUsernameRevealed = { false };
        final String[] usernameRef = { adminUsername };
        revealAdminUsernameBtn.addActionListener(e -> {
            if (!adminUsernameRevealed[0]) {
                adminUsernameLabel.setText("Username: " + (usernameRef[0] != null ? usernameRef[0] : "N/A"));
                revealAdminUsernameBtn.setIcon(createHideIcon());
                adminUsernameRevealed[0] = true;
            } else {
                adminUsernameLabel.setText("Username: " + (usernameRef[0] != null ? "******" : "N/A"));
                revealAdminUsernameBtn.setIcon(createEyeIcon());
                adminUsernameRevealed[0] = false;
            }
        });

        adminUsernamePanel.add(adminUsernameLabel, BorderLayout.WEST);
        adminUsernamePanel.add(revealAdminUsernameBtn, BorderLayout.EAST);

        JPanel textHolder = new JPanel();
        textHolder.setOpaque(false);
        textHolder.setLayout(new BoxLayout(textHolder, BoxLayout.Y_AXIS));
        textHolder.add(title);
        textHolder.add(Box.createVerticalStrut(3));
        textHolder.add(subtitle);
        textHolder.add(Box.createVerticalStrut(5));
        textHolder.add(adminUsernamePanel);

        logoPanel.add(textHolder, BorderLayout.CENTER);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(30));

        // Navigation
        sidebar.add(createNavItem("Dashboard", "Overview of bank KPIs", "DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavItem("Accounts", "Open / close & manage", "ACCOUNTS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavItem("Customers", "Customer CRUD", "CUSTOMERS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavItem("Transactions", "History & audit trail", "TRANSACTIONS"));

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

        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBorder(new LineBorder(new Color(248, 250, 252), 1, true));
            }

            @Override
            public void mouseExited(MouseEvent e) {
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
        nav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Solid hover highlight (no ghosting of background/logo)
                nav.setBackground(new Color(37, 99, 235));
                nav.setOpaque(true);
                nav.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nav.setOpaque(false);
                nav.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
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
        contentCards.add(createAccountsPanel(), "ACCOUNTS");
        contentCards.add(createCustomersPanel(), "CUSTOMERS");
        contentCards.add(createTransactionsPanel(), "TRANSACTIONS");

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

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Live summary of your bank performance");
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

        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsRow.setOpaque(false);

        totalCustomersLabel = createMetricTile("Customers", "Total registered customers", new Color(59, 130, 246));
        totalAccountsLabel = createMetricTile("Accounts", "Open accounts in system", new Color(45, 212, 191));
        totalBalanceLabel = createMetricTile("Total Balance", "Sum of all account balances", new Color(245, 158, 11));
        todayTxLabel = createMetricTile("Today's Activity", "Transactions & net flow", new Color(248, 113, 113));

        metricsRow.add(totalCustomersLabel.getParent().getParent());
        metricsRow.add(totalAccountsLabel.getParent().getParent());
        metricsRow.add(totalBalanceLabel.getParent().getParent());
        metricsRow.add(todayTxLabel.getParent().getParent());

        card.add(metricsRow, BorderLayout.CENTER);

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

        // Return the valueLabel but keep structure accessible
        return valueLabel;
    }

    /* ============================ ACCOUNTS ============================ */

    private JPanel createAccountsPanel() {
        JPanel container = createCardContainer();
        JPanel card = (JPanel) container.getComponent(0);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Accounts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Open, manage, deposit, withdraw and transfer");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));

        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(title);
        t.add(subtitle);

        header.add(t, BorderLayout.WEST);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        card.add(header, BorderLayout.NORTH);

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));
        toolbar.setOpaque(false);

        toolbar.add(createToolbarButton("Open Account", e -> onOpenAccount()));
        toolbar.add(createToolbarButton("Edit Account", e -> onEditAccount()));
        toolbar.add(createToolbarButton("Close Account", e -> onCloseAccount()));
        toolbar.add(createToolbarButton("Delete Account", e -> onDeleteAccount()));
        // Admin restriction: Removed Deposit/Withdraw/Transfer buttons
        // toolbar.addSeparator(new Dimension(12, 0));
        // toolbar.add(createToolbarButton("Deposit", e -> onDeposit()));
        // toolbar.add(createToolbarButton("Withdraw", e -> onWithdraw()));
        // toolbar.add(createToolbarButton("Transfer", e -> onTransfer()));

        card.add(toolbar, BorderLayout.SOUTH);

        // Table
        String[] cols = { "Account No", "Customer", "Type", "Branch", "Opened", "Status", "Balance" };
        accountsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        accountsTable = new JTable(accountsModel);
        accountsTable.setRowHeight(24);
        accountsTable.setFillsViewportHeight(true);
        accountsTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(accountsTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        card.add(scroll, BorderLayout.CENTER);

        return container;
    }

    /* ============================ CUSTOMERS ============================ */

    private JPanel createCustomersPanel() {
        JPanel container = createCardContainer();
        JPanel card = (JPanel) container.getComponent(0);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Customers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Create, edit and remove customers");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));

        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(title);
        t.add(subtitle);

        header.add(t, BorderLayout.WEST);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);
        toolbar.add(createToolbarButton("Add Customer", e -> onAddCustomer()));
        toolbar.add(createToolbarButton("Edit Customer", e -> onEditCustomer()));
        toolbar.add(createToolbarButton("Delete Customer", e -> onDeleteCustomer()));
        toolbar.addSeparator(new Dimension(12, 0));
        toolbar.add(createToolbarButton("Promote to Admin", e -> onPromoteCustomer()));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);
        top.setBorder(new EmptyBorder(0, 0, 10, 0));

        card.add(top, BorderLayout.NORTH);

        String[] cols = { "Customer ID", "Account ID", "ID Card", "Name", "Email", "Phone", "Address" };
        customersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customersTable = new JTable(customersModel);
        customersTable.setRowHeight(24);
        customersTable.getTableHeader().setReorderingAllowed(false);

        // Allow quick edit on double-click
        customersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && customersTable.getSelectedRow() >= 0) {
                    onEditCustomer();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(customersTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        card.add(scroll, BorderLayout.CENTER);

        return container;
    }

    /* ============================ TRANSACTIONS ============================ */

    private JPanel createTransactionsPanel() {
        JPanel container = createCardContainer();
        JPanel card = (JPanel) container.getComponent(0);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Transactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Complete audit trail of account movements");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));

        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(title);
        t.add(subtitle);

        header.add(t, BorderLayout.WEST);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Filters row
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLabel.setForeground(new Color(100, 116, 139));

        String[] typeOptions = new String[TransactionType.values().length + 1];
        typeOptions[0] = "All types";
        int idx = 1;
        for (TransactionType tt : TransactionType.values()) {
            typeOptions[idx++] = tt.name();
        }
        txTypeFilter = new JComboBox<>(typeOptions);
        txTypeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel accountLabel = new JLabel("Account:");
        accountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountLabel.setForeground(new Color(100, 116, 139));

        txAccountFilter = new JComboBox<>();
        txAccountFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        populateTxAccountFilter();

        filterPanel.add(typeLabel);
        filterPanel.add(txTypeFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(accountLabel);
        filterPanel.add(txAccountFilter);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);
        headerContainer.add(header, BorderLayout.NORTH);
        headerContainer.add(filterPanel, BorderLayout.SOUTH);

        card.add(headerContainer, BorderLayout.NORTH);

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

        txSorter = new TableRowSorter<>(txModel);
        txTable.setRowSorter(txSorter);

        java.awt.event.ActionListener filterListener = e -> applyTxFilter();
        txTypeFilter.addActionListener(filterListener);
        txAccountFilter.addActionListener(filterListener);

        JScrollPane scroll = new JScrollPane(txTable);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));

        card.add(scroll, BorderLayout.CENTER);

        return container;
    }

    /* ============================ TOOLBAR HELPERS ============================ */

    private JButton createToolbarButton(String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setBackground(new Color(239, 246, 255));
        btn.setForeground(new Color(30, 64, 175));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    /* ============================ OPERATIONS ============================ */

    private void onOpenAccount() {
        java.util.List<Customer> customers = customerService.getAllCustomers();
        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please create at least one customer first.",
                    "No customers",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<Customer> customerBox = new JComboBox<>(
                customers.toArray(new Customer[0]));
        JComboBox<AccountType> typeBox = new JComboBox<>(AccountType.values());
        JTextField branchField = new JTextField("Main Branch");
        JTextField initialField = new JTextField("1000.00");

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Customer:"));
        panel.add(customerBox);
        panel.add(new JLabel("Account type:"));
        panel.add(typeBox);
        panel.add(new JLabel("Branch:"));
        panel.add(branchField);
        panel.add(new JLabel("Initial deposit:"));
        panel.add(initialField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Open New Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            double amount = Double.parseDouble(initialField.getText().trim());
            if (amount < 0) {
                throw new NumberFormatException();
            }
            Customer customer = (Customer) customerBox.getSelectedItem();
            AccountType type = (AccountType) typeBox.getSelectedItem();
            String branch = branchField.getText().trim();
            if (branch.isEmpty()) {
                branch = "Main Branch";
            }
            // Extract customer ID and call database service
            int customerId = customer.getId();
            BankAccount newAccount = bankService.openAccount(customerId, type, amount, branch);
            if (newAccount != null) {
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to open account. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid non-negative number for initial deposit.",
                    "Invalid amount",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private BankAccount getSelectedAccount() {
        int row = accountsTable.getSelectedRow();
        if (row < 0)
            return null;
        String accNo = accountsModel.getValueAt(row, 0).toString();
        return bankService.findAccount(accNo);
    }

    private void onCloseAccount() {
        BankAccount acc = getSelectedAccount();
        if (acc == null) {
            JOptionPane.showMessageDialog(this, "Select an account first.", "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!acc.isActive()) {
            JOptionPane.showMessageDialog(this, "This account is already closed.", "Already closed",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (acc.getBalance() != 0.0) {
            JOptionPane.showMessageDialog(this,
                    "Account balance must be 0.00 before closing.\nCurrent balance: " +
                            moneyFormat.format(acc.getBalance()),
                    "Cannot close account",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int res = JOptionPane.showConfirmDialog(this,
                "Close account " + acc.getAccountNumber() + " ?",
                "Confirm close",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            if (bankService.closeAccount(acc.getAccountNumber())) {
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to close account. Please check the account status.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEditAccount() {
        BankAccount acc = getSelectedAccount();
        if (acc == null) {
            JOptionPane.showMessageDialog(this,
                    "Select an account first.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField branchField = new JTextField(acc.getBranchName());
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 4));
        p.add(new JLabel("Account: " + acc.getAccountNumber()));
        p.add(new JLabel("Customer: " + acc.getCustomer().getName()));
        p.add(new JLabel("Branch name:"));
        p.add(branchField);

        int res = JOptionPane.showConfirmDialog(this,
                p,
                "Edit Account Details",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }
        String branch = branchField.getText().trim();
        if (bankService.updateAccountBranch(acc.getAccountNumber(), branch)) {
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update account branch.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteAccount() {
        BankAccount acc = getSelectedAccount();
        if (acc == null) {
            JOptionPane.showMessageDialog(this,
                    "Select an account first.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (acc.getBalance() != 0.0) {
            JOptionPane.showMessageDialog(this,
                    "Account balance must be 0.00 before deleting.\nCurrent balance: " +
                            moneyFormat.format(acc.getBalance()),
                    "Cannot delete account",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int res = JOptionPane.showConfirmDialog(this,
                "Delete account " + acc.getAccountNumber() + " permanently?\n" +
                        "This will remove the record from the system.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.YES_OPTION) {
            return;
        }
        if (!bankService.deleteAccount(acc.getAccountNumber())) {
            JOptionPane.showMessageDialog(this,
                    "Unable to delete account. Please verify its state.",
                    "Delete failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        refreshAllData();
    }

    /* ============================ CUSTOMER CRUD ============================ */

    private void onAddCustomer() {
        CustomerForm form = new CustomerForm(this, null);
        form.setVisible(true);
        if (form.isSaved()) {
            String dummyIdCard = "ADM-" + System.currentTimeMillis();
            Customer newCustomer = customerService.createCustomer(null, form.name, form.email, form.phone, form.address,
                    form.password, dummyIdCard, "SAVINGS");
            if (newCustomer != null) {
                refreshAllData();
                JOptionPane.showMessageDialog(this,
                        "Customer created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to create customer. Email may already exist or password is invalid.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Customer getSelectedCustomer() {
        int row = customersTable.getSelectedRow();
        if (row < 0)
            return null;
        String id = customersModel.getValueAt(row, 0).toString();
        try {
            int customerId = Integer.parseInt(id);
            return customerService.getCustomerById(customerId);
        } catch (NumberFormatException e) {
            LoggerUtil.error("Invalid customer ID: " + id, e);
            return null;
        }
    }

    private void onEditCustomer() {
        Customer c = getSelectedCustomer();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Select a customer first.", "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        CustomerForm form = new CustomerForm(this, c);
        form.setVisible(true);
        if (form.isSaved()) {
            int customerId = c.getId();
            if (customerService.updateCustomer(customerId, form.name, form.email, form.phone, form.address)) {
                refreshAllData();
                JOptionPane.showMessageDialog(this,
                        "Customer updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update customer.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteCustomer() {
        Customer c = getSelectedCustomer();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Select a customer first.", "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int res = JOptionPane.showConfirmDialog(this,
                "Delete customer " + c.getName() + " (" + c.getId() + ")?\n" +
                        "All their accounts will be marked closed.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            int customerId = c.getId();
            if (customerService.deleteCustomer(customerId)) {
                refreshAllData();
                JOptionPane.showMessageDialog(this,
                        "Customer deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete customer.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onPromoteCustomer() {
        int row = customersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a customer to promote.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = (int) customersModel.getValueAt(row, 0);
        String customerName = (String) customersModel.getValueAt(row, 3); // Name is index 3 now

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to promote " + customerName + " to Admin?\n" +
                        "They will be able to log in as an administrator using their current username/password.",
                "Confirm Promotion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = CustomerDatabaseService.getInstance().promoteCustomerToAdmin(customerId);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Customer promoted to Admin successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to promote customer. They might already be an admin or lack a username.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ============================ DATA REFRESH ============================ */

    private void refreshAllData() {
        refreshMetrics();
        refreshAccountsTable();
        refreshCustomersTable();
        refreshTransactionsTable();
    }

    private void refreshMetrics() {
        totalCustomersLabel.setText(String.valueOf(bankService.getTotalCustomers()));
        totalAccountsLabel.setText(bankService.getTotalActiveAccounts() + " active / " +
                bankService.getTotalAccounts());
        totalBalanceLabel.setText("PKR " + moneyFormat.format(bankService.getTotalBalance()));
        todayTxLabel.setText(bankService.getTransactionsTodayCount() + " tx, " +
                "net PKR " + moneyFormat.format(bankService.getTodayNetFlow()));
    }

    private void refreshAccountsTable() {
        accountsModel.setRowCount(0);
        for (BankAccount a : bankService.getAllAccounts()) {
            accountsModel.addRow(new Object[] {
                    a.getAccountNumber(),
                    a.getCustomer().getName(),
                    a.getType().getDisplayName(),
                    a.getBranchName(),
                    a.getOpenedAt(),
                    a.isActive() ? "Active" : "Closed",
                    "PKR " + moneyFormat.format(a.getBalance())
            });
        }
    }

    private void refreshCustomersTable() {
        customersModel.setRowCount(0);
        for (Customer c : customerService.getAllCustomers()) {
            customersModel.addRow(new Object[] {
                    c.getId(),
                    c.getAccountId(),
                    c.getIdCardNumber(),
                    c.getName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getAddress()
            });
        }
    }

    private void refreshTransactionsTable() {
        txModel.setRowCount(0);
        for (BankTransaction tx : bankService.getAllTransactions()) {
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
        populateTxAccountFilter();
        applyTxFilter();
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
                g2.setColor(new Color(148, 163, 184));
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

    private void populateTxAccountFilter() {
        if (txAccountFilter == null) {
            return;
        }
        Object selected = txAccountFilter.getSelectedItem();
        txAccountFilter.removeAllItems();
        txAccountFilter.addItem("All accounts");
        for (BankAccount a : bankService.getAllAccounts()) {
            txAccountFilter.addItem(a.getAccountNumber());
        }
        if (selected != null) {
            txAccountFilter.setSelectedItem(selected);
        }
    }

    private void applyTxFilter() {
        if (txSorter == null || txTypeFilter == null || txAccountFilter == null) {
            return;
        }
        final String typeSel = (String) txTypeFilter.getSelectedItem();
        final String accSel = (String) txAccountFilter.getSelectedItem();

        if ((typeSel == null || typeSel.startsWith("All")) &&
                (accSel == null || accSel.startsWith("All"))) {
            txSorter.setRowFilter(null);
            return;
        }

        txSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String typeVal = entry.getStringValue(2);
                String fromVal = entry.getStringValue(3);
                String toVal = entry.getStringValue(4);

                if (typeSel != null && !typeSel.startsWith("All")) {
                    if (typeVal == null || !typeSel.equals(typeVal)) {
                        return false;
                    }
                }

                if (accSel != null && !accSel.startsWith("All")) {
                    boolean matches = (fromVal != null && fromVal.contains(accSel)) ||
                            (toVal != null && toVal.contains(accSel));
                    if (!matches) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /*
     * ============================ INNER CUSTOMER FORM ============================
     */

    private static class CustomerForm extends JDialog {
        private boolean saved = false;
        private final JTextField nameField = new JTextField();
        private final JTextField emailField = new JTextField();
        private final JTextField phoneField = new JTextField();
        private final JTextField addressField = new JTextField();
        private final JPasswordField passwordField = new JPasswordField();

        String name;
        String email;
        String phone;
        String address;
        String password;

        CustomerForm(JFrame owner, Customer existing) {
            super(owner, true);
            setTitle(existing == null ? "Add Customer" : "Edit Customer");
            setSize(420, 260);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(0, 1, 6, 4));
            form.setBorder(new EmptyBorder(12, 12, 12, 12));

            form.add(new JLabel("Name:"));
            form.add(nameField);
            form.add(new JLabel("Email:"));
            form.add(emailField);
            form.add(new JLabel("Phone:"));
            form.add(phoneField);
            form.add(new JLabel("Address:"));
            form.add(addressField);

            // Only show password field when creating new customer
            if (existing == null) {
                form.add(new JLabel("Password (8+ chars, uppercase, lowercase, digit, special):"));
                form.add(passwordField);
            }

            if (existing != null) {
                nameField.setText(existing.getName());
                emailField.setText(existing.getEmail());
                phoneField.setText(existing.getPhone());
                addressField.setText(existing.getAddress());
            }

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancel");
            JButton save = new JButton("Save");
            buttons.add(cancel);
            buttons.add(save);

            cancel.addActionListener(e -> dispose());
            save.addActionListener(e -> {
                String n = nameField.getText().trim();
                if (n.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name is required.", "Validation", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate password for new customers
                if (existing == null) {
                    password = new String(passwordField.getPassword());
                    if (password.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Password is required.", "Validation",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    PasswordValidator.ValidationResult validation = PasswordValidator.validate(password);
                    if (!validation.isValid()) {
                        JOptionPane.showMessageDialog(this,
                                "Password validation failed:\n" + validation.getErrorMessage(), "Validation",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                name = n;
                email = emailField.getText().trim();
                phone = phoneField.getText().trim();
                address = addressField.getText().trim();
                saved = true;
                dispose();
            });

            add(form, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);
        }

        boolean isSaved() {
            return saved;
        }
    }
}
