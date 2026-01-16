package com.bank.brewdreamwelcome.ui.views;

import com.bank.brewdreamwelcome.core.ThemeManager;
import com.bank.brewdreamwelcome.service.AuditService;
import com.bank.brewdreamwelcome.CustomerDatabaseService;
import com.bank.brewdreamwelcome.Customer;
import com.bank.brewdreamwelcome.validation.InputValidator;
import com.bank.brewdreamwelcome.util.CaptchaGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Enhanced Signup View with validation, CAPTCHA, and password visibility toggle.
 */
public class ModernSignupView extends JFrame {
    private JTextField nameField, emailField, idCardField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> accountTypeCombo;
    private JLabel errorLabel;
    private JPanel errorPanel;
    
    private JLabel captchaImageLabel;
    private JTextField captchaInputField;
    private String currentCaptchaCode;
    
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    public ModernSignupView() {
        setTitle("VaultX | Create Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 750));
        
        JPanel root = new JPanel(new MigLayout("fill, insets 0", "[grow, fill][500!, fill]", "fill"));
        root.setBackground(ThemeManager.getBackground());

        // Branding Side
        JPanel branding = new JPanel(new MigLayout("fill, insets 40", "[center]", "[center]"));
        branding.setBackground(new Color(15, 23, 42));
        JLabel logoBrand = new JLabel("VaultX");
        logoBrand.setFont(new Font("Inter", Font.BOLD, 56));
        logoBrand.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("Secure Banking Starts Here");
        tagline.setFont(new Font("Inter", Font.PLAIN, 16));
        tagline.setForeground(new Color(148, 163, 184));
        
        JPanel brandContent = new JPanel(new MigLayout("insets 0", "[center]"));
        brandContent.setOpaque(false);
        brandContent.add(logoBrand, "wrap");
        brandContent.add(tagline);
        branding.add(brandContent);

        // Signup Form Side with ScrollPane
        JPanel formContainer = new JPanel(new MigLayout("fillx, insets 30 60 30 60", "[grow, fill]"));
        formContainer.setOpaque(false);

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Inter", Font.BOLD, 32));
        title.setForeground(ThemeManager.ACCENT_BLUE);

        errorPanel = new JPanel(new MigLayout("fillx, insets 10 15 10 15", "[grow, fill]"));
        errorPanel.setBackground(new Color(254, 226, 226));
        errorPanel.setBorder(new MatteBorder(0, 4, 0, 0, ThemeManager.DANGER_RED));
        errorLabel = new JLabel("Error message");
        errorLabel.setForeground(ThemeManager.DANGER_RED);
        errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        errorPanel.add(errorLabel);
        errorPanel.setVisible(false);

        nameField = createStyledTextField("John Doe");
        emailField = createStyledTextField("john@example.com");
        idCardField = createStyledTextField("12345-6789012-3");
        
        accountTypeCombo = new JComboBox<>(new String[]{"SAVINGS", "CURRENT", "FIXED_DEPOSIT"});
        accountTypeCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        
        // Password fields with eye icons
        JPanel passwordPanel = createPasswordFieldWithToggle();
        JPanel confirmPasswordPanel = createConfirmPasswordFieldWithToggle();

        // CAPTCHA Panel
        JPanel captchaPanel = new JPanel(new MigLayout("fillx, insets 0", "[]10[]10[]"));
        captchaPanel.setOpaque(false);
        
        captchaImageLabel = new JLabel();
        captchaImageLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        
        captchaInputField = new JTextField();
        captchaInputField.setFont(new Font("Inter", Font.PLAIN, 14));
        
        JButton refreshCaptchaBtn = new JButton("↻");
        refreshCaptchaBtn.setFont(new Font("Inter", Font.BOLD, 20));
        refreshCaptchaBtn.setFocusPainted(false);
        refreshCaptchaBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshCaptchaBtn.addActionListener(e -> generateCaptcha());
        
        captchaPanel.add(captchaImageLabel, "width 180!, height 60!");
        captchaPanel.add(captchaInputField, "width 120!, height 40!");
        captchaPanel.add(refreshCaptchaBtn, "width 40!, height 40!");

        JButton signupBtn = new JButton("Create Account");
        signupBtn.setBackground(ThemeManager.ACCENT_BLUE);
        signupBtn.setForeground(Color.WHITE);
        signupBtn.setFont(new Font("Inter", Font.BOLD, 16));
        signupBtn.setFocusPainted(false);
        signupBtn.setBorderPainted(false);
        signupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupBtn.addActionListener(e -> handleSignup());

        JLabel loginLink = new JLabel("Already have an account? Sign In");
        loginLink.setForeground(ThemeManager.ACCENT_BLUE);
        loginLink.setFont(new Font("Inter", Font.BOLD, 13));
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { 
                dispose(); 
                new ModernLoginView().setVisible(true); 
            }
        });

        formContainer.add(title, "wrap, gapbottom 5");
        formContainer.add(new JLabel("Start your professional banking journey"), "wrap, gapbottom 15");
        formContainer.add(errorPanel, "wrap, hidemode 3, gapbottom 10");
        
        formContainer.add(new JLabel("Full Name *"), "wrap, gapbottom 2");
        formContainer.add(nameField, "wrap, height 40!, gapbottom 10");
        
