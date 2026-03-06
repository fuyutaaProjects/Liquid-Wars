package code.model.ai;

import code.model.*;
import code.model.celltype.*;

public class ClosestEnemyAI implements TeamAI {

    @Override
    public void updateCursor(GameMap map, int teamId) {
        Coord myCenter = map.getArmyCenterOfMass(teamId);
        if (myCenter == null) return;

        Coord best = null;
        double bestDist = Double.MAX_VALUE;

        for (int y = 0; y < map.getCellMap().length; y++) {
            for (int x = 0; x < map.getCellMap()[0].length; x++) {
                Cell c = map.getCellAt(x, y);
                if (c instanceof Army a && a.getTeamId() != teamId) {
                    double d = Math.hypot(
                        x - myCenter.getX(),
                        y - myCenter.getY()
                    );
                    if (d < bestDist) {
                        bestDist = d;
                        best = new Coord(x, y);
                    }
                }
            }
        }

        if (best != null) {
            map.setTeamCursor(teamId, best);
        }
    }
}