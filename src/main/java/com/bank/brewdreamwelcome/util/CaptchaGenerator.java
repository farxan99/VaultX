package com.bank.brewdreamwelcome.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * CAPTCHA generator for security validation.
 * Generates random alphanumeric codes with visual distortion.
 */
public class CaptchaGenerator {
    
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CAPTCHA_LENGTH = 6;
    private static final Random random = new Random();
    
    public static class CaptchaData {
        private final String code;
        private final BufferedImage image;
        
        public CaptchaData(String code, BufferedImage image) {
            this.code = code;
            this.image = image;
        }
        
        public String getCode() { return code; }
        public BufferedImage getImage() { return image; }
    }
    
    public static String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            code.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return code.toString();
    }
    
    public static BufferedImage generateImage(String code, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);
        
        // Add noise lines
        g2d.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw CAPTCHA text
        Font font = new Font("Arial", Font.BOLD, 28);
        g2d.setFont(font);
        
        int charWidth = width / code.length();
        for (int i = 0; i < code.length(); i++) {
            // Random color for each character
            g2d.setColor(new Color(
                random.nextInt(100),
                random.nextInt(100),
                random.nextInt(100)
            ));
            
            // Random rotation
            double angle = (random.nextDouble() - 0.5) * 0.4;
            g2d.rotate(angle, charWidth * i + charWidth / 2, height / 2);
            
            // Draw character
            g2d.drawString(
                String.valueOf(code.charAt(i)),
                charWidth * i + 10,
                height / 2 + 10
            );
            
            // Reset rotation
            g2d.rotate(-angle, charWidth * i + charWidth / 2, height / 2);
        }
        
        // Add noise dots
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g2d.fillOval(x, y, 2, 2);
        }
        
        g2d.dispose();
        return image;
    }
    
    public static CaptchaData generate(int width, int height) {
        String code = generateCode();
        BufferedImage image = generateImage(code, width, height);
        return new CaptchaData(code, image);
    }
}
