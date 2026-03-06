package code.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

import code.model.celltype.*;
import javafx.scene.paint.Color;

/** 
 * GameMap.java :<br>
 * Classe utilisé dans la gestion de la carte de jeu coté modèle
 * Version optimisée avec multithreading pour le calcul des gradients
 */
public class GameMap {
    private Cell[][] map = {{new Empty()}};
    private List<Integer> teamIds = new ArrayList<>();
    private final Map<Integer, Coord> teamCursors = new HashMap<>();
    private final Map<Integer, int[][]> gradmapsByTeam = new HashMap<>();
    private final Gradient gradientCalculator = new FloodGradient();
    
    // ExecutorService pour le multithreading
    private final ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 4;

    public static final int tolerance_gradient = 1;

    final int[] dx = {0, 1, 0, -1};
    final int[] dy = {-1, 0, 1, 0};

    public GameMap(int x, int y){
        map = new Cell[y][x];

        for(int l = 0; l < y; l++){ // pour chaque ligne
            for(int c = 0; c < x; c++){
                map[l][c] = new Empty();
            }
        }
        
        // Initialisation de l'ExecutorService
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    //======================== Setter n' Getters ========================

    public Cell[][] getCellMap(){return map;}
    public Cell getCellAt(int x, int y){return map[y][x];}
    public Cell getCellAt(Coord coord) {
        if (!isValidCoord(coord)) return null;
        return map[coord.getY()][coord.getX()];
    }

    public void setCellMap(Cell[][] map){this.map = map;}

    public List<Integer> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(List<Integer> teamIds) {
        this.teamIds = teamIds;
    }


    public int[][] getTeamGradmap(int teamId) {
        return gradmapsByTeam.get(teamId);
    }

    /**
     * Définit la position du curseur pour une équipe donnée
     */
    public void setTeamCursor(int teamId, Coord cursor) {
        teamCursors.put(teamId, cursor);
    }

    /**
     * Récupère la position du curseur pour une équipe donnée
     */
    public Coord getTeamCursor(int teamId) {
        return teamCursors.get(teamId);
    }

    /**
     * Renvoie la valeur du gradient pour la cellule donnée et l'équipe correspondante.
     * Retourne Integer.MAX_VALUE si la cellule est invalide ou si le gradient n'existe pas.
     */
    public int getGradientValue(Coord coord, int teamId) {
        if (!isValidCoord(coord)) return Integer.MAX_VALUE;
        int[][] gradmap = gradmapsByTeam.get(teamId);
        if (gradmap == null) return Integer.MAX_VALUE;

        if (coord.getY() < gradmap.length && coord.getX() < gradmap[0].length) {
            return gradmap[coord.getY()][coord.getX()];
        }

        // Si aucune map de gradient n'a été trouvée pour cette coordonnée
        // Cela facilite certains comparaisons
        return Integer.MAX_VALUE;
    }

    //============================= Méthodes ============================

    /**
     * Met à jour toutes les cellules existantes - VERSION OPTIMISÉE AVEC MULTITHREADING
     * - Les calculs de gradient sont effectués en parallèle
     * - Les équipes agissent dans un ordre aléatoire
     * - Les cellules les plus proches du curseur sont prioritaires
     * - Les nouvelles cellules créées attendent la prochaine update
     */
    public void updateAll() {
        refreshArmyCoordinates();

        Map<Integer, List<Army>> armiesByTeam = getAllArmiesByTeam();
        List<Integer> teamIds = new ArrayList<>(armiesByTeam.keySet());
        Collections.shuffle(teamIds);

        // PHASE 1 : CALCUL DES GRADIENTS EN PARALLÈLE
        List<Callable<GradientResult>> gradientTasks = new ArrayList<>();
        
        for (int teamId : teamIds) {
            // Curseur de la team
            Coord cursor = teamCursors.get(teamId);
            if (cursor == null) {
                cursor = getRandomEmptyCoord();
                if (cursor != null) {
                    teamCursors.put(teamId, cursor);
                }
            }

            if (cursor == null) continue;

            // Création d'une tâche pour calculer le gradient
            final Coord finalCursor = cursor;
            final int finalTeamId = teamId;
            
            Callable<GradientResult> task = () -> {
                int[][] gradmap = gradientCalculator.gradient(
                    this,
                    finalCursor.getX(),
                    finalCursor.getY()
                );
                return new GradientResult(finalTeamId, gradmap);
            };
            
            gradientTasks.add(task);
        }

        // Exécution parallèle de tous les calculs de gradient
        try {
            List<Future<GradientResult>> futures = executorService.invokeAll(gradientTasks);
            
            // Récupération des résultats et stockage dans gradmapsByTeam
            for (Future<GradientResult> future : futures) {
                GradientResult result = future.get();
                gradmapsByTeam.put(result.teamId, result.gradmap);
            }
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Erreur lors du calcul parallèle des gradients : " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, on continue avec les gradmaps existantes
        }

        // PHASE 2 : DÉPLACEMENT DES ARMÉES (SÉQUENTIEL POUR ÉVITER LES PROBLÈMES DE CONCURRENCE)
        for (int teamId : teamIds) {
            int[][] gradmap = gradmapsByTeam.get(teamId);
            if (gradmap == null) continue;

            // Récupération des armées dans l'ordre de priorité
            List<Army> orderedArmies = getOrderedArmiesByGradmap(teamId, gradmap);

            // Update séquentielle pour éviter les conflits d'écriture
            for (Army army : orderedArmies) {
                army.update(this);
            }
        }
    }

    /**
     * Renvoie l'énergie totale d'une équipe
     * @param teamId id de la team traité
     */
    public int getTeamEnergy(int teamId){
        int teamEnergy = 0;
        for(int y = 0; y < map.length; y++){
            for(int x = 0; x < map[0].length; x++){
                if(getCellAt(x,y) instanceof Army a && a.getTeamId() == teamId){
                    teamEnergy += a.getEnergy();
                }
            }
        }
        return teamEnergy;
    }

    /**
     * Classe interne pour stocker le résultat du calcul de gradient
     */
    private static class GradientResult {
        final int teamId;
        final int[][] gradmap;
        
        GradientResult(int teamId, int[][] gradmap) {
            this.teamId = teamId;
            this.gradmap = gradmap;
        }
    }

    /**
     * Méthode à appeler lors de la fermeture de l'application
     * pour libérer proprement les ressources du thread pool
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Commande qui renvoie un boolean indiquant si une case
     * est dans les limites de la map
     * @param x
     * @param y
     * @return
     */
    public boolean inBounds(int x, int y) {
        return y >= 0 && y < map.length
            && x >= 0 && x < map[0].length;
    }

    /**
     * Méthode qui permet de générer la carte de jeu, que ce soit avec un fichier
     * ou de façon aléatoires ou autre
     * 
     * Voir la classe MapGen pour l'explication du fonctionnement de la génération
     * @param gen
     */
    public void generate(MapGen mapGenerator){
        mapGenerator.generate(this);

        //On genère un curseur aléatoire pour les tests
        teamCursors.clear();
        for (int teamId = 0; teamId < teamIds.size(); teamId++) {
            if (teamId == 1) continue;
            
            Coord cursor = getRandomEmptyCoord();
            if (cursor != null) {
                teamCursors.put(teamId, cursor);
            }
        }
    }

    public Cell[] getNeighbour(Coord cellcord) {
        Cell[] neighbour = new Cell[4];

        int x = cellcord.getX();
        int y = cellcord.getY();

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && ny >= 0 && nx < map[0].length && ny < map.length) {
                neighbour[i] = map[ny][nx];
            }
        }

