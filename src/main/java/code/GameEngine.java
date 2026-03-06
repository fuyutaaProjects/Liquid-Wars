package code;

import code.model.GameMap;
import code.view.*;
import code.model.ai.TeamAI;
import code.model.ai.AIFactory;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/** Nous avons deux variables similaires, running et paused. On pourrait penser qu'il suffit d'une seule, cependant on veut pouvoir pause la partie (refresh map), 
 * tout en évitant de freeze tous les updates d'affichages auxiliaires à la map. On veut pouvoir continuer a faire des updates, commandes pendant que le thread tourne.
 * On a donc running pour pauser le thread tout entier et paused pour contrôler l'affichage de la map et intéractions des cells.
 */
public class GameEngine {
    private static GameMap gameMap;
    private static boolean running;
    private static boolean paused;
    private static Thread gameThread;
    private static int winner;
    private static final Map<Integer, TeamAI> teamAIs = new HashMap<>();

    public static final int PLAYER_TEAM_ID = 1;

    private GameEngine() {} // Empêche l'instantiation

    public static void startGame(GameMap map) {
        stopGame(); // on va dé-référencer l'ancien thread, mais on le stoppe quand même, pour pas qu'il ne puisse run encore avant que le garbage collector s'en occupe
        gameMap = map;

        teamAIs.clear();
        for (int teamId = 0; teamId < map.getTeamIds().size(); teamId++) {
            if (teamId == PLAYER_TEAM_ID) continue;
            teamAIs.put(teamId, AIFactory.randomAI());
        }

        running = true;
        paused = false;
        gameThread = new Thread(() -> {
            while (running) {
                if (!paused && gameMap != null) {
                    for (var entry : teamAIs.entrySet()) {
                        entry.getValue().updateCursor(gameMap, entry.getKey());
                    }
                    gameMap.updateAll();
                    SwingUtilities.invokeLater(GameWindow::refresh);
                    winner = gameMap.getWinningTeamId();
                    if (winner != -1) {
                        handleGameEnd(winner);
                        break; // on sort du thread
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        gameThread.start();
    }

    public static void pauseGame() {
        paused = true;
    }

    public static void resumeGame() {
        paused = false;
    }

    public static void stopGame() {
        running = false;
        paused = false;

        teamAIs.clear();

        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }
    }

    /** Utilisé pour le button Pause/resume */
    public static boolean isRunning() { 
        return running; 
    }
    
    /** Utilisé pour le button Pause/resume */
    public static boolean isPaused() { 
        return paused;
    }

    public static GameMap getGameMap() {
        return gameMap;
    }

    private static void handleGameEnd(int winningTeam) {
        stopGame();

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                "Team " + winningTeam + " a gagné !",
                "Fin de la partie",
                JOptionPane.INFORMATION_MESSAGE
            );

            GameWindow.showScreen("MAIN_MENU");
        });
    }
/* 
    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            long delta = now - lastTime;
            if (delta >= 100) { // ~10 FPS
                gameMap.updateAll();
                if (window != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> window.repaint());
                }
                lastTime = now;
            }
        }
    }
        */
}