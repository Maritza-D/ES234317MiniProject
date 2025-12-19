package MazeGame;

public class MazeGrid {
    public static final int COLS = 20;
    public static final int ROWS = 20;
    public static final int CELL_SIZE = 30;

    public static final int COST_GRASS = 1;
    public static final int COST_MUD = 5;
    public static final int COST_WATER = 10;

    public Cell[][] cells;
    public Cell startNode;
    public Cell endNode;

    public MazeGrid() {
        initGrid();
    }

    public void initGrid() {
        cells = new Cell[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
        startNode = cells[0][0];
        endNode = cells[ROWS - 1][COLS - 1];
        resetSolverData();
    }

    public void resetSolverData() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                cells[r][c].visitedSolve = false;
                cells[r][c].parent = null;
                cells[r][c].gCost = Integer.MAX_VALUE;
                cells[r][c].fCost = Integer.MAX_VALUE;
            }
        }
    }

    public void randomizeTerrain() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double chance = Math.random();
                if (chance < 0.5) cells[r][c].weight = COST_GRASS;
                else if (chance < 0.8) cells[r][c].weight = COST_MUD;
                else cells[r][c].weight = COST_WATER;
            }
        }
        startNode.weight = COST_GRASS;
        endNode.weight = COST_GRASS;
    }

    public boolean isValid(int r, int c) {
        return r >= 0 && c >= 0 && r < ROWS && c < COLS;
    }

    public static class Cell implements Comparable<Cell> {
        public int r, c;
        public boolean[] walls = {true, true, true, true};
        public boolean visitedGen = false, visitedSolve = false;
        public Cell parent = null;
        public int weight = 1;
        public int gCost = Integer.MAX_VALUE;
        public int fCost = Integer.MAX_VALUE;

        public Cell(int r, int c) { this.r = r; this.c = c; }

        public void calculateFCost(Cell endNode, String method) {
            if (method.equals("DIJKSTRA") || method.equals("BFS") || method.equals("DFS")) {
                this.fCost = this.gCost;
            } else {
                int hCost = Math.abs(this.r - endNode.r) + Math.abs(this.c - endNode.c);
                this.fCost = this.gCost + hCost;
            }
        }

        @Override
        public int compareTo(Cell other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }
}