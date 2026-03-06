package code.model.utils;

import code.model.Cell;
import code.model.celltype.Army;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Vérifie si la map qui a été génerée par MapGenerator est utilisable pour une partie.
 * A date du 10/01/2026, cette classe vérifie si :
 * -  chaque team peut atteindre chaque autre team sur la map. On renvoie un boolean en fonction du résultat.


1. faire un parcours integral for for de la grille, afin de trouver une position (x,y) pour chaque team. 
chaque position est stockée dans positionsOfArmys[][]. positionsOfArmys est initialisé pour stocker nOfTeams tuples de coordonnées.
on dispose également de found[], un tableau de booléens de taille nOfTeams. quand on trouve une army d'id X, on stocke 
ses coordonnées dans positionsOfArmys[X] et on met found[X] à true. comme ça on sait quelles teams ont déjà été localisées.

2. check si on a bien le nb suffisant de teams en regardant si chaque case de found[] vaut true. Sinon indiquer erreur parameters.

3. lancer un parcours largeur sur une de ces army qu'on a trouvé. quand on trouve une army on la retire de la liste. a la fin check si la liste est vide.

 */
public class MapValidator {

    public static boolean checkIfTeamsCanReachEachOthers(Cell[][] cellmap, int nOfTeams) {
        int height = cellmap.length;
        int width = cellmap[0].length;

        // on stocke x et y pour chaque id de team
        int[][] positionsOfArmys = new int[nOfTeams][2];
        boolean[] found = new boolean[nOfTeams];
        int count = 0;

        // 1. scan intégral pour chopper une position par team
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cellmap[y][x] instanceof Army) {
                    int id = ((Army) cellmap[y][x]).getTeamId();
                    if (id >= 0 && id < nOfTeams && !found[id]) {
                        positionsOfArmys[id][0] = x;
                        positionsOfArmys[id][1] = y;
                        found[id] = true;
                        count++;
                    }
                }
                if (count == nOfTeams) break;
            }
            if (count == nOfTeams) break;
        }

        // 2. check si on a bien trouvé le nombre d'armies que la map est censée posséder (même si techniquement useless, c'est pour la robustesse de code)
        if (count < nOfTeams) {
            System.err.println("erreur parameters: teams manquantes sur la map");
            return false;
        }

        // 3. BFS depuis la team 0
        boolean[][] visited = new boolean[height][width];
        boolean[] reached = new boolean[nOfTeams];
        int reachedCount = 0;
        
        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{positionsOfArmys[0][0], positionsOfArmys[0][1]});
        // positionsOfArmys contient le y d'abord car c'est une grille de grilles la map. dans visited, c'est juste une liste de cells x,y donc les coordonnées s'inversent
        visited[positionsOfArmys[0][1]][positionsOfArmys[0][0]] = true; 

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        while (!q.isEmpty()) {
            int[] currentCellCoordinates = q.poll();
            Cell currentCell = cellmap[currentCellCoordinates[1]][currentCellCoordinates[0]];

            if (currentCell instanceof Army) {
                int id = ((Army) currentCell).getTeamId();
                if (id >= 0 && id < nOfTeams && !reached[id]) {
                    reached[id] = true;
                    reachedCount++;
                }
            }

            // si on a reach toutes les teams, on peut stopper le BFS
            if (reachedCount == nOfTeams) return true;

            for (int[] d : directions) {
                int nx = currentCellCoordinates[0] + d[0];
                int ny = currentCellCoordinates[1] + d[1];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height && !visited[ny][nx]) {
                    if (!(cellmap[ny][nx] instanceof code.model.celltype.Wall)) {
                        visited[ny][nx] = true;
                        q.add(new int[]{nx, ny});
                    }
                }
            }
        }

        return reachedCount == nOfTeams;
    }
}