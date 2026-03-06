package code.model.ai;

import java.util.Random;

public class AIFactory {

    private static final Random rng = new Random();

    public static TeamAI randomAI() {
        return rng.nextBoolean()
            ? new ClosestEnemyAI()
            : new EnemyCenterAI();
    }
}
