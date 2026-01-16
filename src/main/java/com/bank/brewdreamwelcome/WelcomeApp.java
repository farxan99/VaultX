package com.bank.brewdreamwelcome;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;


public class WelcomeApp { 

    public static void main(String[] args) {
        // Initialize Core Infrastructure
        com.bank.brewdreamwelcome.core.ThemeManager.initialize();
        // Firebase disabled until serviceAccountKey.json is added to src/main/resources/
        // com.bank.brewdreamwelcome.config.FirebaseConfig.initialize();

        SwingUtilities.invokeLater(() -> {
            SplashScreenWindow splash = new SplashScreenWindow();
            splash.showSplash(() -> {
                // Determine if we should show Login or Dashboard
                // In production, always start with Login
                // Start with Modern Login
                com.bank.brewdreamwelcome.ui.views.ModernLoginView login = new com.bank.brewdreamwelcome.ui.views.ModernLoginView();
                login.setVisible(true);
            });
        });
    }
}


class SplashScreenWindow extends JWindow {
    private final JProgressBar progressBar;
    private Timer progressTimer;
    private float fadeIn = 0f;
    private Runnable onCompleteCallback;

    SplashScreenWindow() {
        setSize(900, 550);
        setLocationRelativeTo(null);

        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                VaultXTheme.paintBackgroundImage(g2, this);

                GradientPaint overlay = new GradientPaint(
                        0, 0, new Color(0, 0, 0, 170),
                        0, getHeight(), new Color(15, 23, 42, 215));
                g2.setPaint(overlay);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(getWidth(), 100));

