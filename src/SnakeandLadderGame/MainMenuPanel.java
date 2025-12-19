package SnakeandLadderGame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class MainMenuPanel extends JPanel {
    private BufferedImage bgImage;
    private JButton btnStart, btnSettings, btnExit;
    private Font minecraftFont;

    public MainMenuPanel(ActionListener startAction, ActionListener settingsAction) {
        setLayout(new GridBagLayout());
        loadAssets();

        JLabel lblTitle = new JLabel("SKYBLOCK GRAPH");
        lblTitle.setForeground(Color.WHITE);
        if (minecraftFont != null) lblTitle.setFont(minecraftFont.deriveFont(48f));
        else lblTitle.setFont(new Font("Arial", Font.BOLD, 48));

        JLabel lblShadow = new JLabel("SKYBLOCK GRAPH");
        lblShadow.setForeground(Color.BLACK);
        lblShadow.setFont(lblTitle.getFont());

        btnStart = createButton("START ADVENTURE");
        btnStart.addActionListener(startAction);

        btnSettings = createButton("SETTINGS");
        btnSettings.addActionListener(settingsAction);

        btnExit = createButton("EXIT GAME");
        btnExit.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        gbc.gridy = 0; add(lblTitle, gbc);
        gbc.gridy = 1; add(Box.createVerticalStrut(30), gbc);
        gbc.gridy = 2; add(btnStart, gbc);
        gbc.gridy = 3; add(btnSettings, gbc);
        gbc.gridy = 4; add(btnExit, gbc);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(300, 50));

        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);

        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        if (minecraftFont != null) btn.setFont(minecraftFont.deriveFont(20f));
        return btn;
    }

    private void loadAssets() {
        try {
            // Sama, path-nya sesuaikan dengan package
            String basePath = "/SnakeandLadderGame/assets/";

            java.net.URL bgUrl = getClass().getResource(basePath + "mcbg1.jpg");
            if (bgUrl != null) bgImage = ImageIO.read(bgUrl);

            java.net.URL fontUrl = getClass().getResource(basePath + "Minecraft.ttf");
            if(fontUrl != null) {
                // Font butuh InputStream kalau dari resource
                minecraftFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        else { g.setColor(new Color(50, 50, 50)); g.fillRect(0, 0, getWidth(), getHeight()); }
        g.setColor(new Color(0, 0, 0, 100)); g.fillRect(0, 0, getWidth(), getHeight());
    }
}