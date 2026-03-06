package code.model.celltype;

import java.awt.Color;
import java.util.*;

import code.model.*;

public class Army implements Cell {

    int teamId;
    Coord coord;

    int energy = 10;
    int energyMax = 10;
    int lowEnergyLevel  = (int) Math.round(0.2 * energyMax);

    int attack = 1;
    int diviseur = 4;

    Color color = Color.GRAY;

    //variable statique pour gérer les déplacement des cellules
    private static final double COHESION_WEIGHT = 0.9;
    private static final double DIRECTION_WEIGHT = 0.7;
    private static final double GRADIENT_WEIGHT = 1.5;
    private static final int SPLIT_ENERGY_THRESHOLD = 8;

    public Army(int teamId, Coord coord) {
        this.teamId = teamId;
        this.coord = coord;
        color = getColor();
    }

    @Override
    public Color getDisplayColor() {
        return color;
    }

    @Override
    public Coord getCoord() {
        return coord;
    }

    @Override
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public int getTeamId() { return teamId; }
    public int getEnergy() { return energy; }

    /* ========================================================= */
    /* ======================== UPDATE ========================= */
    /* ========================================================= */

    @Override
    public void update(GameMap map) {
        attack = Math.max(Math.min(map.getTeamEnergy(teamId)/100, 8), 1);
        color = getColor();
        
        Coord targetCoord = map.getTeamCursor(teamId);
        boolean isOnCursor = (targetCoord != null &&
                            coord.getX() == targetCoord.getX() &&
                            coord.getY() == targetCoord.getY());

        if (isOnCursor) {
            Coord c = map.getFarArmyCellFromCursorBlob(teamId);
            int maxGradient = map.calculateMaxGradientForBlob(teamId);
            //System.out.println((int) Math.sqrt(map.getTeamEnergy(teamId)));
            Cell cell = map.getCellAt(c);
            if (cell instanceof Army a && a.teamId == teamId) {
                int g = map.getGradientValue(c, teamId);
                if (g > maxGradient) {
                    this.energy += a.energy;
                    a.energy = 0;
                    map.getCellMap()[c.getY()][c.getX()] = new Empty();
                }
            }
            distributeEnergy(map);
            return;
        }

        // =================== DÉPLACEMENT NORMAL ===================
        Coord desiredMove = calculateDesiredMove(map);
        if (desiredMove == null) return;

        Cell targetCell = map.getCellAt(desiredMove);

        if (targetCell == null || targetCell instanceof Empty) {
            moveToCoord(map, desiredMove);
        }
        else if (targetCell instanceof Army targetArmy) {

            if (targetArmy.teamId == this.teamId) {
                if (attemptMerge(targetArmy)) return;
            }
            else {
                attackEnemy(map, targetArmy);
            }
        }

        considerSplitting(map);
        distributeEnergy(map);
    }

    /* ========================================================= */
    /* ====================== MERGE ALLIÉ ====================== */
    /* ========================================================= */

    /**
     * Merge déterministe :
     * - uniquement si les deux sont faibles
     * - la cellule la plus faible disparaît
     * @return true
     */
    private boolean attemptMerge(Army ally) {
        // On ne merge que si ally est plus faible
        if (this.energy >= lowEnergyLevel || ally.energy >= lowEnergyLevel) return false;

        ally.energy += this.energy / 2;
        this.energy = (this.energy + 1) / 2;//On évite de se tuer au cas où
        return true;
    }

    /* ========================================================= */
    /* ===================== DÉPLACEMENT ======================= */
    /* ========================================================= */

    private void moveToCoord(GameMap map, Coord newCoord) {
        map.getCellMap()[coord.getY()][coord.getX()] = new Empty();
        coord = newCoord;
        map.getCellMap()[coord.getY()][coord.getX()] = this;
    }

    /* ========================================================= */
    /* ===================== DISTRIBUTION ====================== */
    /* ========================================================= */

