package code.view;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import code.GameEngine;

public class MainMenu extends JPanel {
    private BufferedImage background;

    public MainMenu() {
        setLayout(new BorderLayout());
        loadBackground();

        JPanel wrapper = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (background != null) {
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        wrapper.setOpaque(false);
        add(wrapper, BorderLayout.CENTER);

        JLabel title = new JLabel("Liquid Wars CD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 50, 1280, 60);
        wrapper.add(title);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton btnLevelSelectionMenu = new JButton("Levels");
        btnLevelSelectionMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLevelSelectionMenu.addActionListener(e -> showLevelSelectionPopup());
        buttonPanel.add(btnLevelSelectionMenu);
        buttonPanel.add(Box.createVerticalStrut(10));

        JButton btnTestMenu = new JButton("Map Tester");
        btnTestMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTestMenu.addActionListener(e -> GameWindow.showScreen("MAP_GENERATION_TESTING"));
        buttonPanel.add(btnTestMenu);
        buttonPanel.add(Box.createVerticalStrut(10));

        JButton btnExit = new JButton("Exit");
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExit.addActionListener(e -> System.exit(0));
        buttonPanel.add(btnExit);

        wrapper.setLayout(null);
        wrapper.add(buttonPanel);

        // Positionnement dynamique en bas gauche
        wrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int panelWidth = 200;
                int panelHeight = buttonPanel.getPreferredSize().height;
                buttonPanel.setBounds(20, wrapper.getHeight() - panelHeight - 20, panelWidth, panelHeight);
            }
        });
    }

    private void loadBackground() {
        try {
            background = ImageIO.read(new File("src/texture/background.jpg"));
        } catch (Exception e) {
            System.out.println("[MainMenu] Impossible de charger background.jpg : " + e.getMessage());
        }
    }

    private void showLevelSelectionPopup() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Levels", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Levels", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        dialog.add(titleLabel, BorderLayout.NORTH);

        MapListPanel mapListPanel = new MapListPanel(loadedMap -> {
            GamePlayMenu.setMap(loadedMap);
            GameWindow.showScreen("GAMEPLAY"); // switch vers gameplay
            dialog.dispose();
        });

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.add(mapListPanel);
        dialog.add(centerWrapper, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Retour");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> dialog.dispose());
        bottomPanel.add(btnBack);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}