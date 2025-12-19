package SnakeandLadderGame;

import java.awt.Color;

public class Player implements Comparable<Player> {
    public String name;
    public int id;
    public int skinIndex;
    public int position;
    public int score;
    public int wins;
    public Color color;

    public Player(String name, int id, int skinIndex, Color color) {
        this.name = name;
        this.id = id;
        this.skinIndex = skinIndex;
        this.color = color;
        this.position = 0;
        this.score = 0;
        this.wins = 0;
    }

    @Override
    public int compareTo(Player other) {
        return other.score - this.score;
    }

    @Override
    public String toString() {
        return name;
    }
}