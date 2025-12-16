package SnakeandLadderGame;

public class DijkstraLogic {

    public interface Visualizer {
        void updateNode(int id, String color, String text);
        void updateEdge(int u, int v, String color);
        void log(String message);
    }

    int size;
    String[] label;

    int[][] gPrice;
    int[][] gDist;

    Visualizer ui;

    public DijkstraLogic(int size, Visualizer ui) {
        this.size = size;
        this.ui = ui;
        this.label = new String[size];
        this.gPrice = new int[size][size];
        this.gDist = new int[size][size];
    }

    public void setLabel(int index, String name) {
        this.label[index] = name;
    }

    public void addEdge(int source, int dest, int price, int distance) {
        this.gPrice[source][dest] = price;
        this.gPrice[dest][source] = price;

        this.gDist[source][dest] = distance;
        this.gDist[dest][source] = distance;
    }

    void dijkstraAlg(int ori, int dest, int mode){
        // Tentukan Graph mana yang dipakai
        int[][] currentGraph = (mode == 0) ? gPrice : gDist;
        String unit = (mode == 0) ? "Rp " : "";
        String suffix = (mode == 0) ? "" : " km";
        String modeName = (mode == 0) ? "HARGA TERMURAH" : "JARAK TERDEKAT";

        ui.log("=== PENCARIAN " + modeName + " ===");
        ui.log("Dari: " + label[ori]);
        ui.log("Ke  : " + label[dest]);
        ui.log("-----------------------");

        int [] dist = new int[this.size];
        for(int i=0;i<this.size;i++) dist[i] = Integer.MAX_VALUE;
        dist[ori] = 0;

        ui.updateNode(ori, "YELLOW", "Start");

        boolean isVisited [] = new boolean[this.size];
        int [] prev = new int[this.size];

        for(int i=0;i<this.size;i++){
            sleep(800);

            int nextNode = findTheNextNode(isVisited, dist);
            if(nextNode == -1) break;

            isVisited[nextNode]=true;

            ui.log("Mengunjungi: " + label[nextNode]);
            ui.log("Total saat ini: " + unit + dist[nextNode] + suffix);
            ui.updateNode(nextNode, "ORANGE", dist[nextNode] + suffix);

            for (int j = 0; j < this.size; j++) {
                if (!isVisited[j] && currentGraph[nextNode][j] > 0) {

                    ui.updateEdge(nextNode, j, "BLUE");
                    sleep(200);

                    if (dist[nextNode] + currentGraph[nextNode][j] < dist[j]) {
                        dist[j] = dist[nextNode] + currentGraph[nextNode][j];
                        prev[j] = nextNode;

                        ui.log("  -> UPDATE: " + label[j]);
                        ui.log("     Nilai baru: " + unit + dist[j] + suffix);
                        ui.updateNode(j, "GRAY", dist[j] + suffix);
                    }
                    ui.updateEdge(nextNode, j, "BLACK");
                }
            }
            ui.updateNode(nextNode, "GREEN", dist[nextNode] + suffix);
            ui.log("");
        }

        ui.log("=======================");
        if(dist[dest] == Integer.MAX_VALUE){
            ui.log("Gagal: Tidak ada rute.");
        } else {
            ui.log("SELESAI!");
            ui.log("Total: " + unit + dist[dest] + suffix);
            traceRouteVisual(dest, ori, prev);
        }
    }

    int findTheNextNode(boolean [] isVisited, int [] dist){
        int min = Integer.MAX_VALUE;
        int minVertex = -1;
        for (int i = 0; i < this.size; i++) {
            if (!isVisited[i] && dist[i] < min) {
                min = dist[i];
                minVertex = i;
            }
        }
        return minVertex;
    }

    void traceRouteVisual(int current, int start, int[] prev) {
        ui.updateNode(current, "RED", "DEST");
        if (current != start) {
            ui.updateEdge(prev[current], current, "RED");
            traceRouteVisual(prev[current], start, prev);
        } else {
            ui.updateNode(current, "RED", "START");
        }
    }

    void sleep(long time) { try { Thread.sleep(time); } catch (Exception e){} }
}