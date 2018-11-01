package task.interview.hedgescape;

import task.interview.hedgescape.gameplay.GameController;

public class Main {

    private static GameController gameController = new GameController();

    public static void main(String[] args) {
        gameController.startNewGame(true, true);
    }
}