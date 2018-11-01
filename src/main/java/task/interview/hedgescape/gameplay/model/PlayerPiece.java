package task.interview.hedgescape.gameplay.model;

import task.interview.hedgescape.positioning.Cell;
import task.interview.hedgescape.positioning.model.Position;

/**
 * This class holds all positioning / orientation-related variables and behaviors
 * for the player piece.
 */
public class PlayerPiece {
    /**
     * This array represents the "bounding box" of the player piece.
     * The 3-dimensional shape of the piece itself is defined by the cells
     * holding {@link Cell.PLAYER} values.
     * <p>
     * The contents of the bounding box should be updated with the correct
     * orientation of the initially defined shape each time the piece tumbles (moves).
     * <p>
     * This is done as an optimization, so the current shape does not have
     * to be derived by the orientation alone after performing the player move
     * calculations.
     */
    private Cell[][][] boundingBox;

    /**
     * This field holds the x/y coordinates of the player piece on the game grid.
     * These are needed in order to be able to evaluate the winning conditions
     * of the game.
     */
    private Position position;

    public PlayerPiece(Cell[][][] pieceShape) {
        boundingBox = pieceShape;
        position = new Position(0, 0);
    }

    public Cell[][][] getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Cell[][][] boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Position getPosition() {
        return position;
    }

    /**
     * This is used instead of a "setter" method as an optimization
     * (so new {@link Position} instances are not created each time).
     *
     * @param x
     * @param y
     */
    public void updatePosition(int x, int y) {
        if (position == null) {
            position = new Position(x, y);
        } else {
            position.setX(x);
            position.setY(y);
        }
    }
}