package code.model.generator;
import code.model.*;
import code.model.celltype.*;

/**
 * Cette interface sert à générer une carte stocké dans le code du jeu, utilisé principalement pour les tests<br>
 * La classe sera probablement retiré lorsque l'on aura assez avancé dans le code pour pouvoir générer des
 * cartes d'une meilleur façon
 */
public final class CodeGenerator implements MapGen{
    @Override
    public void generate(GameMap gameMap){
        Cell[][] cellmap = gameMap.getCellMap();
        for(int y = 0; y < cellmap.length; y++){
            for(int x = 0; x < cellmap[0].length; x++){
                cellmap[y][x] = debugGameMap[y][x];
            }
        }
        gameMap.setCellMap(cellmap);
    }

    private Cell ept(){return new Empty();}
    private Cell wll(){return new Wall();}
    private Cell arm(){return new Army(1, null);}

    //On choisit de garder la carte de debug dans le code sous forme de variable car 
    //cela nous permet d'avoir une carte de test avant d'avoir implémenter le code pour
    //générer une carte de n'importe qu'elle autre façon
    private Cell[][] debugGameMap = {
    {ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), arm(), arm(), arm(), arm(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), arm(), arm(), arm(), arm(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), arm(), arm(), arm(), arm(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), arm(), arm(), arm(), arm(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), arm(), arm(), arm(), arm(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll()},
    {ept(), ept(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept()},
    {ept(), ept(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept()},
    {ept(), ept(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept()},
    {ept(), ept(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()},
    {ept(), ept(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept(), wll(), ept()},
    {ept(), ept(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), wll(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept(), ept()}
};

}
