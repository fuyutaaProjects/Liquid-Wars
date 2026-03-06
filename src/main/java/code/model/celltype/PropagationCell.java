package code.model.celltype;

import code.model.Cell;

/**
 * Sert à la propagation de Armies / Walls dans MapGenerator. Logique expliquée au début de MapGenerator.
 * Stocke les coordonnées dans la cellmap (x, y), une valeur de propagation qui va servit au random qui détermine si elle spread ou non, et la cellule qu'elle place.
 */
public class PropagationCell {
    public final int x;
    public final int y;
    public final double propagation;
    public final Cell cell;
    
    public PropagationCell(int x, int y, double propagation, Cell cell) {
        this.x = x;
        this.y = y;
        this.propagation = propagation;
        this.cell = cell;
    }
}