    public void distributeEnergy(GameMap map) {
        if (energy <= energyMax) return;
        if (map.getCellAt(coord) != this) return;

        Queue<Coord> queue = new ArrayDeque<>();
        Set<Coord> visited = new HashSet<>();
        queue.add(coord);
        visited.add(coord);

        int safetyCounter = 0;
        int maxIterations = 50;

        while (energy > energyMax && !queue.isEmpty() && safetyCounter < maxIterations) {
            safetyCounter++;

            Coord current = queue.poll();
            if (current == null) break;

            Cell currentCell = map.getCellAt(current);
            if (!(currentCell instanceof Army)) continue;

            Coord closestAlly = null;
            int minAllyDist = Integer.MAX_VALUE;
            for (Cell neighbour : map.getNeighbour(current)) {
                if (!(neighbour instanceof Army ally)) continue;
                if (ally.teamId != teamId) continue;
                if (visited.contains(ally.coord)) continue;
                if (map.getCellAt(ally.coord) != ally) continue;
                if (ally.energy >= ally.energyMax) continue;

                int dist = Math.abs(coord.getX() - ally.coord.getX()) + Math.abs(coord.getY() - ally.coord.getY());
                if (dist < minAllyDist) {
                    minAllyDist = dist;
                    closestAlly = ally.coord;
                }
            }

            Coord closestEmpty = map.getClosestLowerGradientTypeCell(current, teamId, Empty.class);
            int emptyDist = (closestEmpty != null) ?
                    Math.abs(coord.getX() - closestEmpty.getX()) + Math.abs(coord.getY() - closestEmpty.getY())
                    : Integer.MAX_VALUE;

            if (closestEmpty != null && emptyDist <= minAllyDist) {
                if (map.createNewArmyCell(closestEmpty, teamId)) {
                    Army a = (Army) map.getCellAt(closestEmpty);
                    if (a != null) {
                        a.energy = 1;
                        energy--;
                        queue.add(closestEmpty);
                        visited.add(closestEmpty);
                    }
                }
            } else if (closestAlly != null) {
                Army ally = (Army) map.getCellAt(closestAlly);
                visited.add(ally.coord);
                while (energy > energyMax && ally.energy < ally.energyMax) {
                    ally.energy++;
                    energy--;
                }
                queue.add(ally.coord);
            }
        }
    }

    /* ========================================================= */
    /* ======================== SPLIT ========================== */
    /* ========================================================= */

    private void considerSplitting(GameMap map) {
        if (energy < SPLIT_ENERGY_THRESHOLD) return;

        Coord target = findBestSplitLocation(map);
        if (target != null) performSplit(map, target);
    }

    private void performSplit(GameMap map, Coord newCoord) {
        if (!map.createNewArmyCell(newCoord, teamId)) return;

        Army newArmy = (Army) map.getCellAt(newCoord);
        int half = energy / 2;
        newArmy.energy = half;
        energy -= half;
    }

    /* ========================================================= */
    /* ======================== COMBAT ========================= */
    /* ========================================================= */

    private void attackEnemy(GameMap map, Army enemy) {

        if (enemy.energy < energy / diviseur || map.getTeamEnergy(enemy.getTeamId()) < map.getTeamEnergy(teamId)/3) {

            int stolen = enemy.energy;
            Coord enemyCoord = enemy.coord;
            Coord oldCoord = coord;

            map.getCellMap()[enemyCoord.getY()][enemyCoord.getX()] = new Empty();
            coord = enemyCoord;
            map.getCellMap()[coord.getY()][coord.getX()] = this;

            energy += stolen;

            Army back = new Army(teamId, oldCoord);
            back.energy = 1;
            map.getCellMap()[oldCoord.getY()][oldCoord.getX()] = back;
            energy--;

        } else {

            int stolen = Math.min(attack, enemy.energy);
            enemy.energy -= stolen;
            energy += stolen;

            if (enemy.energy <= 0) {
                Coord c = enemy.coord;
                map.getCellMap()[c.getY()][c.getX()] = new Empty();
                moveToCoord(map, c);
            }
        }
    }

