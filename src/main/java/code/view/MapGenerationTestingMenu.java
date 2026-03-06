package code.view;

import code.model.GameMap;
import code.model.generator.MapGenerator;
import code.model.utils.SidePanelFactory;
import javax.swing.*;
import java.awt.*;
import code.model.utils.SliderFactory;
import code.model.utils.GameMapExporter;
import code.model.utils.ButtonFactory;
import code.GameEngine;

/** Menu qui affiche la map avec des sliders et boutons pour tester la génération */
public class MapGenerationTestingMenu extends JPanel {
    private static GameMap gameMap;
    private static GameMapPanel gameMapPanel;

    private static JSlider sliderNOfTeams, sliderDecayFactor, sliderInitialPropagationFactor, sliderWallFrequency;
    private static JSlider sliderMapWidth, sliderMapHeight;
    private static MapListPanel mapListPanel;
    private static JButton btnPauseResume;

    public MapGenerationTestingMenu() {
        setLayout(new BorderLayout(10, 10));
        gameMap = new GameMap(60, 60);
        gameMapPanel = new GameMapPanel(gameMap);
        add(gameMapPanel, BorderLayout.CENTER);

        sliderMapWidth = SliderFactory.createStyledSlider(20, 150, 20, "Largeur de la map", 1.0);
        sliderMapHeight = SliderFactory.createStyledSlider(20, 150, 20, "Hauteur de la map", 1.0);
        sliderNOfTeams = SliderFactory.createStyledSlider(2, 8, MapGenerator.DEFAULT_N_OF_TEAMS, "Nombre de teams", 1.0);
        sliderWallFrequency = SliderFactory.createStyledSlider(50, 500, MapGenerator.DEFAULT_FREQUENCY_OF_WALL_SPOTS, "Frequency of walls (doesnt change much)", 1.0);
        sliderInitialPropagationFactor = SliderFactory.createStyledSlider(0, 100, (int)(MapGenerator.DEFAULT_INITIAL_PROPAGATION_FACTOR * 100), "Initial propagation factor", 100.0);
        sliderDecayFactor = SliderFactory.createStyledSlider(0, 100, (int)(MapGenerator.DEFAULT_PROPAGATION_DECAY_FACTOR * 100), "Decay factor", 100.0);

        JButton btnGenerate = createGenerateButton();
        btnPauseResume = createPauseResumeButton();
        JButton btnExport = createExportButton();
        JButton btnBackToMainMenu = createBackToMainMenuButton();

        mapListPanel = new MapListPanel(loadedMap -> {
            GameEngine.stopGame();
            gameMap = loadedMap;
            gameMapPanel.setMap(gameMap);
            gameMapPanel.repaint();
        });
        mapListPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel sidePanel = SidePanelFactory.createMapGenerationTestingMenuSidePanel(
            sliderMapWidth,
            sliderMapHeight,
            sliderNOfTeams,
            sliderWallFrequency,
            sliderInitialPropagationFactor,
            sliderDecayFactor,
            btnGenerate,
            btnPauseResume,
            btnBackToMainMenu,
            btnExport,
            mapListPanel
        );
        sidePanel.setOpaque(false);
        
        add(sidePanel, BorderLayout.EAST);
    }

    private JButton createBackToMainMenuButton() {
        JButton btnBackToMainMenu = ButtonFactory.createStylizedRegularButton("Menu principal");
        btnBackToMainMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnBackToMainMenu.addActionListener(e -> {
            GameEngine.stopGame();
            GameWindow.showScreen("MAIN_MENU");
        });

        return btnBackToMainMenu;
    }

    /** Crée le bouton Generate */
    private JButton createGenerateButton() {
        JButton btnGenerate = ButtonFactory.createStylizedRegularButton("Générer Map");
        btnGenerate.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnGenerate.addActionListener(e -> {
            GameEngine.stopGame();
            int width = sliderMapWidth.getValue();
            int height = sliderMapHeight.getValue();
            gameMap = new GameMap(width, height);

            MapGenerator generator = new MapGenerator.Builder()
                .setNOfTeams(sliderNOfTeams.getValue())
                .setFrequencyOfWallSpots(sliderWallFrequency.getValue())
                .setInitialPropagationFactor(sliderInitialPropagationFactor.getValue() / 100.0)
                .setPropagationDecayFactor(sliderDecayFactor.getValue() / 100.0)
                .build();
            
            generator.generate(gameMap);
            
            gameMapPanel.setMap(gameMap);
            gameMapPanel.repaint();
        });
        
        return btnGenerate;
    }

    /** Crée le bouton Pause/Resume */
    private JButton createPauseResumeButton() {
        JButton btnPauseResume = ButtonFactory.createStylizedRegularButton("Start");
        btnPauseResume.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPauseResume.setMaximumSize(new Dimension(220, 40));
        
        btnPauseResume.addActionListener(e -> {
            if (!GameEngine.isRunning()) {
                GameEngine.startGame(gameMap);
                btnPauseResume.setText("Pause");
            } else if (GameEngine.isPaused()) {
                GameEngine.resumeGame();
                btnPauseResume.setText("Pause");
            } else {
                GameEngine.pauseGame();
                btnPauseResume.setText("Resume");
            }
        });
        
        return btnPauseResume;
    }

    /** Crée le bouton Export */
    private JButton createExportButton() {
        JButton btnExport = ButtonFactory.createStylizedRegularButton("Exporter en JSON");
        btnExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnExport.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(
                this,
                "Nom du fichier (sans le .json):",
                "Exporteur",
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (fileName != null && !fileName.trim().isEmpty()) {
                try {
                    GameMapExporter.exportGameMap(gameMap, fileName.trim());
                    mapListPanel.refreshMapList();
                    JOptionPane.showMessageDialog(
                        this,
                        "Map exportée !",
                        "Export réussi",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Erreur : " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        return btnExport;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (getLayout() instanceof BorderLayout) {
            Component east = ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.EAST);
            if (east != null) {
                Rectangle bounds = east.getBounds();
                g2d.setColor(new Color(150, 130, 100));
                g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }
}