package code.model.generator;

import code.model.GameMap;
import code.model.Cell;
import code.model.Coord;
import code.model.MapGen;
import code.model.celltype.Army;
import code.model.celltype.Empty;
import code.model.celltype.PropagationCell;
import code.model.celltype.Wall;
import code.model.utils.MapValidator; // Import du validateur
import java.util.*;

/**
 * Générateur de map. Il contient une configuration de base pour une partie.
 * Une instance par configuration de paramètres, non modifiables.
 */
public class MapGenerator implements MapGen {
    // Valeurs par défaut si non fournies au builder
    public static final int DEFAULT_N_OF_TEAMS = 2;
    public static final int DEFAULT_PARTICLES_PER_TEAM = 20;
    public static final double DEFAULT_PROPAGATION_DECAY_FACTOR = 0.85;
    public static final double DEFAULT_INITIAL_PROPAGATION_FACTOR = 0.75;
    public static final int DEFAULT_FREQUENCY_OF_WALL_SPOTS = 250;

    // Valeurs données au builder
    private final Random random;
    private final List<Integer> teamIds;
    private final int particlesPerTeam;
    private final double propagationDecayFactor;
    private final double initialPropagationFactor;
    private final int frequencyOfWallSpots;

    private MapGenerator(Builder builder) {
        this.random = new Random();
        this.teamIds = new ArrayList<>();
        for (int i = 0; i < builder.nOfTeams; i++) {
            this.teamIds.add(i);
        }
        this.particlesPerTeam = builder.particlesPerTeam;
        this.propagationDecayFactor = builder.propagationDecayFactor;
        this.initialPropagationFactor = builder.initialPropagationFactor;
        this.frequencyOfWallSpots = builder.frequencyOfWallSpots;
    }

    public static class Builder {
        private int nOfTeams = DEFAULT_N_OF_TEAMS;
        private int particlesPerTeam = DEFAULT_PARTICLES_PER_TEAM;
        private double propagationDecayFactor = DEFAULT_PROPAGATION_DECAY_FACTOR;
        private double initialPropagationFactor = DEFAULT_INITIAL_PROPAGATION_FACTOR;
        private int frequencyOfWallSpots = DEFAULT_FREQUENCY_OF_WALL_SPOTS;

        public Builder setNOfTeams(int nOfTeams) {
            this.nOfTeams = nOfTeams;
            return this;
        }

        public Builder setParticlesPerTeam(int particlesPerTeam) {
            this.particlesPerTeam = particlesPerTeam;
            return this;
        }

        public Builder setPropagationDecayFactor(double propagationDecayFactor) {
            this.propagationDecayFactor = propagationDecayFactor;
            return this;
        }

        public Builder setInitialPropagationFactor(double initialPropagationFactor) {
            this.initialPropagationFactor = initialPropagationFactor;
            return this;
        }

        public Builder setFrequencyOfWallSpots(int frequencyOfWallSpots) {
            this.frequencyOfWallSpots = frequencyOfWallSpots;
            return this;
        }

        public MapGenerator build() {
            return new MapGenerator(this);
        }
    }

    @Override
    public void generate(GameMap mapObj) {
        // On met à jour les IDs des équipes dans la map
        mapObj.setTeamIds(this.teamIds);

        Cell[][] cellmap = mapObj.getCellMap();
        int height = cellmap.length;
        int width = cellmap[0].length;
        
        boolean mapIsValid = false;
        int attempts = 0;
        int maxAttempts = 400;

        while (!mapIsValid && attempts < maxAttempts) {
            attempts++;

            // Reset de la map
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    cellmap[y][x] = new Empty();
                }
            }

            // Placement des spots de murs
            int mapSize = width * height;
            int numSpots = Math.max(1, mapSize / frequencyOfWallSpots);
            numSpots = Math.min(numSpots, 15);

            for(int i = 0; i < numSpots; i++) {
                int startX = random.nextInt(width);
                int startY = random.nextInt(height);

                if(cellmap[startY][startX] instanceof Empty) {
                    propagateWallFromPosition(cellmap, startX, startY, initialPropagationFactor);
                }
            }

            placeArmiesWithPropagation(cellmap, width, height);

            // Vérification de la validité via MapValidator
            mapIsValid = MapValidator.checkIfTeamsCanReachEachOthers(cellmap, teamIds.size());
        }

        if (!mapIsValid) {
            System.err.println("WARNING: Could not generate a valid map after " + maxAttempts + " attempts.");
        }

        mapObj.setCellMap(cellmap);
    }

    private void propagateWallFromPosition(Cell[][] cellmap, int startX, int startY, double initialPropagation) {
        int height = cellmap.length;
        int width = cellmap[0].length;

        LinkedList<PropagationCell> list = new LinkedList<>();
        list.add(new PropagationCell(startX, startY, initialPropagation, new Wall()));

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        while(!list.isEmpty()) {
            PropagationCell current = list.removeFirst();
            int x = current.x;
            int y = current.y;
            double propagation = current.propagation;

            if(x < 0 || x >= width || y < 0 || y >= height) continue;
            if(!(cellmap[y][x] instanceof Empty)) continue;

            cellmap[y][x] = current.cell;

            if(propagation < 0.1) continue;

            for(int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if(nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                if(!(cellmap[ny][nx] instanceof Empty)) continue;

                if(random.nextDouble() < propagation) {
                    double newPropagation = propagation * (1 - this.propagationDecayFactor);
                    list.add(new PropagationCell(nx, ny, newPropagation, new Wall()));
                }
            }
        }
    }

    private void placeArmiesWithPropagation(Cell[][] cellmap, int width, int height) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for(int teamId : this.teamIds) { // <-- CHANGEMENT ICI
            int centerX, centerY;
            int attempts = 0;
            do {
                centerX = random.nextInt(width);
                centerY = random.nextInt(height);
                attempts++;
            } while(!(cellmap[centerY][centerX] instanceof Empty) && attempts < 100);

            if(!(cellmap[centerY][centerX] instanceof Empty)) {
                System.out.println("failed placing army a 100 times");
                cellmap[centerY][centerX] = new Empty();
            }

            int remainingParticles = this.particlesPerTeam;
            LinkedList<PropagationCell> list = new LinkedList<>();
            list.add(new PropagationCell(centerX, centerY, 1.0, new Army(teamId, new Coord(centerX, centerY) )));

            while(!list.isEmpty() && remainingParticles > 0) {
                PropagationCell current = list.removeFirst();
                int x = current.x;
                int y = current.y;

                if(x < 0 || x >= width || y < 0 || y >= height) continue;
                if(!(cellmap[y][x] instanceof Empty)) continue;

                cellmap[y][x] = current.cell;
                remainingParticles--;
                if(remainingParticles <= 0) break;

                for(int[] dir : directions) {
                    int nx = x + dir[0];
                    int ny = y + dir[1];
                    if(nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                    if(!(cellmap[ny][nx] instanceof Empty)) continue;

                    list.add(new PropagationCell(nx, ny, 1.0, new Army(teamId, new Coord(nx, ny))));
                }
            }
        }
    }
}
