package task.interview.hedgescape.positioning.model;

/**
 * A simple model for the positioning on the player piece on the game board.
 */
public class Position {

    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        Position position = (Position) obj;
        return x == position.getX() && y == position.getY();
    }

    @Override
    public String toString() {
        return String.valueOf(x) + "," + String.valueOf(y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
