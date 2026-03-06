package code.view;

import javax.swing.*;
import java.awt.*;
import code.model.GameMap;
import code.model.utils.ButtonFactory;
import code.model.utils.SidePanelFactory;
import code.GameEngine;

public class GamePlayMenu extends JPanel {
    private static GameMap gameMap;
    private static GameMapPanel gameMapPanel;
    
    public GamePlayMenu() {
        setLayout(new BorderLayout(10, 10));

        setBackground(new Color(200, 200, 200));
        
        // Map par défaut (sera remplacée par setMap qui est call quand on bascule de LevelSelectionMenu à GamePlayMenu)
        gameMap = new GameMap(60, 60);
        gameMapPanel = new GameMapPanel(gameMap);
        add(gameMapPanel, BorderLayout.CENTER);
        
        JButton btnBackToLevelSelect = ButtonFactory.createStylizedRegularButton("Level Selection");
        btnBackToLevelSelect.addActionListener(e -> {
            GameEngine.stopGame();
            GameWindow.showScreen("LEVEL_SELECTION_MENU");
        });
        
        JButton btnBackToMainMenu = ButtonFactory.createStylizedRegularButton("Menu principal");
        btnBackToMainMenu.addActionListener(e -> {
            GameEngine.stopGame();
            GameWindow.showScreen("MAIN_MENU");
        });
        
        JPanel sidePanel = SidePanelFactory.createGamePlayMenuSidePanel(btnBackToLevelSelect, btnBackToMainMenu);
        sidePanel.setOpaque(false); // rendre le sidePanel transparent pour voir le fond gris derrière

        JPanel sideWrapper = new JPanel(new BorderLayout());
        sideWrapper.setOpaque(true);
        sideWrapper.setBackground(new Color(150, 130, 100)); // gris plus foncé derrière les paramètres
        sideWrapper.add(sidePanel, BorderLayout.CENTER);

        add(sideWrapper, BorderLayout.EAST);
    }
    
    /**
     * Met à jour la map affichée et démarre le jeu
     */
    public static void setMap(GameMap map) {
        gameMap = map;
        gameMapPanel.setMap(map);
        gameMapPanel.repaint();
        
        // Démarre le moteur avec la nouvelle map
        GameEngine.startGame(gameMap);
    }
}