        return neighbour;
    }

    /**
     * Renvoie le nombre de cases armées voisines dans un carré de 3x3 autour 
     * des coordonnées passées en paramètres, sans compter la case située aux coordonnées.
     */
    public int getNeighbourNum(Coord coord, int teamId) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue; // ignorer la case centrale
                int nx = coord.getX() + dx;
                int ny = coord.getY() + dy;
                if (!inBounds(nx, ny)) continue;
                Cell c = map[ny][nx];
                if (c instanceof Army a && a.getTeamId() == teamId) count++;
            }
        }
        return count;
    }

    /**
     * Renvoie les coordonnées de la cellule armée voisine ALLIÉE
     * ayant le plus petit gradient autour d'une coordonnée donnée.
     * Retourne null si aucune cellule alliée avec gradient plus bas n'existe.
     */
    public Coord getLowerGradientArmyCell(Coord center, int teamId) {
        List<Coord> candidates =
            getAcceptableGradientCells(center, teamId, false, true);

        if (candidates.isEmpty()) return null;

        Coord best = center;
        int bestGrad = getGradientValue(best, teamId);
        int[][] gradMap = gradmapsByTeam.get(teamId);
        for (Coord c : candidates) {
            int g = gradMap[c.getY()][c.getX()];
            if (g < bestGrad) {
                bestGrad = g;
                best = c;
            }
        }
        return best;
    }

    /**
     * Renvoie les coordonnées de la cellule EMPTY voisine
     * ayant le plus petit gradient autour d'une coordonnée donnée.
     * Retourne null si aucune cellule vide avec gradient plus bas n'existe.
     */
    public Coord getLowerGradientEmptyCell(Coord center, int teamId){
        List<Coord> candidates =
            getAcceptableGradientCells(center, teamId, true, false);

        if (candidates.isEmpty()) return null;

        // ne garder que les cases avec au moins 2 armées autour
        List<Coord> safeCandidates = new ArrayList<>();
        for (Coord c : candidates) {
            if (getNeighbourNum(c,teamId) >= 2) {
                safeCandidates.add(c);
            }
        }

        if (safeCandidates.isEmpty()) return null;

        // retourner la case avec le gradient le plus bas parmi les safe candidates
        int[][] gradMap = gradmapsByTeam.get(teamId);
        Coord best = center;
        int bestGrad = getGradientValue(center,teamId);
        for (Coord c : safeCandidates) {
            int g = gradMap[c.getY()][c.getX()];
            if (g < bestGrad) {
                bestGrad = g;
                best = c;
            }
        }
        return best;
    }

    /**
     * Renvoie les coordonnées de la cellule de type donné voisine
     * ayant le plus petit gradient autour d'une coordonnée donnée.
     * On ignore évidemment la cellule de départ
     * Retourne null si aucune cellule vide avec gradient plus bas n'existe.
     * @param center coordonnées du point à partir duquel on vas chercher
     * @param teamId utile uniquement si on cherche des armés ignoré autrement
     * @param type type de cellule que l'on cherche (Empty.class par exemple) 
     * On peux utiliser plusieurs types suivis à la chaine à la fin si on
     * cherche par exemple la cellule Army ou Empty la plus proche
     */
    @SafeVarargs //Sert à éviter un warning sur la liste de générique qui contient les types (Java aime pas trop ça)
    public final Coord getClosestLowerGradientTypeCell(Coord center, int teamId, Class<? extends Cell>... types){
        int[][] gradMap = gradmapsByTeam.get(teamId);
        if (gradMap == null || center == null) return null;
        int maxRadius = 50;
        
        for (int radius = 1; radius <= maxRadius; radius++) {
            List<Coord> candidatesAtRadius = new ArrayList<>();
            
            // Parcourir toutes les cases à cette distance
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    // Vérifier qu'on est bien sur le périmètre du cercle actuel
                    int dist = Math.abs(dx) + Math.abs(dy);
                    if (dist != radius && dist != radius + 1) continue;
                    
                    int x = center.getX() + dx;
                    int y = center.getY() + dy;
                    
                    if (!inBounds(x, y)) continue;
                    
                    Coord testCoord = new Coord(x, y);
                    Cell cell = map[y][x];
                    
                    // Doit être vide
                    for(Class<? extends Cell> t : types){
                        if(t.isInstance(cell)){
                            if (cell instanceof Army) continue;
                            
                            candidatesAtRadius.add(testCoord);
                        }
                    }
                }
            }
            
            // Si on a trouvé des candidats à ce rayon, choisir le meilleur
            if (!candidatesAtRadius.isEmpty()) {
                // Trouver le gradient minimum
                int minGradient = Integer.MAX_VALUE;
                for (Coord c : candidatesAtRadius) {
                    int g = gradMap[c.getY()][c.getX()];
                    if (g < minGradient) {
                        minGradient = g;
                    }
                }
                
                // Garder tous les candidats avec le gradient minimum
                List<Coord> bestCandidates = new ArrayList<>();
                for (Coord c : candidatesAtRadius) {
                    int g = gradMap[c.getY()][c.getX()];
                    if (g == minGradient) {
                        bestCandidates.add(c);
                    }
                }
                
                // Choisir aléatoirement parmi les meilleurs
                if (!bestCandidates.isEmpty() && getGradientValue(bestCandidates.get(0), teamId) <= radius+1) {
                    Collections.shuffle(bestCandidates);
                    return bestCandidates.get(0);
                }
            }
        }
        
        return null;
    }

    /**
     * Crée une nouvelle cellule Army aux coordonnées spécifiées
     * si la case est vide.
     * @return true si la création a eu lieu, false sinon
     */
    public boolean createNewArmyCell(Coord coord, int teamId) {

        int x = coord.getX();
        int y = coord.getY();

        // vérification des limites
        if (y < 0 || y >= map.length || x < 0 || x >= map[0].length) {
            return false;
        }

        // la case doit être vide (Empty ou null)
        Cell currentCell = map[y][x];
        if (!(currentCell instanceof Empty) && currentCell != null) {
            return false;
        }

        // création et placement de la cellule Army
        map[y][x] = new Army(teamId, coord);

        return true;
    }

    public boolean hasAdjacentAlly(Coord coord, int teamId) {
        Cell[] neighbours = getNeighbour(coord);
        for (Cell c : neighbours) {
            if (c instanceof Army a && a.getTeamId() == teamId) {
                return true;
            }
        }
        return false;
    }

    public List<Coord> getAcceptableGradientCells(Coord center,int teamId,boolean onlyEmpty,boolean onlyArmy){
        int[][] gradMap = gradmapsByTeam.get(teamId);
        if (center == null || gradMap == null) return List.of();

        int[][] decal = {{0,-1},{0,1},{-1,0},{1,0}};

        int bestGradient = Integer.MAX_VALUE;

        for (int[] d : decal) {
            int nx = center.getX() + d[0];
            int ny = center.getY() + d[1];
            if (!inBounds(nx, ny)) continue;

            Cell c = map[ny][nx];
            if (onlyEmpty && !(c instanceof Empty)) continue;
            if (onlyArmy && !(c instanceof Army)) continue;

            bestGradient = Math.min(bestGradient, gradMap[ny][nx]);
        }

        if (bestGradient == Integer.MAX_VALUE) return List.of();

        int limit = bestGradient + tolerance_gradient;

        List<Coord> result = new ArrayList<>();
        for (int[] d : decal) {
            int nx = center.getX() + d[0];
            int ny = center.getY() + d[1];
            if (!inBounds(nx, ny)) continue;

            Cell c = map[ny][nx];
            if (onlyEmpty && !(c instanceof Empty)) continue;
            if (onlyArmy && !(c instanceof Army)) continue;

            if (gradMap[ny][nx] <= limit) {
                result.add(new Coord(nx, ny));
            }
        }

        return result;
    }

    /**
     * Calcule le centre de masse de toutes les cellules d'une équipe
     * Si au moins une cellule est sur le curseur de l'équipe, le centre de masse devient le curseur
     */
    public Coord getArmyCenterOfMass(int teamId) {
        int totalX = 0;
        int totalY = 0;
        int count = 0;

        Coord teamCursor = getTeamCursor(teamId);

        boolean onCursor = false;

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                Cell cell = map[y][x];
                if (cell instanceof Army army && army.getTeamId() == teamId) {
                    
                    // Si une cellule est exactement sur le curseur
                    if (teamCursor != null && x == teamCursor.getX() && y == teamCursor.getY()) {
                        onCursor = true;
                    }

                    totalX += x;
                    totalY += y;
                    count++;
                }
            }
        }

        if (count == 0) return null;

        // Si une cellule est sur le curseur, on force le centre de masse sur le curseur
        if (onCursor && teamCursor != null) {
            return new Coord(teamCursor.getX(), teamCursor.getY());
        }

        return new Coord(totalX / count, totalY / count);
    }

    /**
     * Vérifie si une coordonnée est valide (dans les limites de la carte)
     */
    public boolean isValidCoord(Coord coord) {
        return coord.getX() >= 0 && coord.getX() < map[0].length &&
            coord.getY() >= 0 && coord.getY() < map.length;
    }

    /**
     * Retourne les coordonnées des voisins
     */
    public Coord[] getNeighbourCoords(Coord coord) {
        int[][] offsets = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1,  0},          {1,  0},
            {-1,  1}, {0,  1}, {1,  1}
        };
        
        List<Coord> validCoords = new ArrayList<>();
        
        for (int[] offset : offsets) {
            Coord newCoord = new Coord(coord.getX() + offset[0], coord.getY() + offset[1]);
            if (isValidCoord(newCoord)) {
                validCoords.add(newCoord);
            }
        }
        
        return validCoords.toArray(new Coord[0]);
    }

    /**
     * Renvoie les coordonnées de la cellule d'un seul blob d'armées
     * avec la gradient le plus bas et en partant du curseur
     * (sert au regroupement des cellules autour du curseur une fois atteint)
     */
    public Coord getFarArmyCellFromCursorBlob(int teamID) {
        Coord teamCursor = teamCursors.get(teamID);
        if (teamCursor == null) return null;

        Cell startCell = getCellAt(teamCursor);
        if (!(startCell instanceof Army)) return null;
        ArrayList<Coord> connected = getConnectedArmyBlob(teamCursor, teamID);

        if (connected.isEmpty()) return null;

        // Cherche la cellule la plus éloignée du curseur
        Army farthest = null;
        double maxDist = -1;

        for (Coord c : connected) {
            Army a = (Army) getCellAt(c);
            double dist = Math.hypot(a.getCoord().getX() - teamCursor.getX(), a.getCoord().getY() - teamCursor.getY());
            if (dist > maxDist) {
                maxDist = dist;
                farthest = a;
            }
        }

        return farthest != null ? farthest.getCoord() : null;
    }

    /**
     * Renvoie la liste des cellules connectées à partir d'un curseur
     */
    public ArrayList<Coord> getConnectedArmyBlob(Coord start, int teamID) {
        ArrayList<Coord> visited = new ArrayList<>();
        Queue<Coord> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Coord c = queue.poll();
            for (Cell neighbor : getNeighbour(c)) {
                if (neighbor == null) continue;
                Coord nc = neighbor.getCoord();
                if (!visited.contains(nc) && neighbor instanceof Army a && a.getTeamId() == teamID) {
                    visited.add(nc);
                    queue.add(nc);
                }
            }
        }
        return visited;
    }

    /**
     * Renvoie le nombre de cases à une distance donné depuis une coordonnée
     * Ne s'arrête pas avec les armées, seules les Walls vont le bloqués
     * Aussi le centre sera compté comme étant de distance 1
     * @param c Coordonné du centre
     * @param uwu distance jusqu'à laquelle on compte
     */
    public int getTilesNumberAtDistance(Coord c, int uwu){
        if (c == null || uwu < 1) return 0;
        Set<Coord> treated = new HashSet<>();
        return aux_getTilesNumberAtDistance(c,uwu,treated);
    }

    private int aux_getTilesNumberAtDistance(Coord c, int uwu, Set<Coord> s){
        if(uwu == 1) return 1;
        int owo = 1;
        for(int i = 0; i < 4; i++){
            Coord new_c = new Coord(c.getX()+dx[i], c.getY()+dy[i]);
            owo += aux_getTilesNumberAtDistance(new_c, uwu-1, s);
        }
        return owo;
    }

    /**
     * Calcule le gradient maximal autour du curseur nécessaire
     * pour englober toutes les cellules d'une armée.
     * Utilise une BFS pour compter combien de cellules tombent dans chaque gradient.
     */
    public int calculateMaxGradientForBlob(int teamID) {
        Coord cursor = teamCursors.get(teamID);
        if (cursor == null) return 0;

        ArrayList<Coord> blob = getConnectedArmyBlob(cursor, teamID);
        if (blob.isEmpty()) return 0;
        int blob_size = blob.size();

        int gradient = 1;
        while(getTilesNumberAtDistance(cursor, gradient) < blob_size) gradient++;
        return gradient;
    }

    //================ Fonctions pour l'update =================
    /**
     * Met à jour les coordonnées internes de chaque cellule Army
     * pour qu'elles correspondent à leur position réelle dans la map.
     * Ne sert que en tant que précaution
     */
    public void refreshArmyCoordinates() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                Cell c = map[y][x];
                if (c instanceof Army army) {
                    army.setCoord(new Coord(x, y));
                }
            }
        }
    }

    /**
     * Récupère toutes les armées existantes avant l'update, triées par team
     */
    public Map<Integer, List<Army>> getAllArmiesByTeam() {
        Map<Integer, List<Army>> armiesByTeam = new HashMap<>();
        for(int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                Cell c = map[y][x];
                if (c instanceof Army army) {
                    armiesByTeam.computeIfAbsent(army.getTeamId(), k -> new ArrayList<>()).add(army);
                }
            }
        }
        return armiesByTeam;
    }

    public List<Army> getOrderedArmiesByGradmap(int teamId, int[][] gradmap) {
        List<Army> ordered = new ArrayList<>();

        // On cherche la valeur max du gradient
        int maxGrad = 0;
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (gradmap[y][x] > maxGrad) {
                    maxGrad = gradmap[y][x];
                }
            }
        }

        // Pour chaque valeur de gradient, on parcourt la map
        for (int g = 0; g <= maxGrad; g++) {
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map[0].length; x++) {

                    if (gradmap[y][x] != g) continue;

                    Cell c = map[y][x];
                    if (c instanceof Army army && army.getTeamId() == teamId) {
                        ordered.add(army);
                    }
                }
            }
        }
        return ordered;
    }

    public boolean isTeamAlive(int teamId) {
        return getTeamEnergy(teamId) > 0;
    }

    //======== Function Test Temporaire ========
    private Coord getRandomEmptyCoord() {
        List<Coord> emptyCells = new ArrayList<>();
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map[y][x] instanceof Empty || map[y][x] == null) {
                    emptyCells.add(new Coord(x, y));
                }
            }
        }

        if (emptyCells.isEmpty()) return null;

        Collections.shuffle(emptyCells);
        return emptyCells.get(0);
    }

    //======== Affichage ========

    /**
     * Renvoie l'id de l'équipe gagnante (seule avec de l'énergie),
     * ou -1 s'il n'y a pas de gagnant unique.
     */
    public int getWinningTeamId() {
        int aliveTeam = -1;

        for (int teamId : teamIds) {
            if (getTeamEnergy(teamId) > 0) {

                // première équipe encore en vie
                if (aliveTeam == -1) {
                    aliveTeam = teamId;
                } 
                // une deuxième équipe est encore en vie → pas de gagnant
                else {
                    return -1;
                }
            }
        }

        return aliveTeam;
    }

    // Renvoie un map teamId -> énergie pour toutes les équipes qui ont au moins une cellule
    public Map<Integer, Integer> getTeamsEnergyMap() {
        Map<Integer, Integer> energyByTeam = new HashMap<>();
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                Cell c = map[y][x];
                if (c instanceof Army a) {
                    energyByTeam.merge(a.getTeamId(), a.getEnergy(), Integer::sum);
                }
            }
        }
        return energyByTeam;
    }

    // Renvoie les IDs de toutes les équipes présentes dans la map (au moins une cellule)
    public Set<Integer> getTeams() {
        return getTeamsEnergyMap().keySet();
    }

    // Retourne la couleur d'une équipe (hardcodé ici, mais tu peux le changer)
    public Color getTeamColor(int teamId) {
        return switch(teamId) {
            case 0 -> Color.BLUE;
            case 1 -> Color.RED;
            case 2 -> Color.GREEN;
            case 3 -> Color.YELLOW;
            case 4 -> Color.MAGENTA;
            case 5 -> Color.CYAN;
            case 6 -> Color.ORANGE;
            case 7 -> Color.PINK;
            default -> Color.GRAY;
        };
    }

    // Renvoie l'énergie maximale de toutes les équipes (pour normaliser les barres)
    public int getMaxTeamEnergy() {
        int maxEnergy = 1;
        for (int e : getTeamsEnergyMap().values()) {
            maxEnergy = Math.max(maxEnergy, e);
        }
        return maxEnergy;
    }
}