package task.interview.hedgescape.gameplay;

import com.google.gson.Gson;
import task.interview.hedgescape.gameplay.model.PlayerPiece;
import task.interview.hedgescape.gameplay.model.WinningConditions;
import task.interview.hedgescape.positioning.Axis;
import task.interview.hedgescape.positioning.Cell;
import task.interview.hedgescape.positioning.Direction;
import task.interview.hedgescape.positioning.model.PieceRotation;
import task.interview.hedgescape.positioning.model.PlayerMove;
import task.interview.hedgescape.positioning.model.Position;
import task.interview.hedgescape.util.FileUtil;
import task.interview.hedgescape.util.MatrixUtil;
import task.interview.hedgescape.util.UserInterface;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class encapsulates all gameplay-related behaviors.
 */
public class GameController {

    /**
     * These constants define the game board and player piece sizes.
     * The game board is a square matrix (implemented as a 2D array)
     * and the player piece is a cubic matrix (implemented as a 3D array).
     * <p>
     * PLEASE NOTE:
     * Although these values can be modified, the player piece should
     * be at least 1/2 the size of the game board in order to be able
     * to move across its grid.
     * <p>
     * Only one side (plane) of the 3-dimensional player piece is in
     * contact with the game board at any given time and the player moves
     * are performed by "tumbling" the piece in one of the 4 cardinal
     * directions (E, N, W & S).
     * <p>
     * The tumbling movement is implemented as 90º matrix rotations along
     * the 'X' and 'Y' axes.
     */
    private static final int GAME_BOARD_SIZE = 7;
    private static final int PLAYER_PIECE_SIZE = 3;

    /**
     * A constant specifying the number of cells that should be unavailable
     * for the player piece to occupy, at the start of each game.
     */
    private static final int BLOCKED_CELLS_COUNT = 4;

    /**
     * The default shape of the player piece is kept in the application resources
     * as a JSON file (a serialized version of the player piece bounding box
     * containing {@link Cell.FREE} and {@link Cell.PLAYER} values).
     * <p>
     * Due to the requirements for the "tumbling" movement and as per the laws of
     * classical mechanics (and original game rules), this shape should describe
     * a "solid" object, meaning that all "cells" that define it have to be
     * connected either directly or indirectly.
     * <p>
     * PLEASE NOTE:
     * A more optimal approach to the serialization would be to use the {@link Cell}
     * enumeration values' ordinals instead of their names, but that would require
     * custom parsing rules.
     */
    private static final String DEFAULT_PLAYER_PIECE_FILE = "defaultPlayerPiece.json";

    /**
     * The amount of times to tumble the player piece before placing it on the game
     * board in random game setting.
     */
    private static final int RANDOM_TUMBLES = 16;

    /**
     * To beat the game, it is required that the player piece tumbles to
     * a specific position and orientation on the game board.
     */
    private WinningConditions winningConditions = new WinningConditions();

    /**
     * The player piece is initialized with the shape of the piece itself
     * at the start of each game.
     */
    private PlayerPiece playerPiece;

    /**
     * This array represents the game board grid.
     * The board grid can contain {@link Cell.FREE}, {@link Cell.BLOCKED}
     * <p>
     * or {@link Cell.PLAYER} values.
     */
    private Cell[][] gameBoard = new Cell[GAME_BOARD_SIZE][GAME_BOARD_SIZE];

    /**
     * Entry point for every new game.
     *
     * @param random   If random generation is selected, the blocked cells and
     *                 player piece will be placed randomly on the game board.
     * @param solvable If set to 'true', new blocked cells / player piece placements
     *                 will be made until a solvable puzzle is generated.
     */
    public void startNewGame(boolean random, boolean solvable) {
        initializePlayerPiece(FileUtil.readResourceAsString(DEFAULT_PLAYER_PIECE_FILE));
        setWinningConditions(getDefaultWinningConditions());

        if (random) {
            int scenariosEvaluated = 0;
            long totalTime = System.currentTimeMillis();
            long solutionTime;
            boolean solved;

            do {
                if (scenariosEvaluated % 64 == 0) {
                    System.out.println();
                }
                System.out.print(".");

                initializeGameBoard();

                placePlayerPieceRandomly();
                blockCellsRandomly(BLOCKED_CELLS_COUNT);

                System.out.println();
                printGameBoard();

                solutionTime = System.currentTimeMillis();
                solved = solvePuzzle(true, true);
                scenariosEvaluated++;

                if (solved) {
                    System.out.println();
                    totalTime = System.currentTimeMillis() - totalTime;
                    solutionTime = System.currentTimeMillis() - solutionTime;
                    System.out.println("PUZZLE SOLVED!");
                    System.out.println("SCENARIOS EVALUATED: " + scenariosEvaluated);
                    System.out.println("TOTAL TIME: " + ((double) totalTime / 1000) + " seconds");
                    System.out.println("SOLUTION TIME: " + ((double) solutionTime / 1000) + " seconds");

                    // TODO Print some kind of menu instead of exiting?
                    System.exit(0);
                }
            } while (solvable && !solved);
        } else {
            // TODO Implement user-defined game setup and non-AI game-play.
        }
    }

