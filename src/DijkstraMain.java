import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;

public class DijkstraMain extends JFrame implements DijkstraLogic.Visualizer {

    private int[][] coords = {
            {50, 100},  // 0. Jakarta
            {200, 50},  // 1. Bandung
            {350, 50},  // 2. Semarang
            {200, 250}, // 3. Yogyakarta
            {350, 250}  // 4. Surabaya
    };

    private DijkstraLogic logic;
    private GraphPanel graphPanel;
    private JTextArea logArea;
    private JComboBox<String> cbMode;

    private Color[] nodeColors;
    private String[] nodeTexts;
    private Color[][] edgeColors;
    private float[][] edgeThickness;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DijkstraMain().setVisible(true));
    }

    public DijkstraMain() {
        setTitle("Visualisasi Dijkstra (Harga Termurah & Jarak Terdekat)");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupData();

        JPanel topPanel = new JPanel();

        JComboBox<String> cbAsal = new JComboBox<>(logic.label);
        JComboBox<String> cbTujuan = new JComboBox<>(logic.label);
        cbTujuan.setSelectedIndex(4);

        String[] modes = {"Cari Harga Termurah (Rp)", "Cari Jarak Terdekat (km)"};
        cbMode = new JComboBox<>(modes);
        cbMode.addActionListener(e -> {
            resetVisuals();
            graphPanel.repaint();
        });

        JButton btnRun = new JButton("MULAI");
        btnRun.setBackground(new Color(33, 150, 243));
        btnRun.setForeground(Color.WHITE);
        btnRun.setFocusPainted(false);

        btnRun.addActionListener(e -> {
            int asal = cbAsal.getSelectedIndex();
            int tujuan = cbTujuan.getSelectedIndex();
            int modePilihan = cbMode.getSelectedIndex();

            resetVisuals();
            logArea.setText("");

            new Thread(() -> logic.dijkstraAlg(asal, tujuan, modePilihan)).start();
        });

        topPanel.add(new JLabel("Asal:")); topPanel.add(cbAsal);
        topPanel.add(new JLabel("Tujuan:")); topPanel.add(cbTujuan);
        topPanel.add(new JLabel("| Mode:")); topPanel.add(cbMode);
        topPanel.add(btnRun);

        graphPanel = new GraphPanel();
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(Color.GREEN);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, new JScrollPane(logArea));
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.8);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void setupData() {
        int n = 5;
        logic = new DijkstraLogic(n, this);
        nodeColors = new Color[n];
        nodeTexts = new String[n];
        edgeColors = new Color[n][n];
        edgeThickness = new float[n][n];

        logic.setLabel(0, "Jakarta"); logic.setLabel(1, "Bandung");
        logic.setLabel(2, "Semarang"); logic.setLabel(3, "Yogyakarta");
        logic.setLabel(4, "Surabaya");

        //addEdge(Asal, Tujuan, HARGA, JARAK_KM)
        logic.addEdge(0, 1, 150000, 150);
        logic.addEdge(0, 2, 100000, 450);
        logic.addEdge(1, 2, 200000, 300);
        logic.addEdge(1, 3, 250000, 400);
        logic.addEdge(2, 4, 300000, 350);
        logic.addEdge(3, 4, 150000, 320);
        logic.addEdge(2, 3, 120000, 130);

        resetVisuals();
    }

    private void resetVisuals() {
        for(int i=0; i<logic.size; i++) {
            nodeColors[i] = Color.LIGHT_GRAY;
            nodeTexts[i] = "Inf";
            for(int j=0; j<logic.size; j++) {
                edgeColors[i][j] = Color.BLACK;
                edgeThickness[i][j] = 1.0f;
            }
        }
        if(graphPanel != null) graphPanel.repaint();
    }

    private Color parseColor(String name) {
        if(name.equals("YELLOW")) return Color.YELLOW;
        if(name.equals("ORANGE")) return Color.ORANGE;
        if(name.equals("GREEN")) return new Color(144, 238, 144);
        if(name.equals("RED")) return Color.RED;
        if(name.equals("BLUE")) return Color.BLUE;
        if(name.equals("GRAY")) return Color.GRAY;
        return Color.BLACK;
    }

    @Override
    public void updateNode(int id, String colorName, String text) {
        nodeColors[id] = parseColor(colorName);
        nodeTexts[id] = text;
        graphPanel.repaint();
    }

    @Override
    public void updateEdge(int u, int v, String colorName) {
        Color c = parseColor(colorName);
        edgeColors[u][v] = c;
        edgeColors[v][u] = c;
        edgeThickness[u][v] = colorName.equals("BLACK") ? 1.0f : 4.0f;
        graphPanel.repaint();
    }

    @Override
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    class GraphPanel extends JPanel {
        private int draggedNodeIndex = -1;
        private int radius = 25;

        public GraphPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    for (int i = 0; i < logic.size; i++) {
                        double dist = Math.pow(e.getX() - coords[i][0], 2) + Math.pow(e.getY() - coords[i][1], 2);
                        if (dist <= radius * radius) { draggedNodeIndex = i; return; }
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) { draggedNodeIndex = -1; }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggedNodeIndex != -1) {
                        coords[draggedNodeIndex][0] = e.getX();
                        coords[draggedNodeIndex][1] = e.getY();
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int mode = cbMode.getSelectedIndex();

            for (int i = 0; i < logic.size; i++) {
                for (int j = i + 1; j < logic.size; j++) {
                    if (logic.gPrice[i][j] > 0) {
                        g2.setColor(edgeColors[i][j]);
                        g2.setStroke(new BasicStroke(edgeThickness[i][j]));
                        g2.draw(new Line2D.Double(coords[i][0], coords[i][1], coords[j][0], coords[j][1]));

                        int val = (mode == 0) ? logic.gPrice[i][j] : logic.gDist[i][j];
                        String label = (mode == 0) ? "Rp " + val : val + " km";

                        int midX = (coords[i][0] + coords[j][0]) / 2;
                        int midY = (coords[i][1] + coords[j][1]) / 2;

                        g2.setColor(new Color(255,255,255, 220));
                        g2.fillRect(midX-15, midY-15, 60, 15);

                        g2.setColor(Color.BLACK);
                        g2.setStroke(new BasicStroke(1));
                        g2.setFont(new Font("Arial", Font.BOLD, 10));
                        g2.drawString(label, midX-10, midY-5);
                    }
                }
            }

            for (int i = 0; i < logic.size; i++) {
                int x = coords[i][0] - radius;
                int y = coords[i][1] - radius;

                g2.setColor(nodeColors[i]);
                g2.fill(new Ellipse2D.Double(x, y, radius*2, radius*2));

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new Ellipse2D.Double(x, y, radius*2, radius*2));

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String cityName = logic.label[i];
                int nameW = g2.getFontMetrics().stringWidth(cityName);
                g2.drawString(cityName, coords[i][0] - (nameW/2), coords[i][1] - radius - 5);

                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                String distText = nodeTexts[i];
                int textW = g2.getFontMetrics().stringWidth(distText);
                g2.drawString(distText, coords[i][0] - (textW/2), coords[i][1] + 5);
            }
        }
    }
}