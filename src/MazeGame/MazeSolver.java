package MazeGame;

import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class MazeSolver {
    private MazeGrid grid;
    private MazePanel panel;
    private MazeGameMain mainFrame;
    private Timer animTimer;
    private boolean isWorking = false;

    public long nodesVisitedCount = 0;
    public int totalPathCost = 0;

    public MazeSolver(MazeGrid grid, MazePanel panel, MazeGameMain mainFrame) {
        this.grid = grid;
        this.panel = panel;
        this.mainFrame = mainFrame;
    }

    public void stopTimer() {
        if (animTimer != null) { animTimer.cancel(); animTimer = null; }
        isWorking = false;
    }

    private void runAnimationStep(Runnable stepAction) {
        animTimer = new java.util.Timer();
        animTimer.schedule(new TimerTask() {
            @Override public void run() {
                if (!isWorking) { cancel(); return; }
                stepAction.run();
                if (isWorking) runAnimationStep(stepAction);
            }
        }, mainFrame.getAnimationDelay());
    }

    public void startGeneration() {
        if (isWorking) return;
        mainFrame.updateStatus("Sedang Membuat Maze...");
        stopTimer();
        grid.initGrid();
        panel.resetPlayerMode();
        isWorking = true;
        Stack<MazeGrid.Cell> frontier = new Stack<>();
        grid.startNode.visitedGen = true;
        addFrontier(grid.startNode, frontier);

        runAnimationStep(() -> {
            if (!frontier.isEmpty()) {
                int randIndex = (int) (Math.random() * frontier.size());
                MazeGrid.Cell current = frontier.remove(randIndex);
                List<MazeGrid.Cell> neighbors = getVisitedNeighbors(current);
                if (!neighbors.isEmpty()) {
                    MazeGrid.Cell target = neighbors.get((int) (Math.random() * neighbors.size()));
                    removeWalls(current, target);
                    current.visitedGen = true;
                    addFrontier(current, frontier);
                }
                panel.repaint();
            } else {
                addRandomLoops();
                grid.randomizeTerrain();
                isWorking = false;
                mainFrame.updateStatus("Maze Siap. Silakan pilih solusi.");
            }
        });
    }

    public void startUnweightedSearch(String method) {
        if (isWorking) return;
        prepareSearch(method);
        Collection<MazeGrid.Cell> structure = method.equals("DFS") ? new Stack<>() : new LinkedList<>();
        structure.add(grid.startNode);
        grid.startNode.visitedSolve = true;

        runAnimationStep(() -> {
            if (!structure.isEmpty()) {
                MazeGrid.Cell curr = method.equals("DFS") ? ((Stack<MazeGrid.Cell>) structure).pop() : ((Queue<MazeGrid.Cell>) structure).poll();
                panel.setCurrentSearchNode(curr);
                nodesVisitedCount++;

                // --- CEK WIN DI SINI ---
                if (curr == grid.endNode) {
                    SoundManager.playSFX("win.wav");
                    finishSearch(curr, method);
                    return;
                }
                // -----------------------

                checkNeighborUnweighted(curr, -1, 0, 0, structure);
                checkNeighborUnweighted(curr, 1, 0, 1, structure);
                checkNeighborUnweighted(curr, 0, 1, 2, structure);
                checkNeighborUnweighted(curr, 0, -1, 3, structure);
                panel.repaint();
            } else { isWorking = false; mainFrame.updateStatus(method + " Gagal."); }
        });
    }

    public void startWeightedSearch(String method) {
        if (isWorking) return;
        prepareSearch(method);
        PriorityQueue<MazeGrid.Cell> pq = new PriorityQueue<>();
        grid.startNode.gCost = 0;
        grid.startNode.calculateFCost(grid.endNode, method);
        pq.add(grid.startNode);

        runAnimationStep(() -> {
            if (!pq.isEmpty()) {
                MazeGrid.Cell curr = pq.poll();
                if (curr.visitedSolve) return;
                curr.visitedSolve = true;
                panel.setCurrentSearchNode(curr);
                nodesVisitedCount++;

                if (curr == grid.endNode) {
                    SoundManager.playSFX("win.wav"); // <--- AI BUNYI
                    finishSearch(curr, method);
                    return;
                }
                checkNeighborWeighted(curr, -1, 0, 0, method, pq);
                checkNeighborWeighted(curr, 1, 0, 1, method, pq);
                checkNeighborWeighted(curr, 0, 1, 2, method, pq);
                checkNeighborWeighted(curr, 0, -1, 3, method, pq);
                panel.repaint();
            } else { isWorking = false; mainFrame.updateStatus(method + " Gagal."); }
        });
    }

    private void prepareSearch(String method) {
        isWorking = true;
        panel.resetPlayerMode();
        grid.resetSolverData();
        panel.clearPath();
        nodesVisitedCount = 0;
        mainFrame.updateStatus("Menjalankan " + method + "...");
    }

    private void finishSearch(MazeGrid.Cell end, String method) {
        reconstructPath(end);
        isWorking = false;
        panel.repaint();
        mainFrame.updateStatus(String.format("%s Selesai. Total Biaya: %d | Node Dicek: %d", method, totalPathCost, nodesVisitedCount));
    }

    private void reconstructPath(MazeGrid.Cell end) {
        MazeGrid.Cell curr = end;
        totalPathCost = 0;
        List<MazeGrid.Cell> path = new ArrayList<>();
        while (curr != null) {
            path.add(curr);
            if (curr != grid.startNode) totalPathCost += curr.weight;
            curr = curr.parent;
        }
        panel.setFinalPath(path);
    }

    private void checkNeighborUnweighted(MazeGrid.Cell curr, int dr, int dc, int wallIdx, Collection<MazeGrid.Cell> struct) {
        int nr = curr.r + dr, nc = curr.c + dc;
        if (grid.isValid(nr, nc) && !curr.walls[wallIdx] && !grid.cells[nr][nc].visitedSolve) {
            MazeGrid.Cell neighbor = grid.cells[nr][nc];
            neighbor.visitedSolve = true;
            neighbor.parent = curr;
            struct.add(neighbor);
        }
    }

    private void checkNeighborWeighted(MazeGrid.Cell curr, int dr, int dc, int wallIdx, String method, PriorityQueue<MazeGrid.Cell> pq) {
        int nr = curr.r + dr, nc = curr.c + dc;
        if (grid.isValid(nr, nc) && !curr.walls[wallIdx] && !grid.cells[nr][nc].visitedSolve) {
            MazeGrid.Cell neighbor = grid.cells[nr][nc];
            int newGCost = curr.gCost + neighbor.weight;
            if (newGCost < neighbor.gCost) {
                neighbor.gCost = newGCost;
                neighbor.parent = curr;
                neighbor.calculateFCost(grid.endNode, method);
                pq.add(neighbor);
            }
        }
    }

    private void addFrontier(MazeGrid.Cell cell, Stack<MazeGrid.Cell> frontier) {
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = cell.r + dr[i], nc = cell.c + dc[i];
            if (grid.isValid(nr, nc) && !grid.cells[nr][nc].visitedGen && !frontier.contains(grid.cells[nr][nc])) frontier.add(grid.cells[nr][nc]);
        }
    }

    private List<MazeGrid.Cell> getVisitedNeighbors(MazeGrid.Cell cell) {
        List<MazeGrid.Cell> list = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = cell.r + dr[i], nc = cell.c + dc[i];
            if (grid.isValid(nr, nc) && grid.cells[nr][nc].visitedGen) list.add(grid.cells[nr][nc]);
        }
        return list;
    }

    private void removeWalls(MazeGrid.Cell a, MazeGrid.Cell b) {
        int dr = a.r - b.r, dc = a.c - b.c;
        if (dr == 1) { a.walls[0] = false; b.walls[1] = false; }
        if (dr == -1) { a.walls[1] = false; b.walls[0] = false; }
        if (dc == 1) { a.walls[3] = false; b.walls[2] = false; }
        if (dc == -1) { a.walls[2] = false; b.walls[3] = false; }
    }

    private void addRandomLoops() {
        int loops = (MazeGrid.ROWS * MazeGrid.COLS) / 5;
        for(int i=0; i<loops; i++) {
            int r = (int)(Math.random()*(MazeGrid.ROWS-2)) + 1;
            int c = (int)(Math.random()*(MazeGrid.COLS-2)) + 1;
            MazeGrid.Cell cell = grid.cells[r][c];
            if(Math.random() > 0.5) removeWalls(cell, grid.cells[r][c+1]);
            else removeWalls(cell, grid.cells[r+1][c]);
        }
    }
}