    public void blockCell(int cellX, int cellY) {
        gameBoard[cellX][cellY] = Cell.BLOCKED;
    }

    public void setWinningConditions(WinningConditions winningConditions) {
        this.winningConditions = winningConditions;
    }

    /**
     * Checks whether the requested player move is allowed / possible and updates
     * the player piece correspondingly.
     *
     * @param playerMove
     * @return
     */
    public void attemptPlayerMove(PlayerMove playerMove) {
        Position movePosition = playerMove.getPosition();
        PieceRotation pieceRotation = playerMove.getRotationBasedOnDirection();

        System.out.println("ATTEMPT MOVE "
                + "@[" + movePosition.getX() + "," + movePosition.getY() + "] "
                + playerMove.getDirection().getIndicator(false));

        /**
         * Immediately fail in the cases where the player piece touches any of the
         * game board boundaries.
         */
        if ((movePosition.getY() == GAME_BOARD_SIZE - 1 && playerMove.getDirection() == Direction.E)
                || (movePosition.getX() == 0 && playerMove.getDirection() == Direction.N)
                || (movePosition.getY() == 0 && playerMove.getDirection() == Direction.W)
                || (movePosition.getX() == GAME_BOARD_SIZE - 1 && playerMove.getDirection() == Direction.S)) {
            System.out.println("NO ROOM TO MOVE!");
            playerMove.setSuccessful(false);
            return;
        }

        Cell[][] currentFootprint = MatrixUtil.getPieceShapeFootprint(playerPiece.getBoundingBox());

        Cell[][][] reorientedPiece = MatrixUtil.copy3DMatrix(playerPiece.getBoundingBox());
        MatrixUtil.rotate3DMatrix(reorientedPiece, pieceRotation);

        Cell[][] projectedFootprint = MatrixUtil.getPieceShapeFootprint(reorientedPiece);

        int projectedX = movePosition.getX();
        int projectedY = movePosition.getY();
        boolean outOfBounds = false;

        switch (playerMove.getDirection()) {
            case E:
                projectedY += currentFootprint[0].length - 1;
                outOfBounds = projectedY + projectedFootprint[0].length - 1 > GAME_BOARD_SIZE - 1;
                break;
            case N:
                projectedX -= projectedFootprint.length - 1;
                outOfBounds = projectedX < 0;
                break;
            case W:
                projectedY -= projectedFootprint[0].length - 1;
                outOfBounds = projectedY < 0;
                break;
            case S:
                projectedX += currentFootprint.length - 1;
                outOfBounds = projectedX + projectedFootprint.length - 1 > GAME_BOARD_SIZE - 1;
                break;
        }

        /**
         * Fail in the cases where the projected footprint will fall outside of the
         * game board boundaries.
         */
        if (outOfBounds) {
            System.out.println("OUT OF BOUNDS!");
            playerMove.setSuccessful(false);
            return;
        }

        /**
         * Overlay the projected footprint on the game board and check whether any blocked
         * cells prevent its placement.
         */
        for (int x = projectedX; x < projectedX + projectedFootprint.length; x++) {
            for (int y = projectedY; y < projectedY + projectedFootprint[0].length; y++) {
                if (projectedFootprint[x - projectedX][y - projectedY] == Cell.PLAYER
                        && gameBoard[x][y] == Cell.BLOCKED) {
                    System.out.println("BLOCKED POSITION!");
                    playerMove.setSuccessful(false);
                    return;
                }
            }
        }

        /**
         * If the move is successful, update the player piece shape, position and
         * orientation and overlay its footprint on the game board.
         */
        playerMove.setSuccessful(true);
        playerPiece.setBoundingBox(reorientedPiece);
        playerPiece.updatePosition(projectedX, projectedY);

        overlayPieceFootprintOnBoard(playerPiece.getPosition(), projectedFootprint);
    }

