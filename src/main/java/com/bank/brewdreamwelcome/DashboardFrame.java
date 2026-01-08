package com.bank.brewdreamwelcome;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.*;
import org.jfree.data.category.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Brew Dream Coffee – Premium Dashboard with JFreeChart
 * Updated: Improved Menu view — 3x3 mall-style grid with polished card UI, hover glow, edit/delete icons,
 * smooth scrolling, consistent spacing, and fully qualified Swing Timer usage to avoid ambiguity.
 *
 * Note: java.util.* wildcard removed (replaced by explicit java.util.ArrayList import).
 * All Timer usages that referred to Swing timers are now either javax.swing.Timer or rely on the existing javax.swing.* import.
 */
public class DashboardFrame extends JFrame {
    
    private JPanel sidebar;
    private JPanel contentPanel;
    private JPopupMenu userPopupMenu;
    private boolean sidebarExpanded = true;
    private String currentUser = "New Admin Name";
    private String userEmail = "admin@brewdream.com";
    private float fadeIn = 0f;
    private javax.swing.Timer animationTimer;
    private javax.swing.Timer sidebarTimer;
    private int sidebarTargetWidth = 250;
    private int sidebarCurrentWidth = 250;
    private JButton avatarBtn;
    private JButton hamburgerBtn;
    
    // Store nav panels and their labels for animation
    private ArrayList<JPanel> navPanels = new ArrayList<>();
    private ArrayList<JLabel> navTextLabels = new ArrayList<>();
    private ArrayList<JLabel> navIconLabels = new ArrayList<>();
    private JLabel logoLabel;
    
    // Dashboard data
    private double totalSales = 0.0;
    private int totalOrders = 0;
    private int totalCustomers = 0;
    private double avgOrderValue = 0.0;
    
    // Order status data
    private int pendingOrders = 0;
    private int completedOrders = 0;
    private int cancelledOrders = 0;
    
    // Popular items data
    private String[] popularItems = {"Mocha", "Latte", "Black Coffee", "Americano", "Espresso"};
    private int[] itemSales = {0, 0, 0, 0, 0};
    
    // Recent orders data
    private Object[][] recentOrdersData = {};
    private String[] recentOrdersColumns = {"Order ID", "Customer", "Items", "Total", "Status"};

    // Customer data (for Customers view)
    private Object[][] customersData = {
        {"C-1001", "Amanda Peterson", "amanda.peterson@example.com", 18, "$325.50", "Online"},
        {"C-1002", "John Smith", "john.smith@example.com", 12, "$210.00", "Offline"},
        {"C-1003", "Sarah Johnson", "s.johnson@example.com", 9, "$185.75", "Online"},
        {"C-1004", "Mike Davis", "mike.davis@example.com", 6, "$130.20", "Offline"},
        {"C-1005", "Lisa Rodriguez", "lisa.rod@example.com", 22, "$410.30", "Online"}
    };
    private final String[] customersColumns = {
        "Customer ID", "Name", "Email", "Total Orders", "Lifetime Value", "Status"
    };

    // Orders data (for Orders view – uses same structure as recentOrdersColumns)
    private Object[][] allOrdersData = {};
    
    // Menu items data - START EMPTY
    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    
    // JFreeChart components
    private JFreeChart pieChart;
    private JFreeChart barChart;
    // Note: DefaultPieDataset in JFreeChart 1.5.0 is not generic
    private DefaultPieDataset pieDataset;
    private DefaultCategoryDataset barDataset;
    private ChartPanel pieChartPanel;
    private ChartPanel barChartPanel;
    
    // UI Components for dynamic updates
    private MetricCard totalSalesCard;
    private MetricCard ordersCard;
    private MetricCard customersCard;
    private MetricCard avgOrderCard;
    private JTable recentOrdersTable;
    
    // Current view
    private String currentView = "Dashboard";
    
    // Bottom buttons panel (to show/hide)
    private JPanel bottomButtonPanel;
    
    // Colors
    private final Color PRIMARY_COLOR = new Color(90, 60, 40);
    private final Color SECONDARY_COLOR = new Color(140, 100, 70);
    private final Color BACKGROUND_COLOR = new Color(250, 245, 240);
    private final Color CARD_BACKGROUND = new Color(255, 252, 248);
    private final Color BORDER_COLOR = new Color(220, 210, 200);
    private final Color TEXT_PRIMARY = new Color(60, 40, 20);
    private final Color TEXT_SECONDARY = new Color(100, 80, 60);
    private final Color SIDEBAR_COLOR = new Color(70, 50, 30);
    private final Color SIDEBAR_ACTIVE = new Color(100, 70, 50);
    private final Color TABLE_HEADER_BG = new Color(70, 50, 30);
    private final Color MENU_GRID_BG = new Color(245, 240, 235);
    
    // Order prices
    private double[] itemPrices = {4.50, 4.50, 3.75, 4.00, 3.50};
    
