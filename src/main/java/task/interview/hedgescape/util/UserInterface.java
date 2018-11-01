package task.interview.hedgescape.util;

import task.interview.hedgescape.positioning.Cell;

public class UserInterface {

    public static final boolean DEBUG_MODE = true;

    public static void print2DMatrix(Cell[][] matrix) {
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix.length; y++) {
                String cellSymbol = "□";

                if (matrix[x][y] == Cell.BLOCKED) {
                    cellSymbol = "▦";
                } else if (matrix[x][y] == Cell.PLAYER) {
                    cellSymbol = "■";
                }

                System.out.print(cellSymbol + " ");
            }
            System.out.println();
        }

        System.out.println();
    }
}
