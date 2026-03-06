package code.model;

/**
 * Interface pour les classes de calcul du gradient
 */
public interface Gradient {
    public int[][] gradient(GameMap map, int x, int y);
}