    private Coord calculateDesiredMove(GameMap map) {
        int allyCount = countAllyNeighbours(map, this.coord);
        Coord targetCoord = map.getTeamCursor(teamId);

        // =================== VÉRIFICATION SI ON EST SUR LE CURSEUR ===================
        boolean isOnCursor = (targetCoord != null && 
                            coord.getX() == targetCoord.getX() && 
                            coord.getY() == targetCoord.getY());
        
        if (isOnCursor) {
            return null;
        }

        // =================== VÉRIFICATION SI UN ALLIÉ EST SUR LE CURSEUR ===================
        boolean allyOnCursor = false;
        if (targetCoord != null) {
            Cell cursorCell = map.getCellAt(targetCoord);
            if (cursorCell instanceof Army a && a.teamId == this.teamId) {
                allyOnCursor = true;
            }
        }

        // =================== MODE SIMPLE POUR CAS PARTICULIER ===================
        int wallCount = countAdjacentWalls(map);
        if (wallCount > 0 || allyOnCursor) {
            return moveToLowestGradient(map);
        }

        // =================== RÈGLE ABSOLUE ===================
        if (allyCount <= 2) {
            if (targetCoord != null) {
                Coord move = map.getLowerGradientEmptyCell(coord, teamId);
                if (move != null) return move;
            }
        }

        // =================== CALCUL VECTEURS ===================
        Coord centerOfMass = map.getArmyCenterOfMass(teamId);
        if (centerOfMass == null && targetCoord == null) return null;

        double cohesionX = 0, cohesionY = 0;
        if (centerOfMass != null) {
            cohesionX = centerOfMass.getX() - coord.getX();
            cohesionY = centerOfMass.getY() - coord.getY();
        }

        double dirX = 0, dirY = 0;
        if (targetCoord != null) {
            dirX = targetCoord.getX() - coord.getX();
            dirY = targetCoord.getY() - coord.getY();
        }

        double gradX = 0, gradY = 0;
        Coord lower = map.getLowerGradientArmyCell(coord, teamId);
        if (lower != null) {
            gradX = coord.getX() - lower.getX();
            gradY = coord.getY() - lower.getY();
        }

        // =================== POIDS ===================
        double cohesionWeight = COHESION_WEIGHT * (allyCount / 1.0);
        double directionWeight = DIRECTION_WEIGHT;
        double gradientWeight = GRADIENT_WEIGHT * 1.3;

        // =================== SOMME ===================
        double finalX =
                cohesionX * cohesionWeight +
                dirX * directionWeight +
                gradX * gradientWeight;

        double finalY =
                cohesionY * cohesionWeight +
                dirY * directionWeight +
                gradY * gradientWeight;

        // =================== DERNIÈRE SÉCURITÉ ===================
        if (Math.abs(finalX) < 0.1 && Math.abs(finalY) < 0.1) {
            if (targetCoord != null && !allyOnCursor) {
                return moveTowardTarget(map);
            }
            return null;
        }

        // =================== NORMALISATION ===================
        int moveX = 0, moveY = 0;
        if (Math.abs(finalX) >= 0.5) moveX = finalX > 0 ? 1 : -1;
        if (Math.abs(finalY) >= 0.5) moveY = finalY > 0 ? 1 : -1;

        // =================== SÉCURITÉ TERRAIN ===================
        Coord newCoord = new Coord(coord.getX() + moveX, coord.getY() + moveY);
        if (!map.isValidCoord(newCoord)) {
            if (moveX != 0 && map.isValidCoord(new Coord(coord.getX() + moveX, coord.getY())))
                newCoord = new Coord(coord.getX() + moveX, coord.getY());
            else if (moveY != 0 && map.isValidCoord(new Coord(coord.getX(), coord.getY() + moveY)))
                newCoord = new Coord(coord.getX(), coord.getY() + moveY);
            else
                return null;
        }

        return newCoord;
    }

