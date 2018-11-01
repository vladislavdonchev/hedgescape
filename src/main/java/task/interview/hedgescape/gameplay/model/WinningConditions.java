package task.interview.hedgescape.gameplay.model;

import task.interview.hedgescape.positioning.Cell;
import task.interview.hedgescape.positioning.model.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class holds the position(s) and exact configuration(s) of the player piece
 * required to win the game (the configuration is defined by the initial piece shape
 * and its spacial orientation).
 */
public class WinningConditions {

    public List<Position> getWinningPositions() {
        return winningPositions;
    }

    private List<Position> winningPositions = new ArrayList<>();
    private List<Cell[][][]> winningPieceConfigurations = new ArrayList<>();

    public void addPosition(Position position) {
        winningPositions.add(position);
    }

    public void addPieceConfiguration(Cell[][][] pieceConfiguration) {
        winningPieceConfigurations.add(pieceConfiguration);
    }

    public boolean evaluateWinningConditions(Position position, Cell[][][] pieceConfiguration) {
        for (Position winningPosition : winningPositions) {
            for (Cell[][][] winningPieceConfiguration : winningPieceConfigurations) {
                if (winningPosition.equals(position)) {
                    if (Arrays.deepEquals(winningPieceConfiguration, pieceConfiguration)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}