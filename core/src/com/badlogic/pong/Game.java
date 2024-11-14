package com.badlogic.pong;

public class Game {
    private static Game instance;
    private int leftPlayerScore = 0;
    private int rightPlayerScore = 0;
    private boolean gameOver = false;
    private Game() {}

    public static synchronized Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public void scoreLeftPlayer() {
        if (!gameOver) {
            leftPlayerScore++;
        }
        if (leftPlayerScore == 21) {
            gameOver = true;
        }
    }

    public void scoreRightPlayer() {
        if (!gameOver) {
            rightPlayerScore++;
        }
        if (rightPlayerScore == 21) {
            gameOver = true;
        }
    }

    public int getLeftPlayerScore() {
        return leftPlayerScore;
    }

    public int getRightPlayerScore() {
        return rightPlayerScore;
    }

    public int getWinningScore() {
        return 21;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    // Reset game to initial state
    public void resetGame() {
        leftPlayerScore = 0;
        rightPlayerScore = 0;
        gameOver = false;
    }
}