    /**
     * The method for solving the puzzle implements a simplified version of the classic
     * A* search algorithm.
     * <p>
     * TODO NOTE:
     * The path-finding strategy can be optimized to always try the direction pointing
     * as close as possible to the position defined by the winning conditions first.
     *
     * @param printSolution Whether to print the game board, as the algorithm progresses.
     * @return
     */
    public boolean solvePuzzle(boolean printAlgorithm, boolean printSolution) {
        boolean solved = false;

        /**
         * These maps hold all game grid positions that have been visited by the path-finding
         * algorithm and the game moves made at each one of them.
         */
        Map<String, List<PlayerMove>> movesAttempted = new LinkedHashMap<>();
        PlayerMove currentMove;
        PlayerMove previousMove = null;

        do {
            String piecePositionKey = playerPiece.getPosition().toString();

            if (previousMove == null) {
                // First move.
                currentMove = new PlayerMove(playerPiece.getPosition(), Direction.E, null);
                movesAttempted.put(piecePositionKey, new ArrayList<>());
            } else {
                // Get the list of moves from the current position.
                List<PlayerMove> movesAtPosition = movesAttempted.get(piecePositionKey);
                Direction moveDirection;

                if (movesAtPosition == null) {
                    // Get the list of moves from the previous position.
                    movesAtPosition = movesAttempted.get(movesAttempted.keySet().toArray()[movesAttempted.size() - 1]);
                    moveDirection = previousMove.getDirection();

                    movesAttempted.put(piecePositionKey, new ArrayList<>());
                } else {
                    moveDirection = previousMove.getDirection().nextClockwise();
                }

                currentMove =
                        new PlayerMove(playerPiece.getPosition(), moveDirection, previousMove);

                if (movesAtPosition.size() == Direction.values().length) {
                    // Out of moves.
                    break;
                }
            }

            attemptPlayerMove(currentMove);
            previousMove = currentMove;

            movesAttempted.get(currentMove.getPosition().toString()).add(previousMove);

            if (previousMove.isSuccessful()) {
                solved =
                        winningConditions.evaluateWinningConditions(playerPiece.getPosition(), playerPiece.getBoundingBox());

                if (printAlgorithm) {
                    printGameBoard();
                }
            }
        } while (!solved);

        if (printSolution) {
            //TODO Print the final solution without unnecessary moves.
        }

        return solved;
    }

    /**
     * TODO Maybe implement a check whether the piece shape describes a solid object.
     *
     * @param pieceShapeJSON
     */
    public void initializePlayerPiece(String pieceShapeJSON) {
        playerPiece = new PlayerPiece(new Gson().fromJson(pieceShapeJSON, Cell[][][].class));
    }

    private WinningConditions getDefaultWinningConditions() {
        WinningConditions winningConditions = new WinningConditions();
        winningConditions.addPosition(new Position(4, 5));
        winningConditions.addPieceConfiguration(MatrixUtil.copy3DMatrix(playerPiece.getBoundingBox()));

        return winningConditions;
    }

    private void initializeGameBoard() {
        MatrixUtil.fill2DMatrix(gameBoard, Cell.FREE);
    }

    private void placePlayerPieceRandomly() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Tumble the player piece randomly for a while before placing it.
        for (int t = 0; t < RANDOM_TUMBLES; t++) {
            PieceRotation randomRotation = new PieceRotation(Axis.getRandomTumblingAxis(), random.nextBoolean());
            MatrixUtil.rotate3DMatrix(playerPiece.getBoundingBox(), randomRotation);
        }

        Cell[][] pieceFootprint = MatrixUtil.getPieceShapeFootprint(playerPiece.getBoundingBox());

        int randomX;
        int randomY;

        boolean suitablePositionFound = false;

        /**
         * Avoid putting the piece at one of the winning positions from the very beginning.
         */
        do {
            randomX = random.nextInt(GAME_BOARD_SIZE - pieceFootprint.length + 1);
            // We take the length of the first X row of the matrix, as the piece footprint
            // should always be a rectangular matrix, due to fact that the piece can have an
            // irregular, concave or convex shape, and we don't want to waste iterations for
            // rows or columns that contain only empty cells.
            randomY = random.nextInt(GAME_BOARD_SIZE - pieceFootprint[0].length + 1);

            for (Position winningPosition : winningConditions.getWinningPositions()) {
                suitablePositionFound = randomX != winningPosition.getX() && randomY != winningPosition.getY();
            }
        } while (!suitablePositionFound);

        playerPiece.updatePosition(randomX, randomY);

        overlayPieceFootprintOnBoard(playerPiece.getPosition(), pieceFootprint);
    }

    private void blockCellsRandomly(int cellCount) {
        int randomX;
        int randomY;
        int blockedCellsPlaced = 0;

        /**
         * Look for positions not occupied by the player piece or
         * other blocked cells.
         */
        do {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            randomX = random.nextInt(GAME_BOARD_SIZE);
            randomY = random.nextInt(GAME_BOARD_SIZE);
            if (gameBoard[randomX][randomY] == Cell.FREE) {
                blockCell(randomX, randomY);
                blockedCellsPlaced++;
            }
        } while (blockedCellsPlaced < cellCount);
    }

    /**
     * Should only be used after an attempted player move is successful.
     *
     * @param position
     * @param pieceFootprint
     */
    private void overlayPieceFootprintOnBoard(Position position, Cell[][] pieceFootprint) {
        for (int x = 0; x < GAME_BOARD_SIZE; x++) {
            for (int y = 0; y < GAME_BOARD_SIZE; y++) {
                if ((x < position.getX()) || (y < position.getY())
                        || (x > position.getX() + pieceFootprint.length - 1)
                            || (y > position.getY() + pieceFootprint[0].length - 1)) {
                    if (gameBoard[x][y] == Cell.PLAYER) {
                        gameBoard[x][y] = Cell.FREE;
                    }
                } else {
                    gameBoard[x][y] =
                            pieceFootprint[x - position.getX()][y - position.getY()];
                }
            }
        }
    }

    private void printGameBoard() {
        System.out.println();
        UserInterface.print2DMatrix(gameBoard);
    }
}