    // Regex patterns for validation
    private final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private final Pattern DESC_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,!?]+$");
    
    public DashboardFrame() {
        super("Brew Dream Coffee – Dashboard");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        setLayout(new BorderLayout());
        
        createSidebar();
        createContentArea();
        
        // Add some demo menu items so the grid is visible on first run (optional)
        if (menuItems.isEmpty()) {
            menuItems.add(new MenuItem("New Black Coffee", 3.75, "Black Coffee drink description", null));
            menuItems.add(new MenuItem("Espresso", 3.50, "Pure, concentrated coffee shot", null));
            menuItems.add(new MenuItem("Cappuccino", 4.75, "Equal parts espresso, steamed milk and foam", null));
            menuItems.add(new MenuItem("Latte", 4.50, "Creamy espresso with steamed milk", null));
            menuItems.add(new MenuItem("Mocha", 5.00, "Chocolate and coffee delight", null));
        }
        
        startFadeAnimation();
        
        // Add test button
        addTestButton();
    }
    
    private void addTestButton() {
        bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomButtonPanel.setOpaque(false);
        bottomButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton simulateBtn = createBlackButton("GET NEW ORDER", new Color(60, 180, 100));
        simulateBtn.addActionListener(e -> simulateNewOrder());
        
        JButton resetBtn = createBlackButton("RESET ALL DATA", new Color(220, 60, 60));
        resetBtn.addActionListener(e -> resetData());
        
        JButton clearOrdersBtn = createBlackButton("CLEAR ORDERS", new Color(70, 130, 200));
        clearOrdersBtn.addActionListener(e -> clearRecentOrders());
        
        bottomButtonPanel.add(clearOrdersBtn);
        bottomButtonPanel.add(resetBtn);
        bottomButtonPanel.add(simulateBtn);
        
        add(bottomButtonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createBlackButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            new EmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 3),
                    new EmptyBorder(10, 20, 10, 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2),
                    new EmptyBorder(10, 20, 10, 20)
                ));
            }
        });
        
        return button;
    }
    
    private void createSidebar() {
        sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(SIDEBAR_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(sidebarCurrentWidth, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));
        
        // Logo and Hamburger Menu
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setMaximumSize(new Dimension(250, 60));
        
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
        
        hamburgerBtn = new JButton("☰") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("☰")) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString("☰", x, y);
                g2.dispose();
            }
        };
        hamburgerBtn.setPreferredSize(new Dimension(35, 35));
        hamburgerBtn.setOpaque(false);
        hamburgerBtn.setContentAreaFilled(false);
        hamburgerBtn.setBorderPainted(false);
        hamburgerBtn.setFocusPainted(false);
        hamburgerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hamburgerBtn.addActionListener(e -> toggleSidebar());
        
        logoLabel = new JLabel("Brew Dream");
        logoLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        logoPanel.add(hamburgerBtn, BorderLayout.WEST);
        logoPanel.add(logoLabel, BorderLayout.CENTER);
        topPanel.add(logoPanel, BorderLayout.WEST);
        
        sidebar.add(topPanel);
        sidebar.add(Box.createVerticalStrut(40));
        
        // Clear previous nav panels
        navPanels.clear();
        navTextLabels.clear();
        navIconLabels.clear();
        
        // Navigation items
        String[] navItems = {"Dashboard", "Menu", "Customers", "Orders", "Users"};
        String[] navIcons = {"📊", "📋", "👥", "📦", "👤"};
        
        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            JPanel navPanel = createNavItem(navIcons[i], navItems[i], i == 0);
            navPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    switchView(navItems[index]);
                }
            });
            sidebar.add(navPanel);
            sidebar.add(Box.createVerticalStrut(8));
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        add(sidebar, BorderLayout.WEST);
    }
    
    private void switchView(String viewName) {
        currentView = viewName;
        
        // Show/hide bottom buttons
        if (bottomButtonPanel != null) {
            bottomButtonPanel.setVisible(viewName.equals("Dashboard"));
        }
        
        updateContentPanel();
    }
    
    private void updateContentPanel() {
        contentPanel.removeAll();
        
        switch (currentView) {
            case "Dashboard" -> createDashboardContent();
            case "Menu" -> createMenuContent();
            case "Customers" -> createCustomersContent();
            case "Orders" -> createOrdersContent();
            case "Users" -> createUsersContent();
            default -> contentPanel.add(createPlaceholderPanel(currentView), BorderLayout.CENTER);
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void createDashboardContent() {
        // Header
        JPanel header = createPageHeader("Dashboard");
        contentPanel.add(header, BorderLayout.NORTH);
        
        // Main content with scroll
        JPanel mainContent = new JPanel();
        mainContent.setOpaque(false);
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Key Metrics Cards
        JPanel metricsPanel = createMetricsPanel();
        mainContent.add(metricsPanel);
        mainContent.add(Box.createVerticalStrut(30));
        
        // Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        
        // Create pie chart dataset
        pieDataset = new DefaultPieDataset();
        updatePieChartData();
        
        // Create pie chart
        pieChart = createPieChart(pieDataset);
        pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(350, 350));
        pieChartPanel.setOpaque(false);
        pieChartPanel.setBackground(CARD_BACKGROUND);
        
        JPanel ordersStatusPanel = createChartPanel("Orders Status", pieChartPanel, true);
        chartsRow.add(ordersStatusPanel);
        
        // Create bar chart dataset
        barDataset = new DefaultCategoryDataset();
        updateBarChartData();
        
        // Create bar chart
        barChart = createBarChart(barDataset);
        barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(350, 350));
        barChartPanel.setOpaque(false);
        barChartPanel.setBackground(CARD_BACKGROUND);
        
        JPanel popularItemsPanel = createChartPanel("Popular Items", barChartPanel, false);
        chartsRow.add(popularItemsPanel);
        
        mainContent.add(chartsRow);
        mainContent.add(Box.createVerticalStrut(30));
        
        // Recent Orders Table
        JPanel recentOrdersPanel = createRecentOrdersPanel();
        mainContent.add(recentOrdersPanel);
        
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createMenuContent() {
        // Header with title and add button
        JPanel header = createMenuHeader();
        contentPanel.add(header, BorderLayout.NORTH);
        
        // Main grid content
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(false);
        
        if (menuItems.isEmpty()) {
            // Empty state with beautiful design
            JPanel emptyState = createEmptyStatePanel();
            gridContainer.add(emptyState, BorderLayout.CENTER);
        } else {
            // Grid of menu items — improved mall-style 3x3 grid
            JPanel gridPanel = createMenuGrid();
            JScrollPane scrollPane = new JScrollPane(gridPanel);
            scrollPane.setBorder(null);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            gridContainer.add(scrollPane, BorderLayout.CENTER);
        }
        
        contentPanel.add(gridContainer, BorderLayout.CENTER);
    }

    private void createCustomersContent() {
        JPanel header = createPageHeader("Customers");
        contentPanel.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(CARD_BACKGROUND);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Customers Overview");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        int onlineCount = 0;
        for (Object[] row : customersData) {
            if (row.length >= 6 && "Online".equals(row[5])) {
                onlineCount++;
            }
        }
        JLabel summary = new JLabel("Total: " + customersData.length + "  •  Online now: " + onlineCount);
        summary.setFont(new Font("SansSerif", Font.PLAIN, 13));
        summary.setForeground(TEXT_SECONDARY);

        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(summary, BorderLayout.EAST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        DefaultTableModel model = new DefaultTableModel(customersData, customersColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable customersTable = new JTable(model);
        styleTable(customersTable);

        customersTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        customersTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        customersTable.getColumnModel().getColumn(2).setPreferredWidth(210);
        customersTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        customersTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        customersTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(customersTable);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(CARD_BACKGROUND);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        contentPanel.add(card, BorderLayout.CENTER);
    }

    private void createOrdersContent() {
        JPanel header = createPageHeader("Orders");
        contentPanel.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(CARD_BACKGROUND);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("All Orders");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Filter by status:");
        filterLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filterLabel.setForeground(TEXT_SECONDARY);

        String[] statuses = {"All statuses", "Completed", "Pending", "Cancelled"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        filterPanel.add(filterLabel);
        filterPanel.add(statusCombo);

        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(filterPanel, BorderLayout.EAST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        DefaultTableModel model = new DefaultTableModel(allOrdersData, recentOrdersColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable ordersTable = new JTable(model);
        styleTable(ordersTable);

        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(90);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        ordersTable.setRowSorter(sorter);

        statusCombo.addActionListener(e -> {
            String selected = (String) statusCombo.getSelectedItem();
            if (selected == null || selected.startsWith("All")) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        Object value = entry.getValue(4);
                        return value != null && selected.equals(value.toString());
                    }
                });
            }
        });

        JScrollPane scroll = new JScrollPane(ordersTable);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(CARD_BACKGROUND);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        contentPanel.add(card, BorderLayout.CENTER);
    }

    private void createUsersContent() {
        JPanel header = createPageHeader("Users");
        contentPanel.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(CARD_BACKGROUND);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Team & Roles");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage who can access your Brew Dream admin panel");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_SECONDARY);

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        titlePanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        Object[][] usersData = {
                {"U-0001", "Admin", userEmail, "Administrator", "Active"},
                {"U-0002", "Barista Team", "baristas@brewdream.com", "Barista", "Active"},
                {"U-0003", "Manager", "manager@brewdream.com", "Manager", "Inactive"}
        };
        String[] usersColumns = {"User ID", "Name", "Email", "Role", "Status"};

        DefaultTableModel model = new DefaultTableModel(usersData, usersColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable usersTable = new JTable(model);
        styleTable(usersTable);

        usersTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        usersTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        usersTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        usersTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        usersTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(usersTable);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(CARD_BACKGROUND);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        contentPanel.add(card, BorderLayout.CENTER);
    }
    
    /**
     * createMenuGrid: Builds a clean 3-column, scrollable grid.
     * Uses GridLayout so cards never overflow horizontally – they shrink with the window.
     */
    private JPanel createMenuGrid() {
        JPanel gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setBackground(MENU_GRID_BG);
        gridPanel.setBorder(new EmptyBorder(20, 20, 40, 20));
        gridPanel.setLayout(new GridLayout(0, 3, 24, 24)); // rows auto, 3 columns, consistent gaps

        for (MenuItem item : menuItems) {
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(createMenuItemCard(item)); // center card in each cell
            gridPanel.add(wrapper);
        }

        return gridPanel;
    }
    
    /**
     * createMenuItemCard: now returns an instance of the new inner MenuCard class —
     * a mall-style card with rounded corners, image area, name, description, price chip,
     * edit & delete icon buttons, hover glow, and animated border.
     */
    private JPanel createMenuItemCard(MenuItem item) {
        return new MenuCard(item);
    }
    
    private JPanel createMenuHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel("☕");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));
        iconLabel.setForeground(SECONDARY_COLOR);
        
        JLabel title = new JLabel("Café Shop Menu");
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(title);
        
        // Add item button with beautiful styling
        JButton addItemBtn = new JButton("+ Add New Item") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(100, 80, 60),
                    getWidth(), getHeight(), new Color(70, 50, 30)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Draw border
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                
                // Draw text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = "+ Add New Item";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 3;
                g2.drawString(text, x, y);
                
                g2.dispose();
            }
        };
        addItemBtn.setPreferredSize(new Dimension(180, 50));
        addItemBtn.setOpaque(false);
        addItemBtn.setContentAreaFilled(false);
        addItemBtn.setBorderPainted(false);
        addItemBtn.setFocusPainted(false);
        addItemBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addItemBtn.addActionListener(e -> showAddItemDialog());
        
        // Add hover effect
        addItemBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addItemBtn.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                addItemBtn.repaint();
            }
        });
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(addItemBtn, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createEmptyStatePanel() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setOpaque(false);
        emptyPanel.setBorder(new EmptyBorder(100, 50, 100, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        
        // Large coffee icon
        JLabel coffeeIcon = new JLabel("☕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 120));
                g2.setColor(new Color(180, 140, 100));
                FontMetrics fm = g2.getFontMetrics();
                String text = "☕";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 10;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        coffeeIcon.setPreferredSize(new Dimension(200, 200));
        emptyPanel.add(coffeeIcon, gbc);
        
        // Title
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel title = new JLabel("Menu is Empty");
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        emptyPanel.add(title, gbc);
        
        // Description
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 40, 0);
        JLabel description = new JLabel("Add your first menu item to get started");
        description.setFont(new Font("SansSerif", Font.PLAIN, 16));
        description.setForeground(TEXT_SECONDARY);
        emptyPanel.add(description, gbc);
        
        // Add button
        gbc.gridy = 3;
        JButton addBtn = new JButton("Add Your First Item") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(100, 80, 60),
                    getWidth(), 0, new Color(70, 50, 30)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 25, 25);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String text = "Add Your First Item";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 3;
                g2.drawString(text, x, y);
                
                g2.dispose();
            }
        };
        addBtn.setPreferredSize(new Dimension(250, 60));
        addBtn.setOpaque(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddItemDialog());
        
        // Hover effect
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addBtn.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                addBtn.repaint();
            }
        });
        
        emptyPanel.add(addBtn, gbc);
        
        return emptyPanel;
    }
    
    private JButton createGridActionButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw button background
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Draw border
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                
                // Draw text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String btnText = getText();
                int x = (getWidth() - fm.stringWidth(btnText)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(btnText, x, y);
                
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(120, 35));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    private void showAddItemDialog() {
        JDialog dialog = new JDialog(this, "Add New Coffee Product", true);
        dialog.setSize(550, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CARD_BACKGROUND);
        
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BACKGROUND);
        
        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel("➕");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        iconLabel.setForeground(PRIMARY_COLOR);
        
        JLabel titleLabel = new JLabel("Add New Coffee Product");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        contentPanel.add(titlePanel);
        contentPanel.add(Box.createVerticalStrut(25));
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        
        // Name field
        JLabel nameLabel = new JLabel("Coffee Name:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Add document filter for name
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                if (text.matches("[a-zA-Z\\s]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
        formPanel.add(nameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Price field with dollar prefix
        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        priceLabel.setForeground(TEXT_PRIMARY);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel pricePanel = new JPanel(new BorderLayout());
        pricePanel.setOpaque(false);
        pricePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel dollarLabel = new JLabel("$");
        dollarLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        dollarLabel.setForeground(TEXT_PRIMARY);
        dollarLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        JTextField priceField = new JTextField();
        priceField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        priceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Add document filter for price (numbers and decimal only)
        ((AbstractDocument) priceField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                
                // Allow only numbers and one decimal point
                if (newText.matches("^\\d*\\.?\\d{0,2}$")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
        pricePanel.add(dollarLabel, BorderLayout.WEST);
        pricePanel.add(priceField, BorderLayout.CENTER);
        
        formPanel.add(priceLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(pricePanel);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Description field
        JLabel descLabel = new JLabel("Coffee Description:");
        descLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        descLabel.setForeground(TEXT_PRIMARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        
        // Add document filter for description
        ((AbstractDocument) descArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                if (text.matches("[a-zA-Z0-9\\s.,!?]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        descScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        formPanel.add(descLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(descScroll);
        formPanel.add(Box.createVerticalStrut(25));
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(200, 190, 180));
        formPanel.add(separator);
        formPanel.add(Box.createVerticalStrut(25));
        
        // Image upload area
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setOpaque(false);
        imagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel imageTitle = new JLabel("Upload Coffee Image");
        imageTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        imageTitle.setForeground(TEXT_PRIMARY);
        imageTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Track selected image file and show its name
        final File[] selectedImageFile = new File[1];
        JLabel selectedImageLabel = new JLabel("No image selected");
        selectedImageLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        selectedImageLabel.setForeground(TEXT_SECONDARY);
        selectedImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel dropZone = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(245, 240, 235),
                    getWidth(), getHeight(), new Color(235, 230, 225)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Draw dashed border
                g2.setColor(new Color(180, 170, 160));
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
                    0, new float[]{5, 5}, 0));
                g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 15, 15);
                
                // Draw upload icon
                g2.setColor(new Color(150, 130, 110));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 48));
                FontMetrics fm = g2.getFontMetrics();
                String plus = "📁";
                int x = (getWidth() - fm.stringWidth(plus)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 25;
                g2.drawString(plus, x, y);
                
                // Draw text
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                fm = g2.getFontMetrics();
                String text1 = "Drag & drop image here";
                String text2 = "or";
                x = (getWidth() - fm.stringWidth(text1)) / 2;
                g2.drawString(text1, x, y + 40);
                x = (getWidth() - fm.stringWidth(text2)) / 2;
                g2.drawString(text2, x, y + 60);
                
                g2.dispose();
            }
        };
        dropZone.setPreferredSize(new Dimension(400, 180));
        dropZone.setMaximumSize(new Dimension(400, 180));
        dropZone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JButton browseBtn = new JButton("Browse Files") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(100, 80, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String text = "Browse Files";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(text, x, y);
                
                g2.dispose();
            }
        };
        browseBtn.setPreferredSize(new Dimension(120, 35));
        browseBtn.setOpaque(false);
        browseBtn.setContentAreaFilled(false);
        browseBtn.setBorderPainted(false);
        browseBtn.setFocusPainted(false);
        browseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        browseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Coffee Image");
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif");
            fileChooser.addChoosableFileFilter(filter);
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                selectedImageFile[0] = file;
                selectedImageLabel.setText(file.getName());
            }
        });
        
        dropZone.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                browseBtn.doClick();
            }
        });
        
        imagePanel.add(imageTitle);
        imagePanel.add(Box.createVerticalStrut(15));
        imagePanel.add(dropZone);
        imagePanel.add(Box.createVerticalStrut(15));
        imagePanel.add(browseBtn);
        imagePanel.add(Box.createVerticalStrut(5));
        imagePanel.add(selectedImageLabel);
        
        formPanel.add(imagePanel);
        formPanel.add(Box.createVerticalStrut(30));
        
        contentPanel.add(formPanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton cancelBtn = createDialogButton("Cancel", new Color(200, 200, 200), Color.BLACK);
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton addItemBtn = createDialogButton("Publish Coffee", new Color(60, 180, 100), Color.BLACK);
        addItemBtn.addActionListener(e -> {
            if (validateAndAddItem(
                    nameField.getText(),
                    priceField.getText(),
                    descArea.getText(),
                    selectedImageFile[0])) {
                dialog.dispose();
                showSuccessDialog("Coffee product published to your shop menu!");
                updateContentPanel();
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(addItemBtn);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private JButton createDialogButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                
                g2.setColor(textColor);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String btnText = getText();
                int x = (getWidth() - fm.stringWidth(btnText)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(btnText, x, y);
                
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(120, 40));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    private boolean validateAndAddItem(String name, String price, String description, File imageFile) {
        // Name validation
        if (name == null || name.trim().isEmpty()) {
            showErrorDialog("Please enter a name for the menu item.");
            return false;
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            showErrorDialog("Name can only contain letters and spaces.");
            return false;
        }
        
        // Price validation
        if (price == null || price.trim().isEmpty()) {
            showErrorDialog("Please enter a price for the menu item.");
            return false;
        }
        
        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                showErrorDialog("Price must be greater than 0.");
                return false;
            }
            if (priceValue > 999.99) {
                showErrorDialog("Price cannot exceed $999.99.");
                return false;
            }
            
            // Description validation
            if (description == null || description.trim().isEmpty()) {
                showErrorDialog("Please enter a description for the menu item.");
                return false;
            }
            
            if (!DESC_PATTERN.matcher(description).matches()) {
                showErrorDialog("Description can only contain letters, numbers, spaces, and basic punctuation.");
                return false;
            }
            
            // Add the item (image is optional)
            String imagePath = (imageFile != null) ? imageFile.getAbsolutePath() : null;
            MenuItem newItem = new MenuItem(name.trim(), priceValue, description.trim(), imagePath);
            menuItems.add(newItem);
            
            return true;
            
        } catch (NumberFormatException e) {
            showErrorDialog("Please enter a valid price (numbers only).");
            return false;
        }
    }
    
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Validation Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void editMenuItem(MenuItem item) {
        // For now, show a simple edit dialog
        String newName = JOptionPane.showInputDialog(this,
            "Enter new name for " + item.getName() + ":",
            item.getName());
        
        if (newName != null && !newName.trim().isEmpty()) {
            if (!NAME_PATTERN.matcher(newName).matches()) {
                showErrorDialog("Name can only contain letters and spaces.");
                return;
            }
            item.setName(newName.trim());
            updateContentPanel();
            showSuccessDialog("Menu item updated successfully!");
        }
    }
    
    private void deleteMenuItem(MenuItem item) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><div style='text-align: center;'>" +
            "Are you sure you want to delete<br><b>" + item.getName() + "</b>?<br><br>" +
            "<font color='#DC3C3C'>This action cannot be undone.</font></div></html>",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            menuItems.remove(item);
            updateContentPanel();
            showSuccessDialog("Menu item deleted successfully!");
        }
    }
    
    private JPanel createPlaceholderPanel(String viewName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(100, 100, 100, 100));
        
        JLabel placeholder = new JLabel(viewName + " Page - Coming Soon!");
        placeholder.setFont(new Font("Georgia", Font.BOLD, 32));
        placeholder.setForeground(TEXT_PRIMARY);
        placeholder.setHorizontalAlignment(JLabel.CENTER);
        
        panel.add(placeholder, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        sidebarTargetWidth = sidebarExpanded ? 250 : 80;
        
        if (sidebarTimer != null && sidebarTimer.isRunning()) {
            sidebarTimer.stop();
        }
        
        sidebarTimer = new javax.swing.Timer(5, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sidebarCurrentWidth < sidebarTargetWidth) {
                    sidebarCurrentWidth += 5;
                } else if (sidebarCurrentWidth > sidebarTargetWidth) {
                    sidebarCurrentWidth -= 5;
                } else {
                    sidebarTimer.stop();
                }
                
                sidebar.setPreferredSize(new Dimension(sidebarCurrentWidth, 0));
                
                // Update sidebar components based on current width
                updateSidebarComponents();
                
                revalidate();
                repaint();
            }
        });
        sidebarTimer.start();
    }
    
    private void updateSidebarComponents() {
        // Calculate animation progress
        float progress = Math.abs((float)(sidebarCurrentWidth - 80) / 170);
        boolean showText = sidebarCurrentWidth > 100;
        boolean showLogo = sidebarCurrentWidth > 120;
        
        // Update logo
        logoLabel.setVisible(showLogo);
        if (showLogo && sidebarCurrentWidth < 250) {
            logoLabel.setFont(new Font("Georgia", Font.BOLD, 
                Math.max(16, (int)(20 * progress))));
        } else {
            logoLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        }
        
        // Update nav items
        for (int i = 0; i < navPanels.size(); i++) {
            JPanel panel = navPanels.get(i);
            JLabel iconLabel = navIconLabels.get(i);
            JLabel textLabel = navTextLabels.get(i);
            
            // Show/hide text based on sidebar width
            textLabel.setVisible(showText);
            
            // Adjust icon size
            if (!showText) {
                iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
                panel.removeAll();
                panel.setLayout(new BorderLayout());
                
                JPanel iconContainer = new JPanel(new GridBagLayout());
                iconContainer.setOpaque(false);
                iconContainer.add(iconLabel);
                
                panel.add(iconContainer, BorderLayout.CENTER);
                panel.setBorder(new EmptyBorder(10, 5, 10, 5));
            } else {
                iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
                panel.removeAll();
                panel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
                panel.add(iconLabel);
                panel.add(textLabel);
                
                if (sidebarCurrentWidth < 250) {
                    textLabel.setFont(new Font("SansSerif", Font.PLAIN, 
                        Math.max(10, (int)(14 * progress))));
                } else {
                    textLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                }
                panel.setBorder(new EmptyBorder(10, 15, 10, 15));
            }
            
            panel.revalidate();
            panel.repaint();
        }
    }
    
    private JPanel createNavItem(String icon, String text, boolean active) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(true);
        panel.setBackground(active ? SIDEBAR_ACTIVE : new Color(80, 55, 35));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(250, 45));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        iconLabel.setForeground(Color.WHITE);
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textLabel.setForeground(Color.WHITE);
        
        panel.add(iconLabel);
        panel.add(textLabel);
        
        navPanels.add(panel);
        navTextLabels.add(textLabel);
        navIconLabels.add(iconLabel);
        
        return panel;
    }
    
    private void createContentArea() {
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeIn));
                
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Initial view
        updateContentPanel();
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    // ============ HEADER METHODS ============
    
    private JPanel createPageHeader(String pageTitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel title = new JLabel(pageTitle);
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        
        // User panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser);
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeLabel.setForeground(TEXT_SECONDARY);
        
        avatarBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(SECONDARY_COLOR);
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String initial = currentUser.substring(0, 1).toUpperCase();
                int x = (getWidth() - fm.stringWidth(initial)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatarBtn.setPreferredSize(new Dimension(40, 40));
        avatarBtn.setOpaque(false);
        avatarBtn.setContentAreaFilled(false);
        avatarBtn.setBorderPainted(false);
        avatarBtn.setFocusPainted(false);
        avatarBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatarBtn.addActionListener(e -> toggleUserMenu());
        
        userPanel.add(welcomeLabel);
        userPanel.add(avatarBtn);
        
        header.add(title, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        
        return header;
    }
    
    // ============ DASHBOARD CHART METHODS ============
    
  private JPanel createChartPanel(String title, ChartPanel chartPanel, boolean isPieChart) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        container.setBackground(CARD_BACKGROUND);
        container.add(chartPanel, BorderLayout.CENTER);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(container, BorderLayout.CENTER);
        
        if (isPieChart) {
            JPanel legendPanel = createPieChartLegend();
            container.add(legendPanel, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    private JPanel createPieChartLegend() {
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(new EmptyBorder(10, 10, 0, 10));
        
        int total = pendingOrders + completedOrders + cancelledOrders;
        
        if (total > 0) {
            JPanel pendingPanel = createLegendItem(new Color(70, 130, 200), 
                "Pending " + pendingOrders + getPercentage(pendingOrders, total));
            legend.add(pendingPanel);
            legend.add(Box.createVerticalStrut(5));
            
            JPanel completedPanel = createLegendItem(new Color(60, 180, 100), 
                "Completed " + completedOrders + getPercentage(completedOrders, total));
            legend.add(completedPanel);
            legend.add(Box.createVerticalStrut(5));
            
            if (cancelledOrders > 0) {
                JPanel cancelledPanel = createLegendItem(new Color(220, 60, 60), 
                    "Cancelled " + cancelledOrders + getPercentage(cancelledOrders, total));
                legend.add(cancelledPanel);
            }
        } else {
            JLabel noData = new JLabel("No orders yet");
            noData.setFont(new Font("SansSerif", Font.ITALIC, 11));
            noData.setForeground(TEXT_SECONDARY);
            legend.add(noData);
        }
        
        return legend;
    }
    
    private String getPercentage(int value, int total) {
        if (total == 0) return " (0%)";
        double percent = (double) value / total * 100;
        return " (" + String.format("%.1f", percent) + "%)";
    }
    
    private JPanel createLegendItem(Color color, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        
        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setOpaque(false);
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(TEXT_SECONDARY);
        
        panel.add(colorBox);
        panel.add(label);
        
        return panel;
    }
    
    private JFreeChart createPieChart(DefaultPieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(
            null,
            dataset,
            false,
            true,
            false
        );
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_BACKGROUND);
        plot.setOutlinePaint(null);
        plot.setLabelGenerator(null);
        plot.setShadowPaint(null);
        
        plot.setSectionPaint("Pending", new Color(70, 130, 200));
        plot.setSectionPaint("Completed", new Color(60, 180, 100));
        plot.setSectionPaint("Cancelled", new Color(220, 60, 60));
        
        chart.setTitle((String) null);
        chart.setBackgroundPaint(CARD_BACKGROUND);
        
        return chart;
    }
    
    private JFreeChart createBarChart(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            null,
            "Units Sold",
            dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(CARD_BACKGROUND);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(true);
        renderer.setSeriesOutlinePaint(0, PRIMARY_COLOR);
        renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
        
        renderer.setSeriesPaint(0, new Color(90, 60, 40));
        renderer.setSeriesPaint(1, new Color(140, 100, 70));
        renderer.setSeriesPaint(2, new Color(100, 70, 50));
        renderer.setSeriesPaint(3, new Color(180, 140, 100));
        renderer.setSeriesPaint(4, new Color(60, 180, 100));
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.2);
        domainAxis.setTickLabelPaint(TEXT_SECONDARY);
        domainAxis.setAxisLinePaint(BORDER_COLOR);
        domainAxis.setTickMarkPaint(BORDER_COLOR);
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelPaint(TEXT_SECONDARY);
        rangeAxis.setAxisLinePaint(BORDER_COLOR);
        rangeAxis.setTickMarkPaint(BORDER_COLOR);
        
        chart.setTitle((String) null);
        chart.setBackgroundPaint(CARD_BACKGROUND);
        
        return chart;
    }
    
    private void updatePieChartData() {
        pieDataset.clear();
        
        if (pendingOrders + completedOrders + cancelledOrders > 0) {
            pieDataset.setValue("Pending", pendingOrders);
            pieDataset.setValue("Completed", completedOrders);
            if (cancelledOrders > 0) {
                pieDataset.setValue("Cancelled", cancelledOrders);
            }
        }
    }
    
    private void updateBarChartData() {
        barDataset.clear();
        
        for (int i = 0; i < popularItems.length; i++) {
            barDataset.addValue(itemSales[i], "Sales", popularItems[i]);
        }
    }
    
    // ============ USER MENU METHODS ============
    
    private void toggleUserMenu() {
        if (userPopupMenu == null) {
            userPopupMenu = createUserPopupMenu();
        }
        
        if (userPopupMenu.isVisible()) {
            userPopupMenu.setVisible(false);
        } else {
            userPopupMenu.show(avatarBtn, 0, avatarBtn.getHeight());
        }
    }
    
    private JPopupMenu createUserPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(CARD_BACKGROUND);
        menuPanel.setPreferredSize(new Dimension(250, 150));
        
        JLabel userNameLabel = new JLabel(currentUser);
        userNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        userNameLabel.setForeground(TEXT_PRIMARY);
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel roleLabel = new JLabel("Admin");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(TEXT_SECONDARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel emailLabel = new JLabel("Email: " + userEmail);
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        emailLabel.setForeground(TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel phoneLabel = new JLabel("Phone: 3222 999 553 888");
        phoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        phoneLabel.setForeground(TEXT_SECONDARY);
        phoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        menuPanel.add(userNameLabel);
        menuPanel.add(Box.createVerticalStrut(3));
        menuPanel.add(roleLabel);
        menuPanel.add(Box.createVerticalStrut(8));
        menuPanel.add(emailLabel);
        menuPanel.add(Box.createVerticalStrut(3));
        menuPanel.add(phoneLabel);
        menuPanel.add(Box.createVerticalStrut(15));
        
        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setForeground(BORDER_COLOR);
        menuPanel.add(separator);
        
        menuPanel.add(Box.createVerticalStrut(10));
        
        JButton signOutBtn = createBlackMenuButton("Sign Out", new Color(200, 60, 60));
        signOutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signOutBtn.addActionListener(e -> {
            userPopupMenu.setVisible(false);
            signOut();
        });
        
        menuPanel.add(signOutBtn);
        
        popup.add(menuPanel);
        
        return popup;
    }
    
    private JButton createBlackMenuButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            new EmptyBorder(8, 20, 8, 20)
        ));
        button.setPreferredSize(new Dimension(200, 35));
        button.setMaximumSize(new Dimension(200, 35));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2),
                    new EmptyBorder(8, 20, 8, 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    new EmptyBorder(8, 20, 8, 20)
                ));
            }
        });
        
        return button;
    }
    
    // ============ METRICS PANEL ============
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        totalSalesCard = new MetricCard("Total Sales", "$" + String.format("%.1f", totalSales), "💰");
        ordersCard = new MetricCard("Orders Status", totalOrders + " Orders", "📦");
        customersCard = new MetricCard("Customers", String.valueOf(totalCustomers), "👥");
        avgOrderCard = new MetricCard("Avg. Order Value", "$" + String.format("%.2f", avgOrderValue), "📊");
        
        panel.add(totalSalesCard);
        panel.add(ordersCard);
        panel.add(customersCard);
        panel.add(avgOrderCard);
        
        return panel;
    }
    
    // ============ RECENT ORDERS PANEL ============
    
    private JPanel createRecentOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setBackground(CARD_BACKGROUND);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel title = new JLabel("Recent Orders");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        
        JButton clearTableBtn = createBlackButton("Clear Table", new Color(220, 60, 60));
        clearTableBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        clearTableBtn.setPreferredSize(new Dimension(100, 30));
        clearTableBtn.addActionListener(e -> clearRecentOrders());
        
        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(clearTableBtn, BorderLayout.EAST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        DefaultTableModel tableModel = new DefaultTableModel(recentOrdersData, recentOrdersColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        recentOrdersTable = new JTable(tableModel);
        styleTable(recentOrdersTable);
        
        recentOrdersTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        recentOrdersTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        recentOrdersTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        recentOrdersTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        recentOrdersTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(recentOrdersTable);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD_BACKGROUND);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(240, 230, 220));
        table.setSelectionForeground(TEXT_PRIMARY);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setBackground(TABLE_HEADER_BG);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder());
                return this;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (row % 2 == 0) {
                    c.setBackground(CARD_BACKGROUND);
                } else {
                    c.setBackground(new Color(250, 248, 245));
                }
                
                ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                
                if (column == 4 && value != null) {
                    String status = value.toString();
                    if (status.equals("Completed")) {
                        c.setForeground(new Color(60, 180, 100));
                        ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.equals("Pending")) {
                        c.setForeground(new Color(70, 130, 200));
                        ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.equals("Cancelled")) {
                        c.setForeground(new Color(220, 60, 60));
                        ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
                    }
                } else {
                    c.setForeground(TEXT_PRIMARY);
                }
                
                setHorizontalAlignment(JLabel.CENTER);
                
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }
    
    // ============ ANIMATION AND UPDATE METHODS ============
    
    private void startFadeAnimation() {
        animationTimer = new javax.swing.Timer(16, e -> {
            fadeIn = Math.min(1f, fadeIn + 0.03f);
            repaint();
            if (fadeIn >= 1f) {
                animationTimer.stop();
            }
        });
        animationTimer.start();
    }
    
    // ============ ORDER MANAGEMENT METHODS ============
    
    public void addNewOrder(String orderId, String customer, String items, double total, String status) {
        totalSales += total;
        totalOrders++;
        totalCustomers = Math.max(totalCustomers, totalOrders);
        avgOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;
        
        switch (status) {
            case "Pending": pendingOrders++; break;
            case "Completed": completedOrders++; break;
            case "Cancelled": cancelledOrders++; break;
        }
        
        String[] itemParts = items.split(",");
        for (String part : itemParts) {
            part = part.trim();
            for (int i = 0; i < popularItems.length; i++) {
                if (part.contains(popularItems[i])) {
                    String qtyStr = part.split("x")[0].trim();
                    try {
                        int qty = Integer.parseInt(qtyStr);
                        itemSales[i] += qty;
                    } catch (NumberFormatException e) {
                        itemSales[i] += 1;
                    }
                    break;
                }
            }
        }
        
        Object[] newOrder = {orderId, customer, items, "$" + String.format("%.2f", total), status};

        // Maintain compact "recent orders" view (up to 5 rows)
        Object[][] newRecent;
        if (recentOrdersData.length < 5) {
            newRecent = new Object[recentOrdersData.length + 1][5];
            System.arraycopy(recentOrdersData, 0, newRecent, 0, recentOrdersData.length);
            newRecent[recentOrdersData.length] = newOrder;
        } else {
            newRecent = new Object[5][5];
            System.arraycopy(recentOrdersData, 1, newRecent, 0, 4);
            newRecent[4] = newOrder;
        }
        recentOrdersData = newRecent;

        // Maintain full order history for Orders view
        Object[][] newAll = new Object[allOrdersData.length + 1][5];
        System.arraycopy(allOrdersData, 0, newAll, 0, allOrdersData.length);
        newAll[allOrdersData.length] = newOrder;
        allOrdersData = newAll;
        
        updateDashboard();
    }
    
    public void simulateNewOrder() {
        String[] customers = {"Amanda Peterson", "Lisa Rodriguez", "John Smith", "Sarah Johnson", "Mike Davis"};
        
        String customer = customers[(int)(Math.random() * customers.length)];
        
        int numItems = 1 + (int)(Math.random() * 3);
        StringBuilder itemsStr = new StringBuilder();
        double total = 0;
        
        for (int i = 0; i < numItems; i++) {
            int itemIndex = (int)(Math.random() * 5);
            int quantity = 1 + (int)(Math.random() * 4);
            double price = itemPrices[itemIndex];
            
            if (i > 0) itemsStr.append(", ");
            itemsStr.append(quantity).append("x ").append(popularItems[itemIndex])
                   .append(" ($").append(String.format("%.2f", price)).append(")");
            
            total += quantity * price;
        }
        
        String status;
        double rand = Math.random();
        if (rand < 0.6) {
            status = "Completed";
        } else if (rand < 0.9) {
            status = "Pending";
        } else {
            status = "Cancelled";
        }
        
        addNewOrder("#" + (1000 + totalOrders + 1), customer, itemsStr.toString(), total, status);
        
        JOptionPane.showMessageDialog(this,
            "New Order Added!\n" +
            "Customer: " + customer + "\n" +
            "Total: $" + String.format("%.2f", total) + "\n" +
            "Status: " + status,
            "New Order",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearRecentOrders() {
        recentOrdersData = new Object[][]{};
        if (recentOrdersTable != null) {
            ((DefaultTableModel)recentOrdersTable.getModel()).setDataVector(recentOrdersData, recentOrdersColumns);
            styleTable(recentOrdersTable);
            JOptionPane.showMessageDialog(this,
                "Recent orders table cleared!",
                "Table Cleared",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateDashboard() {
        if (totalSalesCard != null) {
            totalSalesCard.updateValue("$" + String.format("%.1f", totalSales));
            ordersCard.updateValue(totalOrders + " Orders");
            customersCard.updateValue(String.valueOf(totalCustomers));
            avgOrderCard.updateValue("$" + String.format("%.2f", avgOrderValue));
        }
        
        updatePieChartData();
        updateBarChartData();
        
        if (pieChartPanel != null) {
            pieChartPanel.repaint();
        }
        if (barChartPanel != null) {
            barChartPanel.repaint();
        }
        
        if (recentOrdersTable != null) {
            ((DefaultTableModel)recentOrdersTable.getModel()).setDataVector(recentOrdersData, recentOrdersColumns);
            styleTable(recentOrdersTable);
        }
        
        revalidate();
        repaint();
    }
    
    public void resetData() {
        totalSales = 0.0;
        totalOrders = 0;
        totalCustomers = 0;
        avgOrderValue = 0.0;
        
        pendingOrders = 0;
        completedOrders = 0;
        cancelledOrders = 0;
        
        itemSales = new int[]{0, 0, 0, 0, 0};
        recentOrdersData = new Object[][]{};
        allOrdersData = new Object[][]{};
        
        updateDashboard();
        
        JOptionPane.showMessageDialog(this,
            "All data has been reset to zero!",
            "Data Reset",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void signOut() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to sign out?",
            "Sign Out",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
    
    // ============ INNER CLASSES ============
    
    class MetricCard extends JPanel {
        private JLabel valueLabel;
        
        MetricCard(String title, String value, String icon) {
            setOpaque(false);
            setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
            ));
            setBackground(CARD_BACKGROUND);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            topPanel.setOpaque(false);
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
            iconLabel.setForeground(SECONDARY_COLOR);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            titleLabel.setForeground(TEXT_SECONDARY);
            topPanel.add(iconLabel);
            topPanel.add(Box.createHorizontalStrut(10));
            topPanel.add(titleLabel);
            
            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Georgia", Font.BOLD, 24));
            valueLabel.setForeground(TEXT_PRIMARY);
            
            add(topPanel);
            add(Box.createVerticalStrut(10));
            add(valueLabel);
        }
        
        public void updateValue(String newValue) {
            valueLabel.setText(newValue);
            repaint();
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(250, 120);
        }
    }
    
    /**
     * MenuCard - improved mall-style menu item card.
     * - Rounded card with shadow
     * - Image area (vector placeholder if no image)
     * - Name, description (two lines), price chip
     * - Edit & Delete icon buttons that appear on hover
     * - Hover glow + animated border
     */
    class MenuCard extends JPanel {
        private final int CARD_W = 320;
        private final int CARD_H = 430;
        private final int IMAGE_H = 210;
        private final int ARC = 18;
        private MenuItem item;
        private boolean hovered = false;
        private float glow = 0f;
        private javax.swing.Timer animTimer;
        private JButton editBtn;
        private JButton deleteBtn;
        
        MenuCard(MenuItem it) {
            this.item = it;
            setOpaque(false);
            setLayout(null); // manual placement for overlay buttons
            setPreferredSize(new Dimension(CARD_W, CARD_H));
            
            // Edit button
            editBtn = iconButton("✏", new Color(70,130,200));
            deleteBtn = iconButton("🗑", new Color(220,60,60));
            editBtn.setVisible(false);
            deleteBtn.setVisible(false);
            add(editBtn);
            add(deleteBtn);
            
            editBtn.addActionListener(e -> editMenuItem(item));
            deleteBtn.addActionListener(e -> deleteMenuItem(item));
            
            // Hover detection
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    editBtn.setVisible(true);
                    deleteBtn.setVisible(true);
                    startAnim(true);
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    Point p = e.getPoint();
                    if (p.x < 0 || p.y < 0 || p.x > getWidth() || p.y > getHeight()) {
                        hovered = false;
                        // small delay to allow clicks on overlay buttons
                        javax.swing.Timer t = new javax.swing.Timer(200, ev -> {
                            if (!hovered) {
                                editBtn.setVisible(false);
                                deleteBtn.setVisible(false);
                            }
                        });
                        t.setRepeats(false);
                        t.start();
                        startAnim(false);
                        repaint();
                    }
                }
            });
            
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // place overlay buttons top-right
                    int bx = getWidth() - 48;
                    editBtn.setBounds(bx - 48, 14, 38, 38);
                    deleteBtn.setBounds(bx - 48 + 44, 14, 38, 38);
                }
            });
        }
        
        private JButton iconButton(String text, Color background) {
            JButton b = new JButton(text);
            b.setMargin(new Insets(0,0,0,0));
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setOpaque(false);
            b.setForeground(Color.WHITE);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setToolTipText(text);
            b.setContentAreaFilled(false);
            // custom painting
            b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = c.getWidth(), h = c.getHeight();
                    // shadow
                    g2.setColor(new Color(0,0,0,50));
                    g2.fillOval(1,2,w,h);
                    g2.setColor(background);
                    g2.fillOval(0,0,w-1,h-1);
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (w - fm.stringWidth(text)) / 2;
                    int ty = (h + fm.getAscent()) / 2 - 2;
                    g2.drawString(text, tx, ty);
                    g2.dispose();
                }
            });
            return b;
        }
        
        private void startAnim(boolean appear) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            animTimer = new javax.swing.Timer(16, null);
            animTimer.addActionListener(e -> {
                if (appear) {
                    glow = Math.min(1f, glow + 0.08f);
                } else {
                    glow = Math.max(0f, glow - 0.08f);
                }
                repaint();
                if ((!appear && glow <= 0f) || (appear && glow >= 1f)) {
                    animTimer.stop();
                }
            });
            animTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            
            // shadow
            g2.setColor(new Color(0,0,0,20));
            g2.fillRoundRect(6, 10, w-12, h-20, ARC, ARC);
            
            // card background
            GradientPaint gp = new GradientPaint(0,0,new Color(255,255,254), 0, h, new Color(250,247,244));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w-2, h-10, ARC, ARC);
            
            // border
            g2.setColor(new Color(210,200,190));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w-2, h-10, ARC, ARC);
            
            // glow/border animation
            if (glow > 0f) {
                int alpha = (int)(80 * glow);
                g2.setColor(new Color(100,140,200, alpha));
                g2.setStroke(new BasicStroke(6f * glow));
                g2.drawRoundRect(4, 4, w-10, h-18, ARC+4, ARC+4);
            }
            
            // image clip area
            int imgX = 16;
            int imgY = 20;
            int imgW = w - 32;
            RoundRectangle2D imgClip = new RoundRectangle2D.Float(imgX, imgY, imgW, IMAGE_H, 12, 12);
            g2.setClip(imgClip);
            // light background behind image
            g2.setColor(new Color(235, 230, 225));
            g2.fillRect(imgX, imgY, imgW, IMAGE_H);

            Image thumb = item.getThumbnail();
            if (thumb != null) {
                int tw = thumb.getWidth(null);
                int th = thumb.getHeight(null);
                int tx = imgX + (imgW - tw) / 2;
                int ty = imgY + (IMAGE_H - th) / 2;
                g2.drawImage(thumb, tx, ty, null);
            } else {
                // Fallback to vector coffee illustration
                GradientPaint imgGp = new GradientPaint(
                        imgX, imgY, new Color(222,200,180),
                        imgX, imgY+IMAGE_H, new Color(200,170,140));
                g2.setPaint(imgGp);
                g2.fillRect(imgX, imgY, imgW, IMAGE_H);
                drawCoffeePlaceholder(g2, imgX, imgY, imgW, IMAGE_H);
            }
            g2.setClip(null);
            
            // Price chip
            String price = "$" + new DecimalFormat("#0.00").format(item.getPrice());
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int chipW = fm.stringWidth(price) + 18;
            int chipH = 32;
            int chipX = imgX + imgW - chipW - 12;
            int chipY = imgY + IMAGE_H - (chipH/2);
            // chip background with slight shadow
            g2.setColor(new Color(255,250,245));
            g2.fillRoundRect(chipX, chipY, chipW, chipH, 18, 18);
            g2.setColor(new Color(100,70,50));
            g2.drawString(price, chipX + 10, chipY + 20);
            
            // Title
            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Georgia", Font.BOLD, 20));
            fm = g2.getFontMetrics();
            int titleY = imgY + IMAGE_H + 36;
            g2.drawString(item.getName(), imgX + 8, titleY);
            
            // Description (up to 2 lines, lighter font)
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(TEXT_SECONDARY);
            int descY = titleY + 22;
            drawWrappedString(g2, item.getDescription(), imgX + 8, descY, imgW - 16, 16);
            
            // subtle divider
            g2.setColor(new Color(235,230,225));
            g2.fillRect(imgX + 8, descY + 36, imgW - 16, 1);
            
            g2.dispose();
        }
        
        private void drawCoffeePlaceholder(Graphics2D g2, int x, int y, int w, int h) {
            int cx = x + w/2;
            int cy = y + h/2 + 14;
            // saucer
            g2.setColor(new Color(210,190,170));
            g2.fillOval(cx - 60, cy + 40, 120, 18);
            // cup
            g2.setColor(new Color(240,235,230));
            g2.fillRoundRect(cx - 48, cy - 8, 96, 64, 16, 16);
            // coffee
            g2.setColor(new Color(110,70,40));
            g2.fillOval(cx - 36, cy, 72, 22);
            // handle
            g2.setColor(new Color(240,235,230));
            g2.fillOval(cx + 34, cy + 6, 22, 30);
        }
        
        private void drawWrappedString(Graphics2D g2, String text, int x, int y, int wrapWidth, int lineHeight) {
            if (text == null) return;
            FontMetrics fm = g2.getFontMetrics();
            String[] words = text.split("\\s+");
            StringBuilder line = new StringBuilder();
            int lines = 0;
            for (int i = 0; i < words.length; i++) {
                String test = line.length() == 0 ? words[i] : line + " " + words[i];
                if (fm.stringWidth(test) > wrapWidth) {
                    g2.drawString(line.toString(), x, y + (lines * lineHeight));
                    lines++;
                    line = new StringBuilder(words[i]);
                    if (lines >= 2) break; // limit to 2 lines in card
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (lines < 2 && line.length() > 0) {
                if (lines == 1 && fm.stringWidth(line.toString()) > wrapWidth) {
                    String s = trimToWidth(line.toString(), fm, wrapWidth);
                    g2.drawString(s, x, y + (lines * lineHeight));
                } else {
                    g2.drawString(line.toString(), x, y + (lines * lineHeight));
                }
            }
        }
        
        private String trimToWidth(String s, FontMetrics fm, int maxW) {
            String ell = "...";
            if (fm.stringWidth(s) <= maxW) return s;
            int cut = s.length();
            while (cut > 0 && fm.stringWidth(s.substring(0, cut) + ell) > maxW) cut--;
            return s.substring(0, cut) + ell;
        }
    }
    
    class MenuItem {
        private String name;
        private double price;
        private String description;
        private String imagePath;
        // cached, already-resized thumbnail for performant painting
        private Image thumbnail;
        
        public MenuItem(String name, double price, String description, String imagePath) {
            this.name = name;
            this.price = price;
            this.description = description;
            setImagePath(imagePath);
        }
        
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getDescription() { return description; }
        public String getImagePath() { return imagePath; }
        public Image getThumbnail() { return thumbnail; }
        
        public void setName(String name) { this.name = name; }
        public void setPrice(double price) { this.price = price; }
        public void setDescription(String description) { this.description = description; }
        
        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
            this.thumbnail = null;
            if (imagePath == null || imagePath.isEmpty()) {
                return;
            }
            try {
                BufferedImage original = ImageIO.read(new File(imagePath));
                if (original == null) {
                    return;
                }
                // Target area roughly matches MenuCard IMAGE_H and width minus padding
                int targetW = 220;
                int targetH = 150;
                double scale = Math.min(
                        (double) targetW / original.getWidth(),
                        (double) targetH / original.getHeight());
                scale = Math.min(scale, 1.0); // never upscale
                
                int newW = (int) (original.getWidth() * scale);
                int newH = (int) (original.getHeight() * scale);
                
                Image scaled = original.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                BufferedImage thumbImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = thumbImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(scaled, 0, 0, null);
                g2.dispose();
                this.thumbnail = thumbImg;
            } catch (IOException ex) {
                // Log to console; keep UI responsive even if image fails
                ex.printStackTrace();
            }
        }
    }
    
    // ============ MAIN METHOD ============
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame();
            frame.setVisible(true);
        });
    }
}