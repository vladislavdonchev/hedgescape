package task.interview.hedgescape.positioning;

/**
 * Enumeration defining the 4 allowed rotation angles for each axis of
 * the player piece matrix.
 */
public enum Orientation {
    DEGREES_0,
    DEGREES_90,
    DEGREES_180,
    DEGREES_270;

    /**
     * Wrapper method for determining the next adjacent {@link Orientation} value.
     *
     * @param clockwise Right (-90º) or left (90º) rotation.
     * @return The new {@link Orientation} value.
     */
    public Orientation getNextRotation(boolean clockwise) {
        int newIndex = ordinal();

        if (clockwise) {
            newIndex--;
        } else {
            newIndex++;
        }

        if (newIndex < 0) {
            newIndex = values().length - 1;
        } else if (newIndex >= values().length) {
            newIndex = 0;
        }

        return values()[newIndex];
    }
}