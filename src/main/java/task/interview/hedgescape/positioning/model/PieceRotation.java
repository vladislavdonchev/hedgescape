package task.interview.hedgescape.positioning.model;

import task.interview.hedgescape.positioning.Axis;

/**
 * A wrapper class describing the change in orientation of the player piece
 * cubic matrix in 3D space for each player (tumble) move.
 * <p>
 * As per the game rules, rotations are done in 90º increments.
 */
public class PieceRotation {

    private Axis axis;
    private boolean clockwise;

    public PieceRotation(Axis axis, boolean clockwise) {
        this.axis = axis;
        this.clockwise = clockwise;
    }

    public Axis getAxis() {
        return axis;
    }

    public boolean isClockwise() {
        return clockwise;
    }
}