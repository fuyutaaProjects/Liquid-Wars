package code.model.ai;

import code.model.*;

public class EnemyCenterAI implements TeamAI {

    @Override
    public void updateCursor(GameMap map, int teamId) {
        Coord myCenter = map.getArmyCenterOfMass(teamId);
        if (myCenter == null) return;

        Coord bestEnemyCenter = null;
        double bestDist = Double.MAX_VALUE;

        for (int enemyId = 0; enemyId < map.getTeamIds().size(); enemyId++) {
            if (enemyId == teamId) continue;

            Coord enemyCenter = map.getArmyCenterOfMass(enemyId);
            if (enemyCenter == null) continue;

            double d = Math.hypot(
                enemyCenter.getX() - myCenter.getX(),
                enemyCenter.getY() - myCenter.getY()
            );

            if (d < bestDist) {
                bestDist = d;
                bestEnemyCenter = enemyCenter;
            }
        }

        if (bestEnemyCenter != null) {
            map.setTeamCursor(teamId, bestEnemyCenter);
        }
    }
}
