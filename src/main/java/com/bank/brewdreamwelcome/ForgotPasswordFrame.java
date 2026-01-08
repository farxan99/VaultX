package com.bank.brewdreamwelcome;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Pattern;

/**
 * VaultX Bank – Forgot Password Page
 * Theme-matched with Login & Signup screens
 */
public class ForgotPasswordFrame extends JFrame {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private JTextField emailField;
    private JLabel errorLabel, successLabel;
    private JLabel captchaTextLabel;
    private JTextField captchaInputField;
    private String captchaCode;
    private float fadeIn = 0f;
    private Timer animationTimer;

    public ForgotPasswordFrame() {
        super("VaultX Bank – Reset Password");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        /* ================= BACKGROUND ================= */
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeIn));

                // VaultX background image
                VaultXTheme.paintBackgroundImage(g2, this);

                // Dark red/blue overlay (lighter for more image visibility)
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0, 0, 0, 170),
                        0, getHeight(), new Color(15, 23, 42, 215)
                );
                g2.setPaint(gp);
              //  g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bg.setLayout(new GridBagLayout());

        /* ================= RESET CARD ================= */
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            /*    g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRoundRect(8, 10, getWidth() - 16, getHeight() - 16, 40, 40);

                g2.setColor(new Color(15, 23, 42, 215));
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 40, 40);
              */  
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 460));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 45, 40, 45));

        /* ================= LOGO PANEL ================= */
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawCoffeeCupIcon(g2, getWidth() / 2, 10);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(420, 60));
        logoPanel.setMaximumSize(new Dimension(420, 60));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ================= TITLE ================= */
        JLabel title = new JLabel("VaultX Bank");
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(VaultXTheme.PRIMARY_RED);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Reset your VaultX password");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 16));
        subtitle.setForeground(new Color(191, 219, 254));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel instruction = new JLabel("<html><center>Enter your email address and we'll<br>send you a reset link</center></html>");
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruction.setForeground(new Color(226, 232, 240));
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ================= INPUT ================= */
        emailField = createField("Email Address");
        
        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);
        
        successLabel = new JLabel("");
        successLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        successLabel.setForeground(new Color(74, 222, 128));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successLabel.setVisible(false);

        /* ================= CAPTCHA ================= */
        JPanel captchaPanel = new JPanel();
        captchaPanel.setOpaque(false);
        captchaPanel.setLayout(new BoxLayout(captchaPanel, BoxLayout.Y_AXIS));
        captchaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel captchaTitle = new JLabel("Security check");
        captchaTitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        captchaTitle.setForeground(new Color(191, 219, 254));
        captchaTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel captchaRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        captchaRow.setOpaque(false);

        captchaTextLabel = new JLabel("------");
        captchaTextLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        captchaTextLabel.setForeground(new Color(248, 250, 252));
        captchaTextLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(148, 163, 184), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        captchaInputField = new JTextField();
        captchaInputField.setColumns(8);
        captchaInputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        captchaInputField.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JButton refreshCaptchaBtn = new JButton("↻");
        refreshCaptchaBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        refreshCaptchaBtn.setFocusPainted(false);
        refreshCaptchaBtn.setBorderPainted(false);
        refreshCaptchaBtn.setContentAreaFilled(false);
        refreshCaptchaBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshCaptchaBtn.setToolTipText("Refresh captcha");
        refreshCaptchaBtn.addActionListener(e -> generateCaptcha());

        captchaRow.add(captchaTextLabel);
        captchaRow.add(captchaInputField);
        captchaRow.add(refreshCaptchaBtn);

        captchaPanel.add(captchaTitle);
        captchaPanel.add(Box.createVerticalStrut(6));
        captchaPanel.add(captchaRow);

        /* ================= BUTTONS ================= */
        JButton resetBtn = createButton("Send Reset Link →");
        resetBtn.addActionListener(e -> handleReset());

        JLabel loginLink = new JLabel("Back to Login");
        loginLink.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginLink.setForeground(new Color(96, 165, 250));
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                loginLink.setForeground(new Color(129, 140, 248));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                loginLink.setForeground(new Color(96, 165, 250));
            }
        });

        /* ================= ADD ================= */
        card.add(logoPanel);
        card.add(Box.createVerticalStrut(5));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(12));
        card.add(instruction);
        card.add(Box.createVerticalStrut(25));
        card.add(emailField);
        card.add(Box.createVerticalStrut(12));
        card.add(captchaPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(successLabel);
        card.add(Box.createVerticalStrut(22));
        card.add(resetBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(loginLink);

        // Position card towards the right (~3/4 width) to reveal left background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 60);
        bg.add(card, gbc);
        setContentPane(bg);
        
        startFadeAnimation();
        generateCaptcha();
    }
    
    private void handleReset() {
        String email = emailField.getText().trim();
        String captchaInput = captchaInputField.getText().trim();
        
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        errorLabel.setText("");
        successLabel.setText("");
        
        // Validation
        if (email.isEmpty() || email.equals("Email Address")) {
            showError("Email is required");
            return;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format");
            return;
        }

        if (captchaInput.isEmpty()) {
            showError("Please complete the captcha");
            return;
        }
        // Captcha is now case-sensitive: must match exactly
        if (captchaCode == null || !captchaInput.equals(captchaCode)) {
            showError("Incorrect captcha (case-sensitive). Please try again.");
            generateCaptcha();
            captchaInputField.setText("");
            return;
        }
        
        // Success
        showSuccess("Reset link sent to your email!");
        emailField.setText("Email Address");
        emailField.setForeground(Color.GRAY);
        
        // Auto-close after 3 seconds
        Timer timer = new Timer(3000, e -> {
            dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            ((Timer) e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
    
    private void startFadeAnimation() {
        animationTimer = new Timer(16, e -> {
            // Faster fade-in to match Login & Signup responsiveness
            fadeIn = Math.min(1f, fadeIn + 0.06f);
            repaint();
            if (fadeIn >= 1f) {
                animationTimer.stop();
            }
        });
        animationTimer.start();
    }
    
    private void drawCoffeeCupIcon(Graphics2D g2, int centerX, int y) {
        // VaultX circular logo updated to red/black/blue theme
        int size = 54;
        int x = centerX - size / 2;
        int baseY = y + 4;

        g2.setColor(VaultXTheme.PRIMARY_RED);
        g2.fillOval(x, baseY, size, size);

        g2.setColor(VaultXTheme.DARK_BG_ALT);
        g2.fillOval(x + 5, baseY + 5, size - 10, size - 10);

        int colWidth = 6;
        int colHeight = 20;
        int colBase = baseY + size - 14;
        g2.setColor(VaultXTheme.TEXT_LIGHT);
        for (int i = 0; i < 3; i++) {
            int cx = x + 12 + i * (colWidth + 6);
            g2.fillRoundRect(cx, colBase - colHeight, colWidth, colHeight, 4, 4);
        }

        Polygon roof = new Polygon();
        roof.addPoint(x + 12, colBase - colHeight - 4);
        roof.addPoint(x + size / 2, baseY + 9);
        roof.addPoint(x + size - 12, colBase - colHeight - 4);
        g2.fillPolygon(roof);
    }

    private JTextField createField(String placeholder) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setBackground(new Color(15, 23, 42));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(148, 163, 184), 2, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        field.setForeground(new Color(226, 232, 240));
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(248, 250, 252));
                }
                field.setBackground(new Color(15, 23, 42));
                field.setBorder(new CompoundBorder(
                        new LineBorder(VaultXTheme.PRIMARY_RED, 2, true),
                        new EmptyBorder(12, 18, 12, 18)
                ));
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(148, 163, 184));
                }
                field.setBorder(new CompoundBorder(
                        new LineBorder(new Color(148, 163, 184), 2, true),
                        new EmptyBorder(12, 18, 12, 18)
                ));
            }
        });
        return field;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ButtonModel model = getModel();
                Color color1;
                Color color2;
                
                if (model.isPressed()) {
                    color1 = VaultXTheme.ACCENT_RED_DARK;
                    color2 = new Color(15, 23, 42);
                } else if (model.isRollover()) {
                    color1 = new Color(248, 113, 113);
                    color2 = new Color(59, 130, 246);
                } else {
                    color1 = VaultXTheme.PRIMARY_RED;
                    color2 = VaultXTheme.PRIMARY_BLUE;
                }
                
                GradientPaint gp = new GradientPaint(
                        0, 0, color1,
                        0, getHeight(), color2
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;

                g2.setColor(Color.WHITE);
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(260, 55));
        btn.setMaximumSize(new Dimension(260, 55));
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void generateCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        captchaCode = sb.toString();
        if (captchaTextLabel != null) {
            captchaTextLabel.setText(captchaCode);
        }
    }
}

