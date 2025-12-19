package MazeGame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL;

public class MazeGameMain extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MazeGrid grid;
    private MazePanel gamePanel;
    private MazeSolver solver;
    private JLabel lblStats;
    private int animationDelay = 20;

    public static Font mcFont = new Font("Monospaced", Font.BOLD, 14);

    private int currentSkinIndex = 1;
    private final int MAX_SKINS = 5;
    private JLabel lblSkinPreview;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeGameMain().setVisible(true));
    }

    public MazeGameMain() {
        loadMinecraftFont();
        setTitle("Block Breakout");
        setSize(980, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        SoundManager.setVolume(100);
        SoundManager.playMusic("bgm.wav");

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        grid = new MazeGrid();
        gamePanel = new MazePanel(grid, this);
        solver = new MazeSolver(grid, gamePanel, this);

        JPanel menuPanel = createMenuPanel();
        JPanel gameContainer = createGameInterface();

        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(gameContainer, "GAME");

        add(mainContainer);
    }

    private void loadMinecraftFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/MazeGame/assets/Minecraft.ttf");
            if (is == null) is = getClass().getResourceAsStream("assets/Minecraft.ttf");

            if (is != null) {
                Font ttfBase = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(ttfBase);
                mcFont = ttfBase.deriveFont(16f);
            }
        } catch (Exception e) { }
    }

    private JPanel createMenuPanel() {
        JPanel menu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                URL bgUrl = getClass().getResource("assets/menu_bg.png");
                if (bgUrl != null) {
                    ImageIcon icon = new ImageIcon(bgUrl);
                    g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(66, 40, 24));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        menu.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Judul
        JLabel title = new JLabel("BLOCK BREAKOUT");
        title.setFont(mcFont.deriveFont(48f));
        title.setForeground(Color.WHITE);
        title.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            public void paint(Graphics g, JComponent c) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(mcFont.deriveFont(48f));
                g.setColor(Color.BLACK); g.drawString("BLOCK BREAKOUT", 4, 44);
                g.setColor(Color.WHITE); g.drawString("BLOCK BREAKOUT", 0, 40);
            }
        });

        JPanel skinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        skinPanel.setOpaque(false);

        MinecraftButton btnPrev = new MinecraftButton("<");
        MinecraftButton btnNext = new MinecraftButton(">");
        btnPrev.setPreferredSize(new Dimension(50, 50));
        btnNext.setPreferredSize(new Dimension(50, 50));

        lblSkinPreview = new JLabel();
        lblSkinPreview.setPreferredSize(new Dimension(64, 64));
        lblSkinPreview.setHorizontalAlignment(SwingConstants.CENTER);
        updateSkinPreview();

        btnPrev.addActionListener(e -> {
            currentSkinIndex--;
            if (currentSkinIndex < 1) currentSkinIndex = MAX_SKINS;
            updateSkinPreview();
        });
        btnNext.addActionListener(e -> {
            currentSkinIndex++;
            if (currentSkinIndex > MAX_SKINS) currentSkinIndex = 1;
            updateSkinPreview();
        });

        skinPanel.add(btnPrev); skinPanel.add(lblSkinPreview); skinPanel.add(btnNext);

        MinecraftButton btnPlay = new MinecraftButton("MAINKAN GAME");
        btnPlay.setPreferredSize(new Dimension(300, 50));
        btnPlay.setFont(mcFont.deriveFont(24f));
        btnPlay.addActionListener(e -> {
            gamePanel.setPlayerSkin(currentSkinIndex);
            cardLayout.show(mainContainer, "GAME");
            solver.stopTimer();
            gamePanel.startPlayerMode();
            gamePanel.requestFocusInWindow();
        });

        MinecraftButton btnQuit = new MinecraftButton("KELUAR GAME");
        btnQuit.setPreferredSize(new Dimension(300, 50));
        btnQuit.setFont(mcFont.deriveFont(24f));
        btnQuit.setBackground(new Color(200, 50, 50));
        btnQuit.addActionListener(e -> System.exit(0));

        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        volumePanel.setOpaque(false);

        JLabel lblVol = new JLabel("Volume: ");
        lblVol.setFont(mcFont.deriveFont(16f));
        lblVol.setForeground(Color.WHITE);

        JSlider sliderVol = new JSlider(0, 100, 100);
        sliderVol.setOpaque(false);
        sliderVol.setPreferredSize(new Dimension(200, 30));
        sliderVol.addChangeListener(e -> {
            SoundManager.setVolume(sliderVol.getValue());
        });

        volumePanel.add(lblVol);
        volumePanel.add(sliderVol);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        menu.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        JLabel lblChoose = new JLabel("PILIH KARAKTER:");
        lblChoose.setFont(mcFont.deriveFont(16f));
        lblChoose.setForeground(Color.WHITE);
        menu.add(lblChoose, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        menu.add(skinPanel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        menu.add(btnPlay, gbc);

        gbc.gridy = 4;
        menu.add(btnQuit, gbc);

        gbc.gridy = 5; // Posisi Slider di paling bawah
        gbc.insets = new Insets(20, 0, 0, 0);
        menu.add(volumePanel, gbc);

        return menu;
    }

    private void updateSkinPreview() {
        try {
            URL url = getClass().getResource("assets/player" + currentSkinIndex + ".png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                lblSkinPreview.setIcon(new ImageIcon(img));
                lblSkinPreview.setText("");
            } else lblSkinPreview.setText("?");
        } catch (Exception e) { lblSkinPreview.setText("Error"); }
    }

    private JPanel createGameInterface() {
        lblStats = new JLabel("Status: Siap. Klik 'Buat World' untuk mulai.");
        lblStats.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblStats.setFont(mcFont.deriveFont(14f));
        lblStats.setForeground(Color.WHITE);
        lblStats.setOpaque(true);
        lblStats.setBackground(Color.BLACK);

        JPanel fullGamePanel = new JPanel(new BorderLayout());
        fullGamePanel.add(gamePanel, BorderLayout.CENTER);
        fullGamePanel.add(createControlPanel(), BorderLayout.SOUTH);
        return fullGamePanel;
    }

    private JPanel createControlPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new GridLayout(2, 5, 8, 8));
        controls.setBackground(new Color(198, 198, 198));
        controls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        MinecraftButton btnGen = new MinecraftButton("Buat World");
        MinecraftButton btnTerrain = new MinecraftButton("Acak Biome");
        MinecraftButton btnReset = new MinecraftButton("Reset");
        MinecraftButton btnInfo = new MinecraftButton("Info");
        MinecraftButton btnBack = new MinecraftButton("<< Menu");
        MinecraftButton btnBFS = new MinecraftButton("BFS");
        MinecraftButton btnDFS = new MinecraftButton("DFS");
        MinecraftButton btnDijkstra = new MinecraftButton("Dijkstra");
        MinecraftButton btnAStar = new MinecraftButton("A*");
        MinecraftButton btnPlay = new MinecraftButton("Main Sendiri");

        btnGen.addActionListener(e -> solver.startGeneration());
        btnTerrain.addActionListener(e -> { grid.randomizeTerrain(); gamePanel.repaint(); });
        btnReset.addActionListener(e -> {
            solver.stopTimer(); grid.initGrid(); gamePanel.resetPlayerMode();
            gamePanel.repaint(); updateStatus("Status: World Reset.");
        });
        btnInfo.addActionListener(e -> showAlgorithmInfo());
        btnBack.addActionListener(e -> { solver.stopTimer(); cardLayout.show(mainContainer, "MENU"); });
        btnBFS.addActionListener(e -> solver.startUnweightedSearch("BFS"));
        btnDFS.addActionListener(e -> solver.startUnweightedSearch("DFS"));
        btnDijkstra.addActionListener(e -> solver.startWeightedSearch("DIJKSTRA"));
        btnAStar.addActionListener(e -> solver.startWeightedSearch("ASTAR"));
        btnPlay.addActionListener(e -> { solver.stopTimer(); gamePanel.startPlayerMode(); });

        controls.add(btnGen); controls.add(btnTerrain); controls.add(btnBFS); controls.add(btnDijkstra); controls.add(btnPlay);
        controls.add(btnReset); controls.add(btnBack); controls.add(btnDFS); controls.add(btnAStar); controls.add(btnInfo);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        speedPanel.setBackground(new Color(198, 198, 198));
        JSlider sliderSpeed = new JSlider(JSlider.HORIZONTAL, 1, 100, 20);
        sliderSpeed.setInverted(true);
        sliderSpeed.setPreferredSize(new Dimension(300, 40));
        sliderSpeed.setBackground(new Color(198, 198, 198));
        sliderSpeed.addChangeListener(e -> animationDelay = (101 - sliderSpeed.getValue()));
        sliderSpeed.setValue(80);

        JLabel lblCepat = new JLabel("Cepat"); lblCepat.setFont(mcFont);
        JLabel lblLambat = new JLabel("Lambat"); lblLambat.setFont(mcFont);

        speedPanel.add(lblCepat); speedPanel.add(sliderSpeed); speedPanel.add(lblLambat);
        JPanel combinedControl = new JPanel(new BorderLayout());
        combinedControl.add(speedPanel, BorderLayout.NORTH);
        combinedControl.add(controls, BorderLayout.CENTER);

        bottomPanel.add(combinedControl, BorderLayout.CENTER);
        bottomPanel.add(lblStats, BorderLayout.SOUTH);
        return bottomPanel;
    }

    public void updateStatus(String text) { lblStats.setText(text); }
    public int getAnimationDelay() { return animationDelay; }

    private void showAlgorithmInfo() {
        JTextArea textArea = new JTextArea("BFS: Menyebar ke semua arah selangkah demi selangkah. \nDFS: Menelusuri satu jalur sampai habis, baru ke jalur lain.\nDijkstra: Mencari jalur dengan biaya paling murah.\nA*: Mencari jalur tercepat dengan perkiraan jarak tujuan.");
        textArea.setFont(mcFont.deriveFont(14f));
        textArea.setEditable(false);
        textArea.setBackground(new Color(230,230,230));
        JOptionPane.showMessageDialog(this, textArea, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class MinecraftButton extends JButton {
        private final Color BG_NORMAL = new Color(110, 110, 110);
        private final Color BG_HOVER = new Color(130, 130, 160);
        private final Color BG_PRESSED = new Color(80, 80, 80);
        private final Color BORDER_LIGHT = new Color(160, 160, 160);
        private final Color BORDER_DARK = new Color(50, 50, 50);
        private boolean isHovered = false;

        public MinecraftButton(String text) {
            super(text);
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setFont(MazeGameMain.mcFont.deriveFont(14f));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(); int h = getHeight();
            if (getModel().isPressed()) g2.setColor(BG_PRESSED);
            else if (isHovered) g2.setColor(BG_HOVER);
            else g2.setColor(BG_NORMAL);
            g2.fillRect(2, 2, w - 4, h - 4);
            g2.setColor(Color.BLACK);
            g2.drawRect(0, 0, w - 1, h - 1);
            g2.drawRect(1, 1, w - 3, h - 3);
            g2.setColor(BORDER_LIGHT);
            g2.drawLine(2, 2, w - 3, 2);
            g2.drawLine(2, 2, 2, h - 3);
            g2.setColor(BORDER_DARK);
            g2.drawLine(w - 3, 3, w - 3, h - 3);
            g2.drawLine(3, h - 3, w - 3, h - 3);
            g2.setColor(new Color(30, 30, 30));
            FontMetrics metrics = g2.getFontMetrics(getFont());
            int textX = (w - metrics.stringWidth(getText())) / 2;
            int textY = ((h - metrics.getHeight()) / 2) + metrics.getAscent();
            g2.drawString(getText(), textX + 2, textY + 2);
            g2.setColor(getForeground());
            g2.drawString(getText(), textX, textY);
        }
    }
}