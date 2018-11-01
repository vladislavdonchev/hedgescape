package task.interview.hedgescape.positioning.model;

import com.sun.istack.internal.NotNull;
import task.interview.hedgescape.positioning.Axis;
import task.interview.hedgescape.positioning.Cell;
import task.interview.hedgescape.positioning.Direction;

/**
 * This class encapsulates the parameters of each move of the player piece.
 * It also holds a reference to its predecessor, for the purposes of the
 * path-finding algorithm.
 */
public class PlayerMove {
    private Position position;
    private Direction direction;

    private PlayerMove precedingMove;
    private boolean successful = false;

    public PlayerMove(Position position, @NotNull Direction direction, PlayerMove precedingMove) {
        this.position = new Position(position.getX(), position.getY());
        this.direction = direction;
        this.precedingMove = precedingMove;
    }

    @Override
    public boolean equals(Object obj) {
        boolean match = true;

        PlayerMove playerMove = (PlayerMove) obj;
        match &= position.equals(playerMove.getPosition())
                && direction == playerMove.getDirection();

        return match;
    }

    public Position getPosition() {
        return position;
    }

    public PlayerMove getPrecedingMove() {
        return precedingMove;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public PieceRotation getRotationBasedOnDirection() {
        /**
         * Maybe some other type of initialization instead of the 'null' value
         * should be implemented as the direction itself can never be null (see constructor).
         */
        PieceRotation pieceRotation = null;

        switch (direction) {
            case E:
                pieceRotation = new PieceRotation(Axis.X, true);
                break;
            case N:
                pieceRotation = new PieceRotation(Axis.Y, true);
                break;
            case W:
                pieceRotation = new PieceRotation(Axis.X, false);
                break;
            case S:
                pieceRotation = new PieceRotation(Axis.Y, false);
                break;
        }

        return pieceRotation;
    }
}
