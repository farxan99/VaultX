package com.bank.brewdreamwelcome;

import javax.swing.*;
import java.awt.*;

/**
 * Shared VaultX visual theme helpers.
 * Central place to load the background image and reuse core colors.
 */
public final class VaultXTheme {

    private static Image backgroundImage;

    // Core brand colors
    public static final Color DARK_BG = new Color(10, 10, 20);
    public static final Color DARK_BG_ALT = new Color(15, 23, 42);
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    public static final Color PRIMARY_RED = new Color(239, 68, 68);
    public static final Color ACCENT_RED_DARK = new Color(185, 28, 28);
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);

    private VaultXTheme() {
    }

    private static Image loadBackgroundImage() {
        if (backgroundImage != null) {
            return backgroundImage;
        }
        try {
            java.net.URL url = VaultXTheme.class.getResource("vaultX.png");
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            }
        } catch (Exception ignored) {
        }
        return backgroundImage;
    }

    /**
     * Paints the VaultX background image scaled to fully cover the component.
     * Safe to call from any Swing {@code paintComponent}.
     */
    public static void paintBackgroundImage(Graphics2D g2, Component c) {
        Image img = loadBackgroundImage();
        if (img == null) {
            return;
        }

        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) {
            return;
        }

        int w = c.getWidth();
        int h = c.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        double scale = Math.max((double) w / iw, (double) h / ih);
        int sw = (int) (iw * scale);
        int sh = (int) (ih * scale);
        int x = (w - sw) / 2;
        int y = (h - sh) / 2;

        g2.drawImage(img, x, y, sw, sh, null);
    }
}
