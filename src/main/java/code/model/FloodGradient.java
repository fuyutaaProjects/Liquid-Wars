package code.model;
import code.model.celltype.*;

/**
 * Gradient basé sur l'algorithme de floodfill qui fonctionne
 * de façon récursive
 * Ignore les armées lors du calcul
 */
public class FloodGradient implements Gradient {

    @Override
    public int[][] gradient(GameMap map, int x, int y) {
        Cell[][] cellmap = map.getCellMap();
        int[][] gradmap = new int[cellmap.length][cellmap[0].length];
        gradientrec(cellmap, gradmap, x, y, 1);
        return gradmap;
    }

    private void gradientrec(Cell[][] cellmap, int[][] gradmap, int x, int y, int value) {
        // Vérifier limites
        if (x < 0 || y < 0 || y >= cellmap.length || x >= cellmap[0].length) return;
        // Ne pas écraser une valeur déjà plus petite
        if (gradmap[y][x] != 0 && gradmap[y][x] <= value) return;
        // Si case bloquée (mur ou autre), on stop
        if (!(cellmap[y][x] instanceof Empty || cellmap[y][x] instanceof Army)) return;

        gradmap[y][x] = value;

        // Appel récursif pour les 4 voisins
        gradientrec(cellmap, gradmap, x + 1, y, value + 1);
        gradientrec(cellmap, gradmap, x - 1, y, value + 1);
        gradientrec(cellmap, gradmap, x, y + 1, value + 1);
        gradientrec(cellmap, gradmap, x, y - 1, value + 1);
    }
}