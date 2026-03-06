package code.model;

import java.awt.Color;

public interface Cell{
    public void update(GameMap map);
    public Coord getCoord();
    public void setCoord(Coord coord);
    /** Couleur de la cellule pour son affichage dans MapPanel */
    public Color getDisplayColor();
}
