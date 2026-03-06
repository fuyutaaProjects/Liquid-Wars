package code.model.celltype;
import code.model.*;
import java.awt.Color;

public class Wall implements Cell{
    Coord coord;
    @Override
    public void update(GameMap map){}

    @Override
    public Coord getCoord(){
        return coord;
    }

    @Override
    public void setCoord(Coord coord){
        this.coord = coord;
    }

    @Override
    public Color getDisplayColor() {
        return Color.BLACK;
    }
}