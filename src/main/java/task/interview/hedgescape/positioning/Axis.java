package task.interview.hedgescape.positioning;

import java.util.concurrent.ThreadLocalRandom;

public enum Axis {
    X,
    Y,
    Z;

    /**
     * An utility method for getting either the X or Y axis, as these are
     * the only two allowed axes for the player piece movement.
     *
     * @return Random {@link Axis ) value.
     */
    public static Axis getRandomTumblingAxis() {
        int randomIndex = ThreadLocalRandom.current().nextInt(2);
        return values()[randomIndex];
    }
}