        formContainer.add(new JLabel("Email Address *"), "wrap, gapbottom 2");
        formContainer.add(emailField, "wrap, height 40!, gapbottom 10");

        formContainer.add(new JLabel("ID Card Number * (Format: 12345-6789012-3)"), "wrap, gapbottom 2");
        formContainer.add(idCardField, "wrap, height 40!, gapbottom 10");

        formContainer.add(new JLabel("Account Type *"), "wrap, gapbottom 2");
        formContainer.add(accountTypeCombo, "wrap, height 40!, gapbottom 10");

        formContainer.add(new JLabel("Password * (Min 8 chars, uppercase, lowercase, number, special char)"), "wrap, gapbottom 2");
        formContainer.add(passwordPanel, "wrap, height 40!, gapbottom 10");

        formContainer.add(new JLabel("Confirm Password *"), "wrap, gapbottom 2");
        formContainer.add(confirmPasswordPanel, "wrap, height 40!, gapbottom 15");

        formContainer.add(new JLabel("Security Verification"), "wrap, gapbottom 5");
        formContainer.add(captchaPanel, "wrap, gapbottom 20");

        formContainer.add(signupBtn, "height 48!, wrap, gapbottom 10");
        formContainer.add(loginLink, "center");

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        root.add(branding, "grow");
        root.add(scrollPane, "grow");

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        
        generateCaptcha();
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setFont(new Font("Inter", Font.PLAIN, 14));
        return f;
    }

    private JPanel createPasswordFieldWithToggle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Inter", Font.PLAIN, 14));
        
        JButton eyeBtn = new JButton("👁");
        eyeBtn.setFont(new Font("Inter", Font.PLAIN, 16));
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeBtn.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                passwordField.setEchoChar((char) 0);
                eyeBtn.setText("🙈");
            } else {
                passwordField.setEchoChar('•');
                eyeBtn.setText("👁");
            }
        });
        
        panel.add(passwordField, BorderLayout.CENTER);
        panel.add(eyeBtn, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createConfirmPasswordFieldWithToggle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Inter", Font.PLAIN, 14));
        
        JButton eyeBtn = new JButton("👁");
        eyeBtn.setFont(new Font("Inter", Font.PLAIN, 16));
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeBtn.addActionListener(e -> {
            confirmPasswordVisible = !confirmPasswordVisible;
            if (confirmPasswordVisible) {
                confirmPasswordField.setEchoChar((char) 0);
                eyeBtn.setText("🙈");
            } else {
                confirmPasswordField.setEchoChar('•');
                eyeBtn.setText("👁");
            }
        });
        
        panel.add(confirmPasswordField, BorderLayout.CENTER);
        panel.add(eyeBtn, BorderLayout.EAST);
        
        return panel;
    }

    private void generateCaptcha() {
        CaptchaGenerator.CaptchaData captcha = CaptchaGenerator.generate(180, 60);
        currentCaptchaCode = captcha.getCode();
        captchaImageLabel.setIcon(new ImageIcon(captcha.getImage()));
        captchaInputField.setText("");
    }

    private void handleSignup() {
        hideError();
        
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String idCard = idCardField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        String type = (String) accountTypeCombo.getSelectedItem();
        String captchaInput = captchaInputField.getText().trim();

        // Comprehensive Validation
        InputValidator.ValidationResult nameValid = InputValidator.validateName(name);
        if (!nameValid.isValid()) {
            showError(nameValid.getFirstError());
            return;
        }

        InputValidator.ValidationResult emailValid = InputValidator.validateEmail(email);
        if (!emailValid.isValid()) {
            showError(emailValid.getFirstError());
            return;
        }

        InputValidator.ValidationResult idCardValid = InputValidator.validateIdCard(idCard);
        if (!idCardValid.isValid()) {
            showError(idCardValid.getFirstError());
            return;
        }

        InputValidator.ValidationResult passwordValid = InputValidator.validatePassword(pass);
        if (!passwordValid.isValid()) {
            showError(passwordValid.getFirstError());
            return;
        }

        if (!pass.equals(confirmPass)) {
            showError("Passwords do not match");
            return;
        }

        if (captchaInput.isEmpty()) {
            showError("Please enter the CAPTCHA code");
            return;
        }

        if (!captchaInput.equalsIgnoreCase(currentCaptchaCode)) {
            showError("Incorrect CAPTCHA. Please try again.");
            generateCaptcha();
            return;
        }

        // Create customer with PENDING status (default in database)
        Customer customer = CustomerDatabaseService.getInstance().createCustomer(
            name.toLowerCase().replace(" ", "_"),
            name, email, "", "", pass, idCard, type
        );

        if (customer != null) {
            AuditService.log("SIGNUP_REQUEST", "New account created (PENDING approval): " + email);
            JOptionPane.showMessageDialog(this, 
                "Account created successfully!\n\n" +
                "Your account is pending admin approval.\n" +
                "You will be able to login once approved.\n\n" +
                "Account ID: " + customer.getAccountId(),
                "Registration Successful", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new ModernLoginView().setVisible(true);
        } else {
            showError("Account creation failed. Email or ID Card might already be in use.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void hideError() {
        errorPanel.setVisible(false);
        revalidate();
        repaint();
    }
}
