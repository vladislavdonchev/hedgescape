package task.interview.hedgescape.util;

import task.interview.hedgescape.gameplay.model.PlayerPiece;
import task.interview.hedgescape.positioning.Cell;
import task.interview.hedgescape.positioning.model.PieceRotation;
import task.interview.hedgescape.positioning.model.Position;

/**
 * Utility class for all matrix-related operations (game board / player piece transformations).
 */
public class MatrixUtil {

    /**
     * Fills a given square 2D array uniformly with the specified {@link Cell} value.
     *
     * @param matrix
     * @param cellType
     */
    public static void fill2DMatrix(Cell[][] matrix, Cell cellType) {
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix.length; y++) {
                matrix[x][y] = cellType;
            }
        }
    }

    /**
     * Create a "deep" copy of the given 3D array.
     *
     * @param matrix
     * @return
     */
    public static Cell[][][] copy3DMatrix(Cell[][][] matrix) {
        Cell[][][] clonedMatrix = new Cell[matrix.length][matrix.length][matrix.length];

        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix.length; y++) {
                for (int z = 0; z < matrix.length; z++) {
                    clonedMatrix[x][y][z] = matrix[x][y][z];
                }
            }
        }

        return clonedMatrix;
    }

    /**
     * Rotate the values in the player piece bounding box 3D matrix around a given
     * axis to 90º in either clockwise or counter-clockwise direction, by rotating
     * each 2D sub-matrix 'layer' of either the Y/Z or X/Z planes.
     * <p>
     * TODO NOTE:
     * This implementation is not optimal, a better approach would be to perform
     * multiplication with a rotational matrix such as the one given below:
     * <p>
     * | 1     0      0    |
     * Rx(a) = | 0  cos(a) -sin(a) |
     * | 0  sin(a)  cos(a) |
     * <p>
     * Where 'a' will be either 90º or 270º (-90º) (in this case around the 'X' axis).
     *
     * @param matrix
     * @param pieceRotation
     */
    public static void rotate3DMatrix(Cell[][][] matrix, PieceRotation pieceRotation) {
        switch (pieceRotation.getAxis()) {
            case X:
                // Rotate all layers of the Y / Z plane.
                for (int x = 0; x < matrix.length; x++) {
                    rotate2DMatrix90Degrees(matrix[x], pieceRotation.isClockwise());
                }
                break;
            case Y:
                /**
                 * This is the main flaw of this approach - we need to extract the
                 * X / Z plane sub-matrix before performing the 2D rotation and then
                 * iterate it back into the original 3D matrix.
                 */
                Cell[][] subMatrix = new Cell[matrix.length][matrix.length];
                // Rotate all layers of the X / Z plane.
                for (int y = 0; y < matrix.length; y++) {
                    for (int x = 0; x < matrix.length; x++) {
                        for (int z = 0; z < matrix.length; z++) {
                            subMatrix[x][z] = matrix[x][y][z];
                        }
                    }

                    rotate2DMatrix90Degrees(subMatrix, pieceRotation.isClockwise());

                    for (int x = 0; x < matrix.length; x++) {
                        for (int z = 0; z < matrix.length; z++) {
                            matrix[x][y][z] = subMatrix[x][z];
                        }
                    }
                }
                break;
            case Z:
                /**
                 * Rotation around the 'Z' axis is not allowed in the original game rules.
                 * However, it might prove to be an interesting additional option for more
                 * complex game board scenarios.
                 */
                break;
        }

        realignPieceShapeInMatrix(matrix);
    }

    /**
     * The player piece footprint is used for the all game-play related calculations.
     * It's the bottommost (z=0) X/Y sub-matrix containing {@link Cell.PLAYER}
     * and {@link Cell.FREE} positions, but without any empty rows or columns.
     * <p>
     * TODO NOTE:
     * Another algorithm should be implemented, so the source matrix is not iterated
     * through twice, also the generated footprint can be cached in the {@link PlayerPiece}
     * as a performance optimization.
     *
     * @return
     */
    public static Cell[][] getPieceShapeFootprint(Cell[][][] matrix) {
        int shapeHeight = matrix.length;
        int shapeWidth = matrix.length;

        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix.length; y++) {
                if (matrix[x][y][0] == Cell.PLAYER) {
                    shapeHeight = x + 1;
                    shapeWidth = y + 1;
                }
            }
        }

        Cell[][] footprint = new Cell[shapeHeight][shapeWidth];

        for (int x = 0; x < shapeHeight; x++) {
            for (int y = 0; y < shapeWidth; y++) {
                footprint[x][y] = matrix[x][y][0];
            }
        }

        return footprint;
    }

    /**
     * This method is used to align the "shape" of the player piece inside its
     * bounding box to the first rows along each axis of the bounding box 3D matrix.
     * <p>
     * The way this is achieved is by checking all 2D sub-matrices defined by the
     * X/Y, Y/Z and X/Z axes for, essentially, "empty" layers (entirely filled with
     * {@link Cell.FREE} values).
     * <p>
     * The result is that the player piece shape is aligned with the starting point
     * of the coordinate system octant.
     * <p>
     * EXAMPLES (3x3x3 player piece):
     * (2D sub-matrix for the X/Y axes realignment - the base for the "footprint" of the player piece)
     * □ ■ ■        ■ ■ □       □ □ □        ■ ■ □
     * □ □ ■   ->   □ ■ □       □ ■ ■   ->   ■ ■ □
     * □ ■ ■        ■ ■ □       □ ■ ■        □ □ □
     * <p>
     * (2D sub-matrix for the Y/Z or X/Z axes realignment)
     * ■ ■ ■        □ □ □       ■ ■ □        □ □ □
     * ■ □ ■   ->   ■ ■ ■       ■ ■ □   ->   ■ ■ □
     * □ □ □        ■ □ ■       □ □ □        ■ ■ □
     *
     * @param matrix
     */
    private static void realignPieceShapeInMatrix(Cell[][][] matrix) {
        int startingX = matrix.length;
        int startingY = matrix.length;
        int startingZ = matrix.length;

        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix.length; y++) {
                for (int z = 0; z < matrix.length; z++) {
                    if (matrix[x][y][z] == Cell.PLAYER) {
                        if (x < startingX) {
                            startingX = x;
                        }
                        if (y < startingY) {
                            startingY = y;
                        }
                        if (z < startingZ) {
                            startingZ = z;
                        }
                    }
                }
            }
        }

        for (int x = startingX; x < matrix.length + startingX; x++) {
            for (int y = startingY; y < matrix.length + startingY; y++) {
                for (int z = startingZ; z < matrix.length + startingZ; z++) {
                    Cell realignedValue = Cell.FREE;
                    if (x < matrix.length && y < matrix.length && z < matrix.length) {
                        realignedValue = matrix[x][y][z];
                    }
                    matrix[x - startingX][y - startingY][z - startingZ] = realignedValue;
                }
            }
        }
    }

    private static void rotate2DMatrix90Degrees(Cell[][] matrix, boolean clockwise) {
        Cell temp;

        // Transpose the matrix.
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < i; j++) {
                temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }

        if (clockwise) {
            // Swap columns.
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix.length / 2; j++) {
                    temp = matrix[i][j];
                    matrix[i][j] = matrix[i][matrix.length - j - 1];
                    matrix[i][matrix.length - j - 1] = temp;
                }
            }
        } else {
            // Swap rows.
            for (int i = 0; i < matrix.length / 2; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    temp = matrix[i][j];
                    matrix[i][j] = matrix[matrix.length - 1 - i][j];
                    matrix[matrix.length - 1 - i][j] = temp;
                }
            }
        }
    }

    public static int pointDirection(Position p1, Position p2) {
        double xDiff = p2.getX() - p1.getX();
        double yDiff = p2.getY() - p1.getY();

        return (int) Math.toDegrees(Math.atan2(yDiff, xDiff));
    }
}