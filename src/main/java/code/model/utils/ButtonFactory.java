package code.model.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/** Fonctions de stylization */
public class ButtonFactory {
    private static final Color BG_COLOR = new Color(232, 233, 235);
    private static final Color TEXT_COLOR = new Color(0, 0, 0);
    private static final Color BORDER_COLOR = new Color(0,0,0);
    
    private static Font customFont = null;
    
    // static initializer block, seul moyen qu'une static class execute du code seule sans références.
    static {
        loadCustomFont();
    }
    
    /** Load la VCR_MONO utilisée pour les textes des buttons */
    private static void loadCustomFont() {
        try {
            File fontFile = new File("appdata/fonts/Swansea.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (FontFormatException | IOException e) {
            System.out.println("[ButtonFactory] : " + e.getMessage());
            customFont = new Font("Arial", Font.BOLD, 14);
        }
    }
    
    /** Crée un bouton stylisé standard */
    public static JButton createStylizedRegularButton(String text) {
        JButton button = new JButton(text);
        
        button.setFont(customFont.deriveFont(Font.BOLD, 14f));
        button.setForeground(TEXT_COLOR);
        button.setBackground(BG_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        
        return button;
    }
}