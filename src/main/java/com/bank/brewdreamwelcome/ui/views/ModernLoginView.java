package com.bank.brewdreamwelcome.ui.views;

import com.bank.brewdreamwelcome.core.ThemeManager;
import com.bank.brewdreamwelcome.core.SessionManager;
import com.bank.brewdreamwelcome.service.AuditService;
import com.bank.brewdreamwelcome.service.AccountApprovalService;
import com.bank.brewdreamwelcome.AuthService;
import com.bank.brewdreamwelcome.util.CaptchaGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Enhanced Login View with CAPTCHA, account status validation, and role-based routing.
 */
public class ModernLoginView extends JFrame {
    private JTextField identityField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JPanel errorPanel;
    
    private JLabel captchaImageLabel;
    private JTextField captchaInputField;
    private String currentCaptchaCode;

    public ModernLoginView() {
        setTitle("VaultX | Secure Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(900, 600));
        
        JPanel root = new JPanel(new MigLayout("fill, insets 0", "[grow, fill][450!, fill]", "fill"));
        root.setBackground(ThemeManager.getBackground());

        // Branding Side
        JPanel branding = new JPanel(new MigLayout("fill, insets 40", "[center]", "[center]"));
        branding.setBackground(new Color(15, 23, 42));
        
        JLabel logoBrand = new JLabel("VaultX");
        logoBrand.setFont(new Font("Inter", Font.BOLD, 56));
        logoBrand.setForeground(Color.WHITE);
        
        JLabel tagline = new JLabel("Your security, our priority.");
        tagline.setFont(new Font("Inter", Font.PLAIN, 18));
        tagline.setForeground(new Color(148, 163, 184));
        
        branding.add(logoBrand, "wrap");
        branding.add(tagline);

        // Login Form Side
        JPanel form = new JPanel(new MigLayout("fillx, insets 40 50 40 50", "[grow, fill]"));
        form.setOpaque(false);

        JLabel title = new JLabel("Sign In");
        title.setFont(new Font("Inter", Font.BOLD, 32));
        title.setForeground(ThemeManager.ACCENT_BLUE);
        
        // Error Panel
        errorPanel = new JPanel(new MigLayout("fillx, insets 8 12 8 12", "[grow, fill]"));
        errorPanel.setBackground(new Color(254, 226, 226));
        errorPanel.setBorder(new MatteBorder(0, 4, 0, 0, ThemeManager.DANGER_RED));
        errorLabel = new JLabel("Error message goes here");
        errorLabel.setForeground(ThemeManager.DANGER_RED);
        errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        errorPanel.add(errorLabel);
        errorPanel.setVisible(false);

        identityField = createStyledTextField("Username / Email / Account ID");
        passwordField = createStyledPasswordField();
        
        // CAPTCHA Panel
        JPanel captchaPanel = new JPanel(new MigLayout("fillx, insets 0", "[]8[]8[]"));
        captchaPanel.setOpaque(false);
        
        captchaImageLabel = new JLabel();
        captchaImageLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        
        captchaInputField = new JTextField();
        captchaInputField.setFont(new Font("Inter", Font.PLAIN, 14));
        
        JButton refreshCaptchaBtn = new JButton("↻");
        refreshCaptchaBtn.setFont(new Font("Inter", Font.BOLD, 18));
        refreshCaptchaBtn.setFocusPainted(false);
        refreshCaptchaBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshCaptchaBtn.addActionListener(e -> generateCaptcha());
        
        captchaPanel.add(captchaImageLabel, "width 160!, height 50!");
        captchaPanel.add(captchaInputField, "width 100!, height 35!");
        captchaPanel.add(refreshCaptchaBtn, "width 35!, height 35!");
        
        JButton loginBtn = new JButton("Login to Dashboard");
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setBackground(ThemeManager.ACCENT_BLUE);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Inter", Font.BOLD, 15));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> handleLogin());

        // Signup Option
        JPanel signupPrompt = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        signupPrompt.setOpaque(false);
        JLabel text = new JLabel("New to VaultX?");
        text.setForeground(new Color(100, 116, 139));
        text.setFont(new Font("Inter", Font.PLAIN, 12));
        JLabel signupLink = new JLabel("Create an account");
        signupLink.setForeground(ThemeManager.ACCENT_BLUE);
        signupLink.setFont(new Font("Inter", Font.BOLD, 12));
        signupLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new ModernSignupView().setVisible(true);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                signupLink.setText("<html><u>Create an account</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                signupLink.setText("Create an account");
            }
        });
        signupPrompt.add(text);
        signupPrompt.add(signupLink);

