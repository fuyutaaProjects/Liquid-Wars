package code.view;

import code.model.Cell;
import code.model.GameMap;
import code.model.celltype.*;

/**
 * <pre>
 * Classe servant à donner un rendu dans le terminal
 * de la map passé en paramètre dans le constructeur
 * 
 * Attention une map trop grande horizontalement pourrait
 * être affiché bizarrement et donner un rendu peu lisible
 * si votre terminal est trop petit
 * </pre>
 */
public class TerminalMapRender {
    public static void printmap(GameMap map){
        Cell[][] cellmap = map.getCellMap();
        String row = "";
        for(int y = 0; y < cellmap.length; y++){
            row = "| ";
            for(int x = 0; x < cellmap[0].length; x++){
                if(cellmap == null || cellmap[y] == null || cellmap[y][x] == null){}
                else switch(cellmap[y][x]){
                    case Empty e: row += "  ";break;
                    case Wall w: row += "# ";break;
                    case Army a: row += a.getTeamId() + " ";break;
                    default: row += "E ";//Cas default : Erreur
                }
                row += " ";
            }
            row += "|";
            System.out.println(row);
        }
    }
}
