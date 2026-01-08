package com.bank.brewdreamwelcome;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Pattern;

/**
 * VaultX Bank – Signup Page
 * Theme-matched with Login & Welcome screens
 */
public class SignupFrame extends JFrame {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private JTextField nameField, emailField, idCardField, usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel errorLabel;
    private JLabel captchaTextLabel;
    private JTextField captchaInputField;
    private String captchaCode;
    private float fadeIn = 0f;
    private Timer animationTimer;
    private ButtonGroup accountTypeGroup;
    private JRadioButton savingsRadio, currentRadio, fixedDepositRadio;

    public SignupFrame() {
        super("VaultX Bank – Sign Up");
        setSize(900, 650);
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
                        0, getHeight(), new Color(15, 23, 42, 215));
                g2.setPaint(gp);
                // g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bg.setLayout(new BorderLayout());

        /* ================= SIGNUP CARD ================= */
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(420, d.height);
            }
        };
        card.setOpaque(false);
        // Removed fixed size to allow scrolling, but we need fixed WIDTH
        // card.setPreferredSize(new Dimension(420, 590));
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
        JLabel title = new JLabel(
                "VaultX Bank");
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(VaultXTheme.PRIMARY_RED);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Create your VaultX profile");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 16));
        subtitle.setForeground(new Color(191, 219, 254));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ================= INPUTS ================= */
        nameField =

                createField("Full Name");
        emailField = createField("Email Address");
        idCardField = createField("ID Card Number");
        usernameField = createField("Username (optional)");

        // Account Type Selection
        JPanel accountTypePanel = new JPanel();
        accountTypePanel.setOpaque(false);
        accountTypePanel.setLayout(new BoxLayout(accountTypePanel, BoxLayout.Y_AXIS));
        accountTypePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        accountTypePanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel accountTypeLabel = new JLabel("Account Type:");
        accountTypeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        accountTypeLabel.setForeground(new Color(191, 219, 254));
        accountTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        accountTypeGroup = new ButtonGroup();
        savingsRadio = new JRadioButton("Savings Account", true);
        currentRadio = new JRadioButton("Current Account");
        fixedDepositRadio = new JRadioButton("Fixed Deposit");

        savingsRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        currentRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fixedDepositRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));

        savingsRadio.setForeground(new Color(191, 219, 254));
        currentRadio.setForeground(new Color(191, 219, 254));
        fixedDepositRadio.setForeground(new Color(191, 219, 254));

        savingsRadio.setOpaque(false);
        currentRadio.setOpaque(false);
        fixedDepositRadio.setOpaque(false);

        accountTypeGroup.add(savingsRadio);
        accountTypeGroup.add(currentRadio);
        accountTypeGroup.add(fixedDepositRadio);

        JPanel radioPanel = new JPanel();
        radioPanel.setOpaque(false);
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.add(savingsRadio);
        radioPanel.add(currentRadio);
        radioPanel.add(fixedDepositRadio);

        accountTypePanel.add(accountTypeLabel);
        accountTypePanel.add(Box.createVerticalStrut(5));
        accountTypePanel.add(radioPanel);

        JPanel passwordPanel = createPasswordFieldWithToggle("Password");
        passwordField = (JPasswordField) passwordPanel.getComponent(0);
        JPanel confirmPasswordPanel = createPasswordFieldWithToggle("Confirm Password");
        confirmPasswordField = (JPasswordField) confirmPasswordPanel.getComponent(0);

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
        captchaTextLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        captchaTextLabel.setForeground(new Color(248, 250, 252));
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
        JButton signupBtn = createButton("Sign Up →");
        signupBtn.addActionListener(e -> handleSignup());

        JLabel loginLink = new JLabel("Already have an account? Login");
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
        card.add(Box.createVerticalStrut(20));
        card.add(nameField);
        card.add(Box.createVerticalStrut(15));
        card.add(emailField);
        card.add(Box.createVerticalStrut(15));
        card.add(idCardField);
        card.add(Box.createVerticalStrut(15));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(15));
        card.add(accountTypePanel);
        card.add(Box.createVerticalStrut(15));
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(15));
        card.add(confirmPasswordPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(captchaPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(signupBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(loginLink);

        // Create a wrapper panel for the card to accept GridBagLayout constraints
        // inside ScrollPane
        JPanel scrollContent = new JPanel(new GridBagLayout());
        scrollContent.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(40, 0, 40, 60); // Added vertical spacing
        scrollContent.add(card, gbc);

        // ScrollPane Setup
        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Custom Dark ScrollBar UI
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(71, 85, 105); // Slate-600
                this.trackColor = new Color(15, 23, 42, 100); // Semi-transparent dark
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8,
                        8);
                g2.dispose();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
        });

        bg.add(scrollPane, BorderLayout.CENTER);
        setContentPane(bg);

        startFadeAnimation();
        generateCaptcha();
    }

    private void handleSignup() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String captchaInput = captchaInputField.getText().trim();

        errorLabel.setVisible(false);
        errorLabel.setText("");

        // Validation
        if (name.isEmpty() || name.equals("Full Name")) {
            showError("Full name is required");
            return;
        }

        if (name.length() < 3) {
            showError("Name must be at least 3 characters");
            return;
        }

        if (email.isEmpty() || email.equals("Email Address")) {
            showError("Email is required");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format");
            return;
        }

        if (password.isEmpty() || String.valueOf(passwordField.getPassword()).equals("Password")) {
            showError("Password is required");
            return;
        }

        // Validate password strength
        PasswordValidator.ValidationResult passwordValidation = PasswordValidator.validate(password);
        if (!passwordValidation.isValid()) {
            showError(passwordValidation.getErrorMessage());
            return;
        }

        if (confirmPassword.isEmpty()
                || String.valueOf(confirmPasswordField.getPassword()).equals("Confirm Password")) {
            showError("Please confirm your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
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

        String idCardNumber = idCardField.getText().trim();

        if (idCardNumber.isEmpty() || idCardNumber.equals("ID Card Number")) {
            showError("ID Card Number is required");
            return;
        }

        // Get selected account type as String
        String selectedAccountType = "SAVINGS";
        if (currentRadio.isSelected()) {
            selectedAccountType = "CURRENT";
        } else if (fixedDepositRadio.isSelected()) {
            selectedAccountType = "FIXED_DEPOSIT";
        }

        // Persist customer in MySQL (XAMPP) using CustomerDatabaseService
        // Username is optional - use empty string or null if not provided
        String finalUsername = username.isEmpty() || username.equals("Username (optional)") ? null : username;

        Customer newCustomer = CustomerDatabaseService.getInstance().createCustomer(
                finalUsername,
                name,
                email,
                null, // phone (not captured in this form)
                null, // address (not captured in this form)
                password,
                idCardNumber,
                selectedAccountType);
        boolean saved = newCustomer != null;

        if (!saved) {
            showError("Could not create account in database. Please try again.");
            return;
        }

        // Success
        JOptionPane.showMessageDialog(this,
                "Account created successfully!\nYou can now login with your email and password.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // Navigate to login
        dispose();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void startFadeAnimation() {
        animationTimer = new Timer(16, e -> {
            // Faster fade-in to match Login page responsiveness
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

        // Stylized bank columns
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
                field.setBorder(new CompoundBorder(
                        new LineBorder(VaultXTheme.PRIMARY_RED, 2, true),
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
