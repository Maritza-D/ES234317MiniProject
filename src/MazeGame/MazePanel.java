package MazeGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {
    private MazeGrid grid;
    private MazeGameMain mainFrame;
    private BufferedImage imgGrass, imgMud, imgWater, imgPlayer, imgFinish, imgWall;
    private final Color COLOR_GRASS = new Color(34, 139, 34);
    private final Color COLOR_MUD = new Color(139, 69, 19);
    private final Color COLOR_WATER = new Color(30, 144, 255);
    private boolean isPlayerMode = false;
    private MazeGrid.Cell playerCell;
    private int playerTotalCost = 0;
    private MazeGrid.Cell currentSearchNode;
    private List<MazeGrid.Cell> finalPath = new ArrayList<>();

    public MazePanel(MazeGrid grid, MazeGameMain mainFrame) {
        this.grid = grid;
        this.mainFrame = mainFrame;
        loadImages();
        this.setFocusable(true);
        this.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { requestFocusInWindow(); }
        });
        this.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (isPlayerMode) handlePlayerMove(e.getKeyCode());
            }
        });
    }

    private void loadImages() {
        imgGrass = smartLoad("grass.png");
        imgMud = smartLoad("mud.png");
        imgWater = smartLoad("water.png");
        imgFinish = smartLoad("finish.png");
        imgWall = smartLoad("wall.png");
        imgPlayer = smartLoad("player1.png");
    }

    private BufferedImage smartLoad(String name) {
        try {
            URL url = getClass().getResource("assets/" + name);
            if (url == null) url = getClass().getResource("/MazeGame/assets/" + name);
            if (url != null) return ImageIO.read(url);
        } catch(Exception e) { }
        return null;
    }

    public void setPlayerSkin(int skinIndex) {
        BufferedImage newSkin = smartLoad("player" + skinIndex + ".png");
        if (newSkin != null) { this.imgPlayer = newSkin; repaint(); }
    }

    public void startPlayerMode() {
        grid.resetSolverData();
        clearPath();
        currentSearchNode = null;
        isPlayerMode = true;
        playerCell = grid.startNode;
        playerTotalCost = 0;
        finalPath.add(playerCell);
        mainFrame.updateStatus("MAIN SENDIRI: Klik Peta & Gunakan Panah!");
        this.requestFocusInWindow();
        repaint();
    }

    public void resetPlayerMode() { isPlayerMode = false; playerCell = null; repaint(); }
    public void setCurrentSearchNode(MazeGrid.Cell cell) { this.currentSearchNode = cell; }
    public void setFinalPath(List<MazeGrid.Cell> path) { this.finalPath = path; }
    public void clearPath() { finalPath.clear(); currentSearchNode = null; }

    private void handlePlayerMove(int keyCode) {
        if (!isPlayerMode || playerCell == null) return;
        int r = playerCell.r; int c = playerCell.c;
        MazeGrid.Cell nextCell = null;
        if (keyCode == KeyEvent.VK_UP && !playerCell.walls[0]) nextCell = grid.cells[r - 1][c];
        else if (keyCode == KeyEvent.VK_DOWN && !playerCell.walls[1]) nextCell = grid.cells[r + 1][c];
        else if (keyCode == KeyEvent.VK_RIGHT && !playerCell.walls[2]) nextCell = grid.cells[r][c + 1];
        else if (keyCode == KeyEvent.VK_LEFT && !playerCell.walls[3]) nextCell = grid.cells[r][c - 1];

        if (nextCell != null) {
            playerCell = nextCell;
            playerTotalCost += playerCell.weight;
            finalPath.add(playerCell);
            mainFrame.updateStatus("Biaya: " + playerTotalCost);
            SoundManager.playSFX("step.wav");
            if (playerCell == grid.endNode) {
                SoundManager.playSFX("win.wav");
                JLabel msg = new JLabel("YIPPIE, sampai tujuan! Total Biaya: " + playerTotalCost);
                if (MazeGameMain.mcFont != null) msg.setFont(MazeGameMain.mcFont.deriveFont(18f));
                JOptionPane.showMessageDialog(this, msg);
                isPlayerMode = false;
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xOff = (getWidth() - (MazeGrid.COLS * MazeGrid.CELL_SIZE)) / 2;
        int yOff = (getHeight() - (MazeGrid.ROWS * MazeGrid.CELL_SIZE)) / 2;

        for (int r = 0; r < MazeGrid.ROWS; r++) {
            for (int c = 0; c < MazeGrid.COLS; c++) {
                MazeGrid.Cell cell = grid.cells[r][c];
                int x = xOff + c * MazeGrid.CELL_SIZE;
                int y = yOff + r * MazeGrid.CELL_SIZE;

                if (cell.weight == MazeGrid.COST_GRASS) {
                    if(imgGrass!=null) g2.drawImage(imgGrass, x, y, MazeGrid.CELL_SIZE, MazeGrid.CELL_SIZE, null);
                    else { g2.setColor(COLOR_GRASS); g2.fillRect(x, y, MazeGrid.CELL_SIZE, MazeGrid.CELL_SIZE); }
                } else if (cell.weight == MazeGrid.COST_MUD) {
                    if(imgMud!=null) g2.drawImage(imgMud, x, y, MazeGrid.CELL_SIZE, MazeGrid.CELL_SIZE, null);
                    else { g2.setColor(COLOR_MUD); g2.fillRect(x, y, MazeGrid.CELL_SIZE, MazeGrid.CELL_SIZE); }
                } else {
                    if(imgWater!=null) g2.drawImage(imgWater, x, y, MazeGrid.CELL_SIZE, MazeGrid.CELL_SIZE, null);
                    else { g2.setColor(COLOR_WATER); g2.fillRect(x, y, MazeGrid.CELL_SIZE, MazeGrid.CELL_SIZE); }
                }

                if (finalPath.contains(cell)) {
                    g2.setColor(new Color(255, 0, 0, 100));
                    g2.fillRect(x + 5, y + 5, MazeGrid.CELL_SIZE - 10, MazeGrid.CELL_SIZE - 10);
                }

                if ((isPlayerMode && cell == playerCell) || (cell == currentSearchNode && !isPlayerMode)) {
                    if(imgPlayer != null) g2.drawImage(imgPlayer, x+2, y+2, MazeGrid.CELL_SIZE-4, MazeGrid.CELL_SIZE-4, null);
                    else { g2.setColor(Color.YELLOW); g2.fillOval(x+5, y+5, MazeGrid.CELL_SIZE-10, MazeGrid.CELL_SIZE-10); }
                }

                if (cell == grid.startNode) {
                    drawShadowText(g2, "S", x, y, Color.BLUE);
                }
                else if (cell == grid.endNode) {
                    if(imgFinish!=null) g2.drawImage(imgFinish, x, y, MazeGrid.CELL_SIZE, MazeGrid.CELL_SIZE, null);
                    else drawShadowText(g2, "F", x, y, Color.RED);
                }

                g2.setColor(Color.WHITE);
                if(imgWall!=null) g2.setStroke(new BasicStroke(3)); else g2.setStroke(new BasicStroke(2));
                if (cell.walls[0]) g2.drawLine(x, y, x + MazeGrid.CELL_SIZE, y);
                if (cell.walls[1]) g2.drawLine(x, y + MazeGrid.CELL_SIZE, x + MazeGrid.CELL_SIZE, y + MazeGrid.CELL_SIZE);
                if (cell.walls[2]) g2.drawLine(x + MazeGrid.CELL_SIZE, y, x + MazeGrid.CELL_SIZE, y + MazeGrid.CELL_SIZE);
                if (cell.walls[3]) g2.drawLine(x, y, x, y + MazeGrid.CELL_SIZE);
            }
        }
        drawLegend(g2);
    }

    private void drawShadowText(Graphics2D g2, String text, int cellX, int cellY, Color color) {
        if (MazeGameMain.mcFont != null) g2.setFont(MazeGameMain.mcFont.deriveFont(16f));
        else g2.setFont(new Font("Arial", Font.BOLD, 16));

        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getAscent();

        int textX = cellX + (MazeGrid.CELL_SIZE - textW) / 2;
        int textY = cellY + (MazeGrid.CELL_SIZE + textH) / 2 - 2;

        g2.setColor(Color.BLACK);
        g2.drawString(text, textX + 1, textY + 1);

        g2.setColor(color);
        g2.drawString(text, textX, textY);
    }

    private void drawLegend(Graphics2D g2) {
        int lx = 10, ly = 10, bs = 15;
        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillRoundRect(lx-5, ly-5, 180, 85, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(lx-5, ly-5, 180, 85, 10, 10);

        if (MazeGameMain.mcFont != null) g2.setFont(MazeGameMain.mcFont.deriveFont(12f));

        if(imgGrass!=null) g2.drawImage(imgGrass, lx, ly, bs, bs, null);
        else { g2.setColor(COLOR_GRASS); g2.fillRect(lx, ly, bs, bs); }
        g2.setColor(Color.BLACK); g2.drawRect(lx, ly, bs, bs);
        g2.drawString("Rumput (+1)", lx+25, ly+12);

        if(imgMud!=null) g2.drawImage(imgMud, lx, ly+25, bs, bs, null);
        else { g2.setColor(COLOR_MUD); g2.fillRect(lx, ly+25, bs, bs); }
        g2.setColor(Color.BLACK); g2.drawRect(lx, ly+25, bs, bs);
        g2.drawString("Lumpur (+5)", lx+25, ly+37);

        if(imgWater!=null) g2.drawImage(imgWater, lx, ly+50, bs, bs, null);
        else { g2.setColor(COLOR_WATER); g2.fillRect(lx, ly+50, bs, bs); }
        g2.setColor(Color.BLACK); g2.drawRect(lx, ly+50, bs, bs);
        g2.drawString("Air (+10)", lx+25, ly+62);
    }
}