        form.add(title, "wrap, gapbottom 5");
        form.add(new JLabel("Welcome back! Please enter your details."), "wrap, gapbottom 15");
        form.add(errorPanel, "wrap, hidemode 3, gapbottom 12");
        
        form.add(new JLabel("Account Identifier"), "wrap, gapbottom 3");
        form.add(identityField, "wrap, height 42!, gapbottom 12");
        
        form.add(new JLabel("Secure Password"), "wrap, gapbottom 3");
        form.add(passwordField, "wrap, height 42!, gapbottom 12");
        
        form.add(new JLabel("Security Verification"), "wrap, gapbottom 3");
        form.add(captchaPanel, "wrap, gapbottom 15");
        
        form.add(loginBtn, "height 45!, wrap, gapbottom 12");
        form.add(signupPrompt, "center");

        root.add(branding, "grow");
        root.add(form, "grow");

        setContentPane(root);
        setLocationRelativeTo(null);
        
        generateCaptcha();
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        f.setFont(new Font("Inter", Font.PLAIN, 13));
        return f;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.putClientProperty("JTextField.placeholderText", "••••••••");
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        f.setFont(new Font("Inter", Font.PLAIN, 13));
        return f;
    }

    private void generateCaptcha() {
        CaptchaGenerator.CaptchaData captcha = CaptchaGenerator.generate(160, 50);
        currentCaptchaCode = captcha.getCode();
        captchaImageLabel.setIcon(new ImageIcon(captcha.getImage()));
        captchaInputField.setText("");
    }

    private void handleLogin() {
        String id = identityField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String captchaInput = captchaInputField.getText().trim();
        
        // Reset validation UI
        identityField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)));

        // Validate CAPTCHA
        if (captchaInput.isEmpty()) {
            showError("Please enter the CAPTCHA code");
            return;
        }
        
        if (!captchaInput.equalsIgnoreCase(currentCaptchaCode)) {
            showError("Incorrect CAPTCHA. Please try again.");
            generateCaptcha();
            return;
        }

        AuthService.AuthResult result = AuthService.authenticate(id, pass);
        
        if (result.success) {
            // Check account status for customers
            if (result.role == AuthService.Role.CUSTOMER) {
                String status = AccountApprovalService.getInstance().getAccountStatus(result.customerId);
                
                if ("PENDING".equals(status)) {
                    showError("Your account is pending admin approval. Please wait for approval.");
                    AuditService.log("LOGIN_BLOCKED", "Pending account login attempt: " + id);
                    return;
                } else if ("REJECTED".equals(status)) {
                    showError("Your account has been rejected. Please contact support.");
                    AuditService.log("LOGIN_BLOCKED", "Rejected account login attempt: " + id);
                    return;
                }
            }
            
            hideError();
            AuditService.log("LOGIN_SUCCESS", "User logged in: " + id + " (Role: " + result.role + ")");
            
            dispose();
            
            // Route to appropriate dashboard
            if (result.role == AuthService.Role.ADMIN) {
                SessionManager.setAdminSession(result.customerId != null ? result.customerId : 1);
                new com.bank.brewdreamwelcome.AdminDashboardFrame().setVisible(true);
            } else {
                SessionManager.setCustomerSession(result.customerId);
                new FunctionalCustomerDashboard(result.customerId).setVisible(true);
            }
        } else {
            showError(result.message);
            generateCaptcha();
            
            // Highlight specific field based on errorCode
            if ("MISSING_ID".equals(result.errorCode) || "ACCOUNT_NOT_FOUND".equals(result.errorCode)) {
                highlightError(identityField);
            } else if ("MISSING_PASSWORD".equals(result.errorCode) || "INVALID_PASSWORD".equals(result.errorCode)) {
                highlightError(passwordField);
            }
            
            AuditService.log("LOGIN_FAILURE", "Failed login: " + id + " - " + result.errorCode);
        }
    }

    private void highlightError(JComponent comp) {
        comp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.DANGER_RED, 1),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)));
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
