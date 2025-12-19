package SnakeandLadderGame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SnakeGraphGame game = new SnakeGraphGame();
            game.setVisible(true);
        });
    }
}