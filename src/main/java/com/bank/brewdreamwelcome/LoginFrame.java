package com.bank.brewdreamwelcome;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * VaultX Bank – Login Page
 * Theme-matched with Welcome & Splash screen.
 *
 * Uses MySQL (XAMPP) via {@link AuthService}:
 * - Admin credentials from table 'admins'
 * - Customer credentials from table 'customers'
 *
 * GUI layout stays the same; only the backend logic is database-backed now.
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JLabel captchaTextLabel;
    private JTextField captchaInputField;
    private String captchaCode;
    private float fadeIn = 0f;
    private Timer animationTimer;

    public LoginFrame() {
        super("VaultX Bank – Admin Login");
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

                // Dark red/blue overlay (slightly lighter to reveal more of the image)
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0, 0, 0, 170),
                        0, getHeight(), new Color(15, 23, 42, 215));
                g2.setPaint(gp);
                // g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        bg.setLayout(new GridBagLayout());

        /* ================= LOGIN CARD ================= */
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 520));
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

        JLabel subtitle = new JLabel("Secure banking control for admins");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 16));
        subtitle.setForeground(new Color(191, 219, 254));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ================= INPUTS ================= */
        emailField = createField("Username / Email / Account ID");
        JPanel passwordPanel = createPasswordFieldWithToggle("Password");
        passwordField = (JPasswordField) passwordPanel.getComponent(0);

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);

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
        captchaTextLabel.setFont(new Font("Consolas", Font.BOLD, 22));
        captchaTextLabel.setForeground(new Color(248, 250, 252)); // bright text
        captchaTextLabel.setOpaque(true);
        captchaTextLabel.setBackground(new Color(15, 23, 42)); // dark pill background
        captchaTextLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(148, 163, 184), 1, true),
                new EmptyBorder(6, 12, 6, 12)));

        captchaInputField = new JTextField();
        captchaInputField.setColumns(8);
        captchaInputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        captchaInputField.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(6, 10, 6, 10)));

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
        JButton loginBtn = createButton("Login →");
        loginBtn.addActionListener(e -> handleLogin());

        JLabel signupLink = new JLabel("Don't have an account? Sign Up");
        signupLink.setFont(new Font("SansSerif", Font.PLAIN, 14));
        signupLink.setForeground(new Color(96, 165, 250));
        signupLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                SignupFrame signupFrame = new SignupFrame();
                signupFrame.setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                signupLink.setForeground(new Color(129, 140, 248));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signupLink.setForeground(new Color(96, 165, 250));
            }
        });

        JLabel forgot = new JLabel("Forgot password?");
        forgot.setFont(new Font("SansSerif", Font.PLAIN, 14));
        forgot.setForeground(new Color(96, 165, 250));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                ForgotPasswordFrame forgotFrame = new ForgotPasswordFrame();
                forgotFrame.setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                forgot.setForeground(new Color(129, 140, 248));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgot.setForeground(new Color(96, 165, 250));
            }
        });

        /* ================= ADD ================= */
        card.add(logoPanel);
        card.add(Box.createVerticalStrut(5));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));
        card.add(emailField);
        card.add(Box.createVerticalStrut(20));
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(captchaPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(signupLink);
        card.add(Box.createVerticalStrut(8));
        card.add(forgot);

        // Position card towards the right (~3/4 width) so left background art is
        // visible
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 60); // gap from right edge
        bg.add(card, gbc);
        setContentPane(bg);

        // Start fade-in animation and initial captcha
        startFadeAnimation();
        generateCaptcha();
    }

    private void handleLogin() {
        String username = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String captchaInput = captchaInputField.getText().trim();

        errorLabel.setVisible(false);
        errorLabel.setText("");

        // Basic presence validation
        if (username.isEmpty() || username.equals("Username / Email / Account ID")) {
            showError("Username, email, or account ID is required");
            return;
        }

        if (password.isEmpty() || String.valueOf(passwordField.getPassword()).equals("Password")) {
            showError("Password is required");
            return;
        }

        if (captchaInput.isEmpty()) {
            showError("Please enter the captcha code.");
            return;
        }

        // Captcha is now case-sensitive: must match exactly
        if (captchaCode == null || !captchaInput.equals(captchaCode)) {
            showError("Incorrect captcha (case-sensitive). Please try again.");
            generateCaptcha();
            captchaInputField.setText("");
            return;
        }

        // Database-backed authentication (admin or customer)
        AuthService.AuthResult auth = AuthService.authenticate(username, password);
        if (auth == null) {
            showError("Invalid credentials. Please check username/email and password.");
            generateCaptcha();
            captchaInputField.setText("");
            return;
        }

        // Success - Navigate to appropriate dashboard based on role
        dispose();
        if (auth.role == AuthService.Role.ADMIN) {
            AdminDashboardFrame adminDashboard = new AdminDashboardFrame(username);
            adminDashboard.setVisible(true);
        } else {
            // Customer dashboard - pass customer ID
            CustomerDashboardFrame customerDashboard = new CustomerDashboardFrame(auth.customerId);
            customerDashboard.setVisible(true);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void startFadeAnimation() {
        animationTimer = new Timer(16, e -> {
            // Faster fade-in so the screen appears more quickly
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

        // Outer red circle
        g2.setColor(VaultXTheme.PRIMARY_RED);
        g2.fillOval(x, baseY, size, size);

        // Inner near-black circle
        g2.setColor(VaultXTheme.DARK_BG_ALT);
        g2.fillOval(x + 5, baseY + 5, size - 10, size - 10);

        // Bank columns
        int colWidth = 6;
        int colHeight = 20;
        int colBase = baseY + size - 14;
        g2.setColor(VaultXTheme.TEXT_LIGHT);
        for (int i = 0; i < 3; i++) {
            int cx = x + 12 + i * (colWidth + 6);
            g2.fillRoundRect(cx, colBase - colHeight, colWidth, colHeight, 4, 4);
        }

        // Roof triangle
        Polygon roof = new Polygon();
        roof.addPoint(x + 12, colBase - colHeight - 4);
        roof.addPoint(x + size / 2, baseY + 9);
        roof.addPoint(x + size - 12, colBase - colHeight - 4);
        g2.fillPolygon(roof);
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

    /* ================= FIELD ================= */
    private JTextField createField(String placeholder) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setBackground(new Color(15, 23, 42));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(148, 163, 184), 2, true),
                new EmptyBorder(12, 18, 12, 18)));
        field.setForeground(new Color(226, 232, 240));
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(248, 250, 252));
                }
                field.setBackground(new Color(15, 23, 42));
                // Animated glow border on focus
                field.setBorder(new CompoundBorder(
                        new LineBorder(new Color(220, 38, 38), 2),
                        new EmptyBorder(12, 18, 12, 18)));
            }

            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(148, 163, 184));
                }
                field.setBorder(new CompoundBorder(
                        new LineBorder(new Color(148, 163, 184), 2, true),
                        new EmptyBorder(12, 18, 12, 18)));
            }
        });
        return field;
    }

    private JPanel createPasswordFieldWithToggle(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setBackground(new Color(15, 23, 42));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(148, 163, 184), 2, true),
                new EmptyBorder(12, 18, 12, 18)));
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(new Color(226, 232, 240));

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(new Color(248, 250, 252));
                }
                field.setBackground(new Color(15, 23, 42));
                field.setBorder(new CompoundBorder(
                        new LineBorder(VaultXTheme.PRIMARY_RED, 2, true),
                        new EmptyBorder(12, 18, 12, 18)));
            }

            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(new Color(148, 163, 184));
                }
                field.setBorder(new CompoundBorder(
                        new LineBorder(new Color(148, 163, 184), 2, true),
                        new EmptyBorder(12, 18, 12, 18)));
            }
        });

        // Create show/hide password button
        JButton toggleButton = new JButton("👁");
        toggleButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        toggleButton.setPreferredSize(new Dimension(40, 40));
        toggleButton.setMaximumSize(new Dimension(40, 40));
        toggleButton.setMinimumSize(new Dimension(40, 40));
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setForeground(new Color(148, 163, 184));
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleButton.setToolTipText("Show password");

        // Add hover effect
        toggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                toggleButton.setForeground(new Color(191, 219, 254));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                toggleButton.setForeground(new Color(148, 163, 184));
            }
        });

        // Wrap password field and button in a panel
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(toggleButton, BorderLayout.EAST);

        // Toggle password visibility
        toggleButton.addActionListener(e -> {
            String currentText = String.valueOf(field.getPassword());
            // Don't toggle if placeholder is showing
            if (currentText.equals(placeholder)) {
                return;
            }

            if (field.getEchoChar() == '•') {
                // Currently hidden - show password
                field.setEchoChar((char) 0);
                toggleButton.setText("🙈");
                toggleButton.setToolTipText("Hide password");
            } else {
                // Currently visible - hide password
                field.setEchoChar('•');
                toggleButton.setText("👁");
                toggleButton.setToolTipText("Show password");
            }
        });

        return wrapper;
    }

    /* ================= BUTTON ================= */
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
                        0, getHeight(), color2);
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
}
