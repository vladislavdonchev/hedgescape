package task.interview.hedgescape.positioning;

/**
 * Enumeration defining the 4 possible player piece movement directions:
 * "E" - East
 * "N" - North
 * "W" - West
 * "S" - South
 * <p>
 * PLEASE NOTE:
 * These can be correlated (and replaced) with the {@link Orientation}
 * values, but having a separate enumeration for the movement direction
 * improves code intelligibility.
 */
public enum Direction {
    E,
    N,
    W,
    S;

    public Direction nextClockwise() {
        return values()[ordinal() < values().length - 1 ? ordinal() + 1 : 0];
    }

    public String getIndicator(boolean outline) {
        switch (this) {
            case E:
                return outline ? "▷" : "▶";
            case N:
                return outline ? "△" : "▲";
            case W:
                return outline ? "◁" : "◀";
            case S:
                return outline ? "▽" : "▼";
        }
        return "ERROR";
    }
}