package SnakeandLadderGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class SnakeGraphGame extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private SoundManager soundManager = new SoundManager();
    private MainMenuPanel menuPanel;
    private GamePanel gamePanel;

    private List<Player> allPlayers = new ArrayList<>();
    private LinkedList<Player> turnQueue = new LinkedList<>();
    private Player currentPlayer;
    private GameLogic logic;
    private Stack<String> moveHistoryStack = new Stack<>();

    private int currentRound = 1;
    private final int MAX_ROUNDS = 3;
    private boolean isDiceRolling = false;
    private int currentDiceValue = 1;
    private final double SAFE_PROBABILITY = 0.80;

    private JTextArea logArea;
    private JList<String> combinedListUI;
    private DefaultListModel<String> combinedListModel;
    private DiceVisualPanel diceVisual;
    private JLabel lblStatus, lblTurn, lblRound;
    private JButton btnAction;
    private Font minecraftFont;
    private BufferedImage[] skinPreviews = new BufferedImage[12];

    public SnakeGraphGame() {
        setTitle("Minecraft Adventure: Skyblock Graph");
        setSize(1150, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadFont();
        loadSkinPreviews();

        menuPanel = new MainMenuPanel(
                e -> showSetupDialog(),
                e -> showSettingsDialog()
        );

        mainPanel.add(menuPanel, "MENU");
        add(mainPanel);

        soundManager.playBGM();
    }

    private void showSettingsDialog() {
        soundManager.playSFX("click");
        JDialog d = new JDialog(this, "Game Settings", true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(40, 40, 40));

        JLabel lblTitle = new JLabel("AUDIO SETTINGS");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(minecraftFont.deriveFont(24f));

        JLabel lblMus = new JLabel("Music Volume:");
        lblMus.setForeground(Color.WHITE);
        lblMus.setFont(minecraftFont.deriveFont(14f));
        JSlider slMus = new JSlider(0, 100, soundManager.getMusicVolInt());
        styleSlider(slMus);
        slMus.addChangeListener(e -> soundManager.setMusicVolume(slMus.getValue() / 100f));

        JLabel lblSfx = new JLabel("SFX Volume:");
        lblSfx.setForeground(Color.WHITE);
        lblSfx.setFont(minecraftFont.deriveFont(14f));
        JSlider slSfx = new JSlider(0, 100, soundManager.getSFXVolInt());
        styleSlider(slSfx);
        slSfx.addChangeListener(e -> soundManager.setSFXVolume(slSfx.getValue() / 100f));

        JButton btnOk = new JButton("DONE");
        styleButton(btnOk);
        btnOk.addActionListener(e -> d.dispose());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx=0; gbc.gridy=0; p.add(lblTitle, gbc);
        gbc.gridy=1; p.add(lblMus, gbc);
        gbc.gridy=2; p.add(slMus, gbc);
        gbc.gridy=3; p.add(lblSfx, gbc);
        gbc.gridy=4; p.add(slSfx, gbc);
        gbc.gridy=5; p.add(btnOk, gbc);

        d.add(p);
        d.setVisible(true);
    }

    private void styleSlider(JSlider s) {
        s.setOpaque(false);
        s.setForeground(Color.WHITE);
        s.setBackground(new Color(40, 40, 40));
    }

    private void showSetupDialog() {
        soundManager.playSFX("click");
        JDialog dialog = new JDialog(this, "Server Setup", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(new Color(60, 60, 60));
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        ((JPanel)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel lblAsk = new JLabel("Enter Number of Players (2-6):");
        lblAsk.setForeground(Color.WHITE);
        lblAsk.setFont(minecraftFont.deriveFont(16f));

        JTextField txtInput = new JTextField("2");
        txtInput.setFont(minecraftFont.deriveFont(16f));
        txtInput.setPreferredSize(new Dimension(100, 30));
        txtInput.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        txtInput.setHorizontalAlignment(JTextField.CENTER);

        JButton btnNext = new JButton("NEXT STEP");
        styleButton(btnNext);

        JLabel lblError = new JLabel(" ");
        lblError.setForeground(new Color(255, 85, 85));
        lblError.setFont(new Font("Arial", Font.BOLD, 12));

        btnNext.addActionListener(e -> {
            soundManager.playSFX("click");
            try {
                int n = Integer.parseInt(txtInput.getText());
                if (n >= 2 && n <= 6) {
                    dialog.dispose();
                    startPlayerNameInput(n);
                } else {
                    lblError.setText("Error: Must be between 2 and 6!");
                }
            } catch (NumberFormatException ex) {
                lblError.setText("Error: Please enter a number!");
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(lblAsk, gbc);
        gbc.gridy = 1; dialog.add(txtInput, gbc);
        gbc.gridy = 2; dialog.add(lblError, gbc);
        gbc.gridy = 3; dialog.add(btnNext, gbc);

        dialog.setVisible(true);
    }

    private void startPlayerNameInput(int count) {
        allPlayers.clear();
        turnQueue.clear();
        for (int i = 0; i < count; i++) {
            String name = showNameInputDialog(i + 1);
            int skinIdx = showSkinSelector(name);
            Color c = (i % 2 == 0) ? Color.BLUE : Color.RED;
            Player p = new Player(name, i + 1, skinIdx, c);
            allPlayers.add(p);
            turnQueue.add(p);
        }
        currentRound = 1;
        startGame(true);
    }

    private void startGame(boolean resetScores) {
        logic = new GameLogic();
        logic.generateRandomConnections();
        logic.generateRandomScores();

        for(Player p : allPlayers) {
            p.position = 0;
            if (resetScores) {
                p.score = 0;
                p.wins = 0;
            }
        }

        Collections.shuffle(allPlayers);

        turnQueue.clear();
        turnQueue.addAll(allPlayers);
        currentPlayer = turnQueue.poll();
        moveHistoryStack.clear();

        JPanel gameContainer = createGameUI();
        mainPanel.add(gameContainer, "GAME");
        cardLayout.show(mainPanel, "GAME");
        soundManager.stopWinMusic();
        soundManager.playBGM();

        log("Welcome to Skyblock!");
        log("ROUND " + currentRound + " START!");
        log("Turn Order Shuffled!");
        updateUIComponents();
    }

    private JPanel createGameUI() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(400, 750));
        controlPanel.setBackground(new Color(50, 50, 50));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(new Color(198, 198, 198));
        top.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        lblRound = new JLabel("ROUND: " + currentRound + " / " + MAX_ROUNDS);
        lblRound.setFont(minecraftFont.deriveFont(24f));
        lblRound.setForeground(new Color(139, 0, 0));
        lblRound.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 5, 2, 5);
        top.add(lblRound, gbc);

        lblTurn = new JLabel("TURN: " + currentPlayer.name);
        lblTurn.setFont(minecraftFont.deriveFont(20f));
        lblTurn.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 5, 5, 5);
        top.add(lblTurn, gbc);

        diceVisual = new DiceVisualPanel();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        top.add(diceVisual, gbc);

        lblStatus = new JLabel("Ready to Craft!");
        lblStatus.setFont(minecraftFont.deriveFont(14f));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 10, 5);
        top.add(lblStatus, gbc);

        btnAction = new JButton("KOCOK DADU");
        styleButton(btnAction);
        btnAction.addActionListener(e -> {
            soundManager.playSFX("click");
            if (!isDiceRolling) startRolling(); else stopAndMove();
        });
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 20, 10, 20);
        top.add(btnAction, gbc);

        combinedListModel = new DefaultListModel<>();
        combinedListUI = new JList<>(combinedListModel);
        combinedListUI.setFont(minecraftFont.deriveFont(16f));
        combinedListUI.setBackground(new Color(220, 220, 220));

        TitledBorder listBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "PLAYERS & SCORES");
        listBorder.setTitleFont(minecraftFont.deriveFont(12f));

        JScrollPane scrollList = new JScrollPane(combinedListUI);
        scrollList.setBorder(listBorder);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(minecraftFont.deriveFont(12f));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(Color.WHITE);

        TitledBorder logBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "Adventure Log");
        logBorder.setTitleFont(minecraftFont.deriveFont(12f));
        logBorder.setTitleColor(Color.BLACK);

        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(logBorder);
        scrollLog.setPreferredSize(new Dimension(360, 150));

        JPanel centerP = new JPanel(new BorderLayout(0, 10));
        centerP.setOpaque(false);
        centerP.add(scrollList, BorderLayout.CENTER);
        centerP.add(scrollLog, BorderLayout.SOUTH);

        controlPanel.add(top, BorderLayout.NORTH);
        controlPanel.add(centerP, BorderLayout.CENTER);

        gamePanel = new GamePanel(logic, allPlayers, minecraftFont, soundManager, e -> {
            soundManager.playSFX("click");
            currentRound = 1;
            startGame(true);
        });

        container.add(gamePanel, BorderLayout.CENTER);
        container.add(controlPanel, BorderLayout.EAST);
        return container;
    }

    private void startRolling() {
        isDiceRolling = true; btnAction.setText("STOP!");
        new Thread(() -> {
            while (isDiceRolling) {
                currentDiceValue = (int)(Math.random()*6)+1;
                diceVisual.setValue(currentDiceValue);
                try { Thread.sleep(60); } catch(Exception e){}
            }
        }).start();
    }

    private void stopAndMove() {
        isDiceRolling = false; btnAction.setEnabled(false);

        int luckScore = (int)(Math.random() * 100) + 1;
        boolean isGreen = luckScore > 20;

        String statusTxt;
        if (isGreen) {
            statusTxt = "(Maju! - Luck: " + luckScore + "%)";
        } else {
            statusTxt = "(Mundur! - Luck: " + luckScore + "%)";
            soundManager.playSFX("damage");
        }

        int target = isGreen ? currentPlayer.position + currentDiceValue : currentPlayer.position - currentDiceValue;
        if(target < 0) target = 0; if(target >= GameLogic.TOTAL_NODES) target = GameLogic.TOTAL_NODES-1;

        boolean useDijkstra = isGreen && logic.isPrime(currentPlayer.position + 1);

        log("\n--- " + currentPlayer.name + " ---");
        log("Rolled: " + currentDiceValue + " " + statusTxt);

        List<Integer> path;
        if (useDijkstra) {
            log("[PRIME] Algorithmic Calculation...");
            path = logic.getShortestPath(currentPlayer.position, target);
        } else {
            path = new ArrayList<>();
            if (isGreen) {
                if (currentPlayer.position < target) {
                    for(int i=currentPlayer.position+1; i<=target; i++) path.add(i);
                }
            } else {
                if (currentPlayer.position > target) {
                    for(int i=currentPlayer.position-1; i>=target; i--) path.add(i);
                }
            }
        }

        new Thread(() -> {
            try {
                moveHistoryStack.push("Start: " + (currentPlayer.position+1));

                for(int next : path) {
                    Thread.sleep(400);
                    currentPlayer.position = next;
                    soundManager.playStep();
                    moveHistoryStack.push("-> " + (next+1));
                    log("Step: " + (next+1));
                    SwingUtilities.invokeLater(() -> {
                        if(logic.nodeScores.containsKey(next)) {
                            int coin = logic.nodeScores.get(next);
                            currentPlayer.score += coin;
                            soundManager.playSFX("level_up");
                            log("Found Golden Apple! +" + coin);
                            logic.nodeScores.remove(next);
                            updateUIComponents();
                        }
                    });
                    gamePanel.repaint();
                }

                if (isGreen && logic.randomConnections.containsKey(currentPlayer.position)) {
                    Thread.sleep(300);
                    int jumpTarget = logic.randomConnections.get(currentPlayer.position);

                    log("FOUND LADDER! Climbing to " + (jumpTarget + 1));
                    soundManager.playSFX("level_up");

                    Thread.sleep(600);
                    currentPlayer.position = jumpTarget;
                    gamePanel.repaint();

                    moveHistoryStack.push("-> LADDER -> " + (jumpTarget + 1));
                }

                if (currentPlayer.position == GameLogic.TOTAL_NODES - 1) {
                    currentPlayer.score += 50;
                    soundManager.playSFX("level_up");
                    log("FINISH BONUS! +50 Pts");
                    Thread.sleep(1000);

                    if (currentRound < MAX_ROUNDS) {
                        showRoundCompleteDialog();
                    } else {
                        showGrandVictoryScreen();
                    }
                } else {
                    int finalPos = currentPlayer.position + 1;
                    if (finalPos % 5 == 0 && finalPos != 1) {
                        log("CHECKPOINT BONUS! Roll again.");
                        soundManager.playSFX("level_up");
                        turnQueue.addFirst(currentPlayer);
                    } else {
                        turnQueue.addLast(currentPlayer);
                    }
                    currentPlayer = turnQueue.poll();
                    SwingUtilities.invokeLater(this::updateUIComponents);
                }
                moveHistoryStack.clear();
            } catch(Exception e) { e.printStackTrace(); }
            finally {
                soundManager.stopStep();
                SwingUtilities.invokeLater(() -> btnAction.setEnabled(true));
            }
        }).start();
    }

    private void showRoundCompleteDialog() {
        SwingUtilities.invokeLater(() -> {
            soundManager.playWinMusic();
            JDialog d = new JDialog(this, "Round Complete", true);
            d.setSize(400, 250);
            d.setLocationRelativeTo(this);
            d.setUndecorated(true);
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(new Color(50, 50, 50));
            p.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            JLabel lbl = new JLabel("ROUND " + currentRound + " ENDED!");
            lbl.setForeground(Color.YELLOW);
            lbl.setFont(minecraftFont.deriveFont(24f));
            JLabel lblWin = new JLabel("Winner: " + currentPlayer.name);
            lblWin.setForeground(Color.WHITE);
            lblWin.setFont(minecraftFont.deriveFont(16f));
            JButton btnNext = new JButton("START ROUND " + (currentRound+1));
            styleButton(btnNext);
            btnNext.addActionListener(e -> {
                d.dispose();
                soundManager.stopWinMusic();
                currentRound++;
                startGame(false);
            });
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10,10,10,10);
            gbc.gridy=0; p.add(lbl, gbc);
            gbc.gridy=1; p.add(lblWin, gbc);
            gbc.gridy=2; p.add(Box.createVerticalStrut(20), gbc);
            gbc.gridy=3; p.add(btnNext, gbc);
            d.add(p);
            d.setVisible(true);
        });
    }

    private void showGrandVictoryScreen() {
        soundManager.playWinMusic();
        Player grandWinner = allPlayers.stream().max(Comparator.comparingInt(p -> p.score)).orElse(currentPlayer);
        JPanel victoryPanel = new JPanel(new GridBagLayout());
        victoryPanel.setBackground(new Color(255, 215, 0));
        JLabel lblWin = new JLabel("GRAND WINNER!");
        lblWin.setFont(minecraftFont.deriveFont(50f));
        JLabel lblName = new JLabel(grandWinner.name);
        lblName.setFont(minecraftFont.deriveFont(40f));
        JLabel lblScore = new JLabel("Total Score: " + grandWinner.score);
        lblScore.setFont(minecraftFont.deriveFont(30f));
        JButton btnMenu = new JButton("BACK TO MENU");
        styleButton(btnMenu);
        btnMenu.addActionListener(e -> {
            soundManager.stopWinMusic();
            cardLayout.show(mainPanel, "MENU");
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy=0; victoryPanel.add(lblWin, gbc);
        gbc.gridy=1; victoryPanel.add(lblName, gbc);
        gbc.gridy=2; victoryPanel.add(lblScore, gbc);
        gbc.gridy=3; victoryPanel.add(Box.createVerticalStrut(50), gbc);
        gbc.gridy=4; victoryPanel.add(btnMenu, gbc);
        mainPanel.add(victoryPanel, "WIN");
        cardLayout.show(mainPanel, "WIN");
    }

    private String showNameInputDialog(int playerNum) {
        soundManager.playSFX("click");
        JDialog d = new JDialog(this, "Player Setup", true);
        d.setSize(400, 220);
        d.setLocationRelativeTo(this);
        d.setUndecorated(true);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(50, 50, 50));
        p.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        JLabel lbl = new JLabel("ENTER NAME FOR P" + playerNum);
        lbl.setForeground(Color.YELLOW);
        lbl.setFont(minecraftFont.deriveFont(20f));
        JTextField txt = new JTextField("Steve " + playerNum);
        txt.setFont(minecraftFont.deriveFont(16f));
        txt.setPreferredSize(new Dimension(250, 40));
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        JButton btn = new JButton("CONFIRM");
        styleButton(btn);
        final String[] result = { "" };
        btn.addActionListener(e -> {
            soundManager.playSFX("click");
            String input = txt.getText().trim();
            result[0] = input.isEmpty() ? "Player " + playerNum : input;
            d.dispose();
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx=0; gbc.gridy=0; p.add(lbl, gbc);
        gbc.gridy=1; p.add(txt, gbc);
        gbc.gridy=2; p.add(btn, gbc);
        d.add(p);
        d.setVisible(true);
        return result[0];
    }

    private int showSkinSelector(String name) {
        JPanel panel = new JPanel(new GridLayout(3, 4, 10, 10));
        final int[] selected = {0};
        JDialog dialog = new JDialog(this, "Skin: " + name, true);
        for (int k = 0; k < 12; k++) {
            final int idx = k; JButton btn = new JButton();
            if (skinPreviews[k] != null) btn.setIcon(new ImageIcon(skinPreviews[k].getScaledInstance(64,64,Image.SCALE_SMOOTH)));
            else btn.setText(""+(k+1));
            btn.addActionListener(e -> { selected[0] = idx; dialog.dispose(); });
            panel.add(btn);
        }
        dialog.add(new JScrollPane(panel)); dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this); dialog.setVisible(true);
        return selected[0];
    }

    private void updateUIComponents() {
        combinedListModel.clear();

        combinedListModel.addElement(">> CURRENT TURN: " + currentPlayer.name);
        combinedListModel.addElement("-----------------------");

        allPlayers.sort((p1, p2) -> p2.score - p1.score);

        int i=1;
        for(Player p : allPlayers) {
            String txt = i + ". " + p.name + " (Score: " + p.score + ")";
            combinedListModel.addElement(txt);
            i++;
        }

        lblTurn.setText("TURN: " + currentPlayer.name);
        btnAction.setText("KOCOK DADU");
    }

    private void styleButton(JButton btn) {
        btn.setFont(minecraftFont != null ? minecraftFont.deriveFont(16f) : new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    private void log(String t) {
        SwingUtilities.invokeLater(() -> { logArea.append(t + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); });
    }

    private void loadFont() {
        try {
            java.net.URL fontUrl = getClass().getResource("/SnakeandLadderGame/assets/Minecraft.ttf");
            if(fontUrl != null) {
                minecraftFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(minecraftFont);
            }
        } catch (Exception e) {}
    }

    private void loadSkinPreviews() {
        String basePath = "/SnakeandLadderGame/assets/";
        try {
            for(int i=0; i<6; i++) {
                java.net.URL u1 = getClass().getResource(basePath + "gp" + (i+1) + ".png");
                if (u1 != null) skinPreviews[i] = ImageIO.read(u1);

                java.net.URL u2 = getClass().getResource(basePath + "bp" + (i+1) + ".png");
                if (u2 != null) skinPreviews[i+6] = ImageIO.read(u2);
            }
        } catch(Exception e) { System.out.println("Gagal load preview skin"); }
    }

    class DiceVisualPanel extends JPanel {
        private int value=1; private Color c=Color.BLACK;
        public DiceVisualPanel() {
            setPreferredSize(new Dimension(80,80));
            setMaximumSize(new Dimension(80,80));
            setOpaque(false);
        }
        public void setValue(int v) { value=v; repaint(); }
        public void setColor(Color clr) { c=clr; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 10;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, size, size, 20, 20);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x, y, size, size, 20, 20);

            g2.setColor(c);
            int dotSize = size / 5;
            int center = x + size / 2;
            int left = x + size / 4;
            int right = x + size * 3 / 4;
            int top = y + size / 4;
            int bottom = y + size * 3 / 4;

            if(value%2!=0) fillDot(g2, center, center, dotSize);
            if(value>1){ fillDot(g2, left, top, dotSize); fillDot(g2, right, bottom, dotSize); }
            if(value>3){ fillDot(g2, right, top, dotSize); fillDot(g2, left, bottom, dotSize); }
            if(value==6){ fillDot(g2, left, center, dotSize); fillDot(g2, right, center, dotSize); }
        }

        private void fillDot(Graphics2D g, int cx, int cy, int s) {
            g.fillOval(cx - s/2, cy - s/2, s, s);
        }
    }
}