    /**
     * Compte le nombre de murs adjacents
     */
    private int countAdjacentWalls(GameMap map) {
        int count = 0;
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        
        for (int[] dir : directions) {
            int nx = coord.getX() + dir[0];
            int ny = coord.getY() + dir[1];
            
            if (!map.inBounds(nx, ny) || map.getCellAt(new Coord(nx, ny)) instanceof Wall) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * Bouge vers la case adjacente avec le gradient le plus bas
     */
    private Coord moveToLowestGradient(GameMap map) {
        int[][] gradmap = map.getTeamGradmap(teamId);
        if (gradmap == null) return null;
        
        int[][] directions = {
            {0, -1}, {1, 0}, {0, 1}, {-1, 0}
        };
        
        Coord bestMove = coord;
        int lowestGradient = map.getGradientValue(coord, teamId);
        
        for (int[] dir : directions) {
            int nx = coord.getX() + dir[0];
            int ny = coord.getY() + dir[1];
            
            if (!map.inBounds(nx, ny)) continue;
            
            Coord testCoord = new Coord(nx, ny);
            Cell cell = map.getCellAt(testCoord);
            
            // Accepter cases vides ou alliés
            if (!(cell instanceof Empty || (cell instanceof Army a && a.teamId == teamId))) {
                continue;
            }
            
            int gradient = gradmap[ny][nx];
            if (gradient < lowestGradient) {
                lowestGradient = gradient;
                bestMove = testCoord;
            }
        }
        
        return bestMove;
    }

    /* ========================================================= */
    /* =================== AUTRES MÉTHODES ===================== */
    /* ========================================================= */

    private Coord moveTowardTarget(GameMap map) {
        Coord targetCoord = map.getTeamCursor(this.teamId);
        if (targetCoord == null) return null;

        int dx = targetCoord.getX() - coord.getX();
        int dy = targetCoord.getY() - coord.getY();

        // Déjà exactement sur la cible
        if (dx == 0 && dy == 0) return null;

        int moveX = 0;
        int moveY = 0;

        // Choix de l'axe dominant (déterministe)
        if (Math.abs(dx) > Math.abs(dy)) {
            moveX = dx > 0 ? 1 : -1;
        } else if (dy != 0) {
            moveY = dy > 0 ? 1 : -1;
        }

        Coord newCoord = new Coord(coord.getX() + moveX, coord.getY() + moveY);

        // Sécurité terrain
        if (!map.isValidCoord(newCoord)) return null;

        return newCoord;
    }

    /* ===================== CHOIX DE CASE POUR SPLIT ===================== */

    private Coord findBestSplitLocation(GameMap map) {
        Cell[] neighbours = map.getNeighbour(coord);
        Coord[] neighbourCoords = map.getNeighbourCoords(coord);

        Coord bestGap = null;
        Coord bestFront = null;

        for (int i = 0; i < neighbours.length; i++) {
            Cell n = neighbours[i];
            if(i >= neighbourCoords.length) continue;
            Coord nc = neighbourCoords[i];

            if (n == null || n instanceof Empty) {
                int allyNeighbours = countAllyNeighbours(map, nc);

                // Priorité 1 : remplir gap
                if (allyNeighbours >= 3) bestGap = nc;

                // Priorité 2 : renforcer front
                if (bestFront == null && allyNeighbours >= 2) {
                    Coord target = map.getTeamCursor(teamId);
                    if (target != null && isCloserToTarget(nc, target)) bestFront = nc;
                }
            }
        }

        return bestGap != null ? bestGap : bestFront;
    }

    private boolean isCloserToTarget(Coord c, Coord target) {
        double currDist = Math.hypot(coord.getX() - target.getX(), coord.getY() - target.getY());
        double newDist  = Math.hypot(c.getX() - target.getX(), c.getY() - target.getY());
        return newDist < currDist;
    }

    private int countAllyNeighbours(GameMap map, Coord c) {
        int count = 0;
        for (Cell cell : map.getNeighbour(c)) {
            if (cell instanceof Army a && a.teamId == teamId) count++;
        }
        return count;
    }

    public Coord getLowerGradientArmyCell(GameMap map) {
        return map.getLowerGradientArmyCell(coord, teamId);
    }

    private Color getColor(){
        Color[] baseColors = {
            Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
        };

        Color base = teamId < baseColors.length ? baseColors[teamId] : Color.GRAY;

        float[] hsb = Color.RGBtoHSB(
            base.getRed(),
            base.getGreen(),
            base.getBlue(),
            null
        );

        float minBrightness = 0.25f;
        float maxBrightness = 0.95f;

        float t = energyMax > 0
            ? Math.max(0f, Math.min(1f, (float) energy / energyMax))
            : 0f;

        float brightness = minBrightness + t * (maxBrightness - minBrightness);

        Color rgb = Color.getHSBColor(hsb[0], hsb[1], brightness);

        int r = clamp(rgb.getRed(),   30, 220);
        int g = clamp(rgb.getGreen(), 30, 220);
        int b = clamp(rgb.getBlue(),  30, 220);

        return new Color(r, g, b);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}