        JLabel titleLabel = new JLabel("VaultX Bank");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 42));
        titleLabel.setForeground(new Color(248, 250, 252));

        JLabel subtitleLabel = new JLabel("Secure. Smart. Modern Banking.");
        subtitleLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
        subtitleLabel.setForeground(new Color(191, 219, 254));

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(10));
        header.add(subtitleLabel);

        JPanel center = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeIn));

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                drawStaticVaultXLogo(g2, centerX, centerY);

                g2.dispose();
            }
        };
        center.setOpaque(false);

        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setOpaque(false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(59, 130, 246));
        progressBar.setBackground(new Color(255, 255, 255, 30));
        progressBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        JLabel loadingLabel = new JLabel("Preparing your secure vault...");
        loadingLabel.setForeground(new Color(219, 234, 254));
        loadingLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressPanel.add(loadingLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(progressPanel, BorderLayout.SOUTH);

        setContentPane(root);

        startAnimations();
    }

    private void startAnimations() {
        progressTimer = new Timer(30, e -> {
            fadeIn = Math.min(1f, fadeIn + 0.05f);
            repaint();

            int value = progressBar.getValue() + 3;
            progressBar.setValue(value);
            progressBar.setString(String.format("%d%%", value));

            if (value >= 100) {
                progressTimer.stop();

                Timer delayTimer = new Timer(250, ev -> {
                    ((Timer) ev.getSource()).stop();
                    dispose();
                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });
        progressTimer.start();
    }

    private void drawStaticVaultXLogo(Graphics2D g2, int centerX, int centerY) {
        int size = 160;
        int x = centerX - size / 2;
        int y = centerY - size / 2;

        g2.setColor(VaultXTheme.PRIMARY_RED);
        g2.fillOval(x, y, size, size);

        g2.setColor(new Color(59, 130, 246, 140));
        g2.setStroke(new BasicStroke(6f));
        g2.drawOval(x + 4, y + 4, size - 8, size - 8);

        g2.setColor(VaultXTheme.DARK_BG_ALT);
        g2.fillOval(x + 10, y + 10, size - 20, size - 20);

        int colWidth = 12;
        int colHeight = 46;
        int colBase = y + size - 40;
        g2.setColor(VaultXTheme.TEXT_LIGHT);
        for (int i = 0; i < 3; i++) {
            int cx = x + 36 + i * (colWidth + 16);
            g2.fillRoundRect(cx, colBase - colHeight, colWidth, colHeight, 10, 10);
        }

        Polygon roof = new Polygon();
        roof.addPoint(x + 34, colBase - colHeight - 10);
        roof.addPoint(x + size / 2, y + 26);
        roof.addPoint(x + size - 34, colBase - colHeight - 10);
        g2.fillPolygon(roof);
    }

    public void showSplash(Runnable onDone) {
        this.onCompleteCallback = onDone;
        setVisible(true);
    }
}

class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        super("VaultX Bank - Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                VaultXTheme.paintBackgroundImage(g2, this);

                GradientPaint overlay = new GradientPaint(
                        0, 0, new Color(0, 0, 0, 160),
                        0, getHeight(), new Color(15, 23, 42, 215));
                g2.setPaint(overlay);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bg.setLayout(new GridBagLayout());

        JPanel contentPanel = new JPanel() {
            {
                setOpaque(false);
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcomeLabel = new JLabel("Welcome to");
        welcomeLabel.setFont(new Font("Georgia", Font.BOLD, 48));
        welcomeLabel.setForeground(VaultXTheme.TEXT_LIGHT);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandLabel = new JLabel("VaultX Bank");
        brandLabel.setFont(new Font("Georgia", Font.BOLD, 62));
        brandLabel.setForeground(VaultXTheme.PRIMARY_RED);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("Smart, fast and beautiful banking");
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 24));
        taglineLabel.setForeground(new Color(191, 219, 254));
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea description = new JTextArea(
                "Manage customer accounts, balances and transactions with a modern bank dashboard. " +
                        "Track deposits, withdrawals and transfers in real time, with smooth animations and a clean UI.");
        description.setFont(new Font("SansSerif", Font.PLAIN, 18));
        description.setForeground(new Color(226, 232, 240));
        description.setOpaque(false);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setMaximumSize(new Dimension(600, 100));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        description.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel buttonPanel = new JPanel() {
            {
                setOpaque(false);
            }
        };
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));

        JButton continueButton = createStyledButton("Continue to Bank Dashboard →",
                VaultXTheme.PRIMARY_RED, VaultXTheme.PRIMARY_BLUE, 320, 55); // Wider button

        continueButton.addActionListener(e -> {
            this.dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

        JButton exitButton = createStyledButton("Exit",
                new Color(15, 23, 42), new Color(30, 64, 175), 180, 55); // Slightly wider, dark theme

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?",
                    "Exit VaultX Bank",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        buttonPanel.add(continueButton);
        buttonPanel.add(exitButton);

        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(welcomeLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(brandLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(taglineLabel);
        contentPanel.add(Box.createVerticalStrut(40));
        contentPanel.add(description);
        contentPanel.add(Box.createVerticalStrut(50));
        contentPanel.add(buttonPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 80);
        bg.add(contentPanel, gbc);
        setContentPane(bg);
    }

    private JButton createStyledButton(String text, Color color1, Color color2, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ButtonModel model = getModel();
                int btnWidth = getWidth();
                int btnHeight = getHeight();

                Color bgColor1, bgColor2;
                if (model.isPressed()) {
                    bgColor1 = color1.darker();
                    bgColor2 = color2.darker();
                } else if (model.isRollover()) {
                    bgColor1 = color1.brighter();
                    bgColor2 = color2.brighter();
                } else {
                    bgColor1 = color1;
                    bgColor2 = color2;
                }

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(3, 4, btnWidth - 6, btnHeight - 6, btnHeight, btnHeight);

                GradientPaint gradient = new GradientPaint(
                        0, 0, bgColor1,
                        0, btnHeight, bgColor2);
                g2.setPaint(gradient);
                g2.fillRoundRect(2, 2, btnWidth - 4, btnHeight - 4, btnHeight, btnHeight);

                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(2, 2, btnWidth - 4, btnHeight / 3, btnHeight, btnHeight);

                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(2, 2, btnWidth - 4, btnHeight - 4, btnHeight, btnHeight);

                FontMetrics fm = g2.getFontMetrics(getFont());
                String buttonText = getText();
                int textWidth = fm.stringWidth(buttonText);

                int textX = (btnWidth - textWidth) / 2;
                int textY = (btnHeight + fm.getAscent()) / 2 - 2;

                g2.setColor(new Color(0, 0, 0, 40));
                g2.drawString(buttonText, textX + 1, textY + 1);

                g2.setColor(Color.WHITE);
                g2.drawString(buttonText, textX, textY);

                if (buttonText.contains("→")) {
                    g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int arrowX = textX + textWidth + 10; // Position arrow after text
                    int arrowY = btnHeight / 2;
                    g2.drawLine(arrowX, arrowY, arrowX + 8, arrowY);
                    g2.drawLine(arrowX + 6, arrowY - 4, arrowX + 8, arrowY);
                    g2.drawLine(arrowX + 6, arrowY + 4, arrowX + 8, arrowY);
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                int textWidth = fm.stringWidth(getText());
                int arrowSpace = getText().contains("→") ? 30 : 0; 
                int buttonWidth = Math.max(width, textWidth + 60 + arrowSpace); 
                return new Dimension(buttonWidth, height);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };

        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        int padding = 30;
        button.setBorder(new EmptyBorder(15, padding, 15, padding));

        return button;
    }
}