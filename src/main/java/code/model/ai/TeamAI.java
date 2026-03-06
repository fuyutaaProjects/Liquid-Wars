package code.model.ai;

import code.model.GameMap;

public interface TeamAI {
    void updateCursor(GameMap map, int teamId);
}
