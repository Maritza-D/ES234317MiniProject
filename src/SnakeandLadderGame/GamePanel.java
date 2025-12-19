package SnakeandLadderGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {
    private final int NODE_SIZE = 50;
    private final int PLAYER_SIZE = 50;
    private Point[] nodePositions;
    private BufferedImage bgImage, nodeReguler, nodePrime, nodeFinish;
    private BufferedImage imgGoldenApple, imgChain, imgLadder;
    private BufferedImage[] playerSkins = new BufferedImage[12];

    private GameLogic logic;
    private List<Player> players;
    private Font customFont;
    private SoundManager soundManager;

    private JButton btnResetOverlay;

    public GamePanel(GameLogic logic, List<Player> players, Font font, SoundManager sm, ActionListener resetAction) {
        this.logic = logic;
        this.players = players;
        this.customFont = font;
        this.soundManager = sm;

        setLayout(null);
        loadAssets();
        setupResetButton(resetAction);

        nodePositions = new Point[GameLogic.TOTAL_NODES];
    }

    private void setupResetButton(ActionListener action) {
        btnResetOverlay = new JButton("RESET");
        btnResetOverlay.setBounds(10, 10, 80, 30);
        btnResetOverlay.setBackground(new Color(255, 85, 85));
        btnResetOverlay.setForeground(Color.WHITE);
        btnResetOverlay.setFocusPainted(false);

        if (customFont != null) btnResetOverlay.setFont(customFont.deriveFont(12f));
        else btnResetOverlay.setFont(new Font("Arial", Font.BOLD, 12));

        btnResetOverlay.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        btnResetOverlay.addActionListener(action);

        add(btnResetOverlay);
    }

    private void loadAssets() {

        String basePath = "/SnakeandLadderGame/assets/";

        bgImage = loadImage(basePath + "mcbg1.jpg");
        nodeReguler = loadImage(basePath + "regulernode.png");
        nodePrime = loadImage(basePath + "primenode.png");
        nodeFinish = loadImage(basePath + "finishnode.png");
        imgGoldenApple = loadImage(basePath + "gldapl.png");

        BufferedImage rawChain = loadImage(basePath + "chain.png");
        imgChain = rotateImageCW90(rawChain);

        BufferedImage rawLadder = loadImage(basePath + "Ladder.png");
        imgLadder = rotateImageCW90(rawLadder);

        for (int i = 0; i < 6; i++) playerSkins[i] = loadImage(basePath + "gp" + (i + 1) + ".png");
        for (int i = 0; i < 6; i++) playerSkins[i + 6] = loadImage(basePath + "bp" + (i + 1) + ".png");
    }

    private BufferedImage loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.out.println("Image missing: " + path);
                return null;
            }
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage rotateImageCW90(BufferedImage img) {
        if (img == null) return null;
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage r = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = r.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate(h, 0); at.rotate(Math.PI/2);
        g.setTransform(at); g.drawImage(img, 0, 0, null); g.dispose();
        return r;
    }

    private void updateNodePositions() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        int cols = 5;
        int rows = 5;
        int stepX = 130;
        int stepY = 100;

        int totalMapWidth = (cols - 1) * stepX;
        int totalMapHeight = (rows - 1) * stepY;

        int startX = (w - totalMapWidth) / 2;
        int startY = (h + totalMapHeight) / 2 - 50;

        int x = startX;
        int y = startY;
        boolean movingRight = true;

        for (int i = 0; i < GameLogic.TOTAL_NODES; i++) {
            nodePositions[i] = new Point(x, y);
            if ((i + 1) % 5 == 0) {
                y -= stepY;
                movingRight = !movingRight;
            } else {
                if (movingRight) x += stepX; else x -= stepX;
            }
        }
    }

    public void playStepSound() {
        if(soundManager != null) soundManager.playStep();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        updateNodePositions();
        if (nodePositions[0] == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        else { g2.setColor(new Color(135, 206, 235)); g2.fillRect(0, 0, getWidth(), getHeight()); }

        for (int i = 0; i < GameLogic.TOTAL_NODES - 1; i++)
            drawTiledImage(g2, imgChain, nodePositions[i], nodePositions[i + 1], 30);
        for (var e : logic.randomConnections.entrySet())
            drawTiledImage(g2, imgLadder, nodePositions[e.getKey()], nodePositions[e.getValue()], 35);

        for (int i = 0; i < GameLogic.TOTAL_NODES; i++) {
            Point p = nodePositions[i];
            BufferedImage img = (i==GameLogic.TOTAL_NODES-1)?nodeFinish:(logic.isPrime(i+1)?nodePrime:nodeReguler);
            if(img==null && logic.isPrime(i+1)) img = nodeReguler;

            if (img != null) g2.drawImage(img, p.x-NODE_SIZE/2, p.y-NODE_SIZE/2, NODE_SIZE, NODE_SIZE, null);
            else { g2.setColor(Color.GREEN); g2.fillRect(p.x-25, p.y-25, 50, 50); }

            if (logic.nodeScores.containsKey(i)) {
                if(imgGoldenApple!=null) g2.drawImage(imgGoldenApple, p.x-10, p.y-35, 20, 20, null);
                else { g2.setColor(Color.ORANGE); g2.fillOval(p.x-5, p.y-35, 10, 10); }
            }
            g2.setColor(Color.WHITE); g2.setFont(customFont!=null?customFont.deriveFont(14f):new Font("Arial",Font.BOLD,14));
            g2.setColor(Color.BLACK); g2.drawString(""+(i+1), p.x-6, p.y+4);
            g2.setColor(Color.WHITE); g2.drawString(""+(i+1), p.x-8, p.y+2);
        }

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i); Point pt = nodePositions[p.position];
            int ox = (i%2)*15-8, oy = (i/2)*15-20;
            if(playerSkins[p.skinIndex]!=null) g2.drawImage(playerSkins[p.skinIndex], pt.x+ox-25, pt.y+oy-25, 50, 50, null);
            else { g2.setColor(p.color); g2.fillRect(pt.x+ox-10, pt.y+oy-10, 20, 20); }
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,10)); g2.drawString(p.name, pt.x+ox-15, pt.y+oy-25);
        }
    }

    private void drawTiledImage(Graphics2D g2, BufferedImage img, Point p1, Point p2, int width) {
        if(img==null) return;
        double dx = p2.x - p1.x, dy = p2.y - p1.y, dist = Math.sqrt(dx*dx+dy*dy), angle = Math.atan2(dy, dx);
        AffineTransform old = g2.getTransform();
        g2.translate(p1.x, p1.y); g2.rotate(angle);
        double segLen = width * ((double)img.getHeight()/img.getWidth());
        for(double i=0; i<dist; i+=segLen) {
            double dLen = (i+segLen>dist) ? dist-i : segLen;
            g2.drawImage(img, (int)i, -width/2, (int)(i+dLen), width/2, 0, 0, img.getWidth(), (int)(img.getHeight()*(dLen/segLen)), null);
        }
        g2.setTransform(old);
    }
}