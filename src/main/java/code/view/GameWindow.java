package code.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import code.GameEngine;
import code.model.GameMap;

import java.awt.CardLayout;

public class GameWindow extends JFrame {
    private static GameWindow instance;
    private static CardLayout cardLayout;
    private static JPanel container;
    
    private static final MainMenu mainMenu = new MainMenu();
    private static final MapGenerationTestingMenu mapGenerationTestingMenu = new MapGenerationTestingMenu();
    private static final GamePlayMenu gamePlayMenu = new GamePlayMenu();

    public GameWindow(){
        instance = this;
        setTitle("Liquid Wars - Splatoon 4");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
                dispose();
                System.exit(0);
            }
        });

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        add(container);
        initialize();
        setVisible(true);
    }

    /** Ajoute les maps au container (sépare un gros bloc du constructeur) */
    private void initialize() {        
        container.add(mainMenu, "MAIN_MENU");
        container.add(mapGenerationTestingMenu, "MAP_GENERATION_TESTING");
        container.add(gamePlayMenu, "GAMEPLAY");
        
        revalidate();
        repaint();
    }

    public static void showScreen(String key){
        cardLayout.show(container, key);
    }

    /** Repaint n'est pas static. on a besoin d'une méthode statique pour repaint GameWindow sans ref de gamewindow, dans GameEngine. cette fonction statique appelle repaint. */
    public static void refresh() {
        if (instance != null) {
            instance.repaint();
        }
    }

    /**
     * Méthode appelée juste avant la fermeture de la fenêtre.
     * (libération de threads, sauvegarde, nettoyage, etc.)
     */
    private void onClose() {
        GameEngine.stopGame(); // Stopper le thread AVANT de shutdown l'ExecutorService
        GameMap gameMap = GameEngine.getGameMap();
        if (gameMap != null) {
            gameMap.shutdown();
        }
    }
}