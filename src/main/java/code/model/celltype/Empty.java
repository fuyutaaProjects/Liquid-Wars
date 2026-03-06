package code.model.celltype;
import code.model.*;
import java.awt.Color;

public class Empty implements Cell{
    Coord coord;
    
    @Override
    public void update(GameMap map){}

    @Override
    public Coord getCoord(){return coord;}

    @Override
    public void setCoord(Coord c){this.coord = c;}

    @Override
    public Color getDisplayColor() { 
        return Color.WHITE;
    }
}
