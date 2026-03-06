package code.view;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import code.model.GameMap;
import code.model.Cell;
import code.model.Coord;

/** Un panel qui contient le mapObj et affiche la cellMap */
public class GameMapPanel extends JPanel {
    private GameMap gameMap;
    private Cell[][] cellMap; // la map de gameMap.getCellMap, on veut éviter de devoir get plein de fois
    private int mapWidth = 0;
    private int mapHeight = 0;
    private int cellSize = 1;
    private BufferedImage backgroundImage;

    private int playerTeamId = 1; // TODO : Hardcoded, peut être à changer plus tard mais on n'aura pas le temps pour le réseau en soit.

    public GameMapPanel(GameMap gameMap) { 
        setOpaque(false);
        setMap(gameMap);
        setupMouseControl();

        try {
            backgroundImage = ImageIO.read(new File("src/texture/background.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.addComponentListener(new java.awt.event.ComponentAdapter() { // Si on resize la fenêtre du jeu
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                recalculateCellSize();
            }
        });
        
        recalculateCellSize(); // Quand on créé un GameMapPanel 
    }

    public void setMap(GameMap gameMap) {
        this.gameMap = gameMap;
        if (gameMap != null) {
            this.cellMap = gameMap.getCellMap(); 
            this.mapHeight = cellMap.length;
            this.mapWidth = cellMap[0].length;
        } else {
            System.out.println("[setMap - GameMapPanel] pas de gamemap");
            this.cellMap = null;
            this.mapWidth = 0;
            this.mapHeight = 0;
        }
        recalculateCellSize();
        repaint();
    }

    /** Calcule la taille d'une cell quand la gamemap est changée, pour ensuite être utilisée dans updateCursorPosition */
    private void recalculateCellSize() {
        if (gameMap == null) return;

        if (mapWidth > 0 && mapHeight > 0) {
            this.cellSize = Math.min(getWidth() / mapWidth, getHeight() / mapHeight);
            if (this.cellSize < 1) {
                System.out.println("[recalculatecellsize] erreur");
                this.cellSize = 1; 
            }
        }
    }

    public GameMap getGameMapPanel(){return gameMap;}

    /**
     * Configure le contrôle par souris pour déplacer le curseur de l'équipe du joueur
     */
    private void setupMouseControl() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateCursorPosition(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateCursorPosition(e.getX(), e.getY());
            }
        });
    }

    /**
     * Détermine une position de la souris utilisateur sur la map. On calcule selon la position de la souris et la taille de la map, un set (x,y) correspondant à la case
     * ou l'on se situe. si le (x,y) est en dehors des mesures de la map, c'est que la souris est en dehors. on a un Math.min(cursorX, mapwidth-1) qui clamp
     * la souris aux bords de la map dans ce cas. 
     */ 
    private void updateCursorPosition(int mouseX, int mouseY) {
        if (cellMap == null || cellSize <= 0) return;
        
        // WARNING : CALCUL VALABLE TANT QUE LA MAP RESTE EN HAUT A GAUCHE
        int cursorX = mouseX / cellSize;
        int cursorY = mouseY / cellSize;

        // Clamp
        cursorX = Math.max(0, Math.min(cursorX, mapWidth - 1));
        cursorY = Math.max(0, Math.min(cursorY, mapHeight - 1));

        gameMap.setTeamCursor(playerTeamId, new Coord(cursorX, cursorY));
    }

    private java.awt.Color fxToAwt(javafx.scene.paint.Color fxColor) {
        if (fxColor == null) return java.awt.Color.BLACK;
        return new java.awt.Color(
            (float) fxColor.getRed(),
            (float) fxColor.getGreen(),
            (float) fxColor.getBlue(),
            (float) fxColor.getOpacity()
        );
    }


    @Override
    protected void paintComponent(Graphics g) {
        //System.out.println("painting");
        super.paintComponent(g);
        if (cellMap == null) return;
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, mapWidth * cellSize, mapHeight * cellSize, null);
        }

        for (int y = 0; y < cellMap.length; y++) {
            for (int x = 0; x < cellMap[0].length; x++) {
                Color cellColor = cellMap[y][x].getDisplayColor();
                if (cellColor.equals(Color.WHITE)) {
                    g2d.setColor(new Color(0, 0, 0, 0));
                } else {
                    g2d.setColor(new Color(cellColor.getRed(), cellColor.getGreen(), cellColor.getBlue(), 255));
                }
                g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                g2d.setColor(new Color(200,200,200,20));
                g2d.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        // Entoure la case correspondante au curseur en noir (c'est surtout à des fins de debug)
        Coord playerCursor = gameMap.getTeamCursor(playerTeamId);
        if (playerCursor != null) {
            int cursorX = playerCursor.getX() * cellSize;
            int cursorY = playerCursor.getY() * cellSize;

            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            
            g2.setColor(Color.BLACK);
            g2.drawRect(cursorX + 1, cursorY + 1, cellSize - 2, cellSize - 2);
            
            g2.setStroke(new BasicStroke(1));
        }

        // --- Barre globale des équipes ---
if (gameMap.getTeams() != null && !gameMap.getTeams().isEmpty()) {
    int barWidth = 30;
    int padding = 10;
    int x = mapWidth * cellSize + padding;
    int y = padding;
    int totalHeight = getHeight() - 2 * padding;

    g2d.setColor(new Color(50, 50, 50, 180));
    g2d.fillRect(x, y, barWidth, totalHeight);

    // Calcul énergie totale
    int totalEnergy = gameMap.getTeams().stream()
            .mapToInt(gameMap::getTeamEnergy)
            .sum();
    if (totalEnergy == 0) totalEnergy = 1;

    // Dessin des portions
    int currentY = y + totalHeight;
    for (Integer teamId : gameMap.getTeams()) {
        int energy = gameMap.getTeamEnergy(teamId);
        double ratio = (double) energy / totalEnergy;
        int portionHeight = (int) (totalHeight * ratio);

        currentY -= portionHeight;
        g2d.setColor(fxToAwt(gameMap.getTeamColor(teamId)));
        g2d.fillRect(x, currentY, barWidth, portionHeight);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(x, currentY, barWidth, portionHeight);
    }

    // Bord extérieur de la barre
    g2d.setColor(Color.BLACK);
    g2d.drawRect(x, y, barWidth, totalHeight);
}
    }
}