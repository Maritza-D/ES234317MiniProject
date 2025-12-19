package SnakeandLadderGame;

import java.util.*;

public class GameLogic {
    public static final int TOTAL_NODES = 25;

    public List<List<Integer>> adj = new ArrayList<>();
    public Map<Integer, Integer> randomConnections = new HashMap<>();
    public Map<Integer, Integer> nodeScores = new HashMap<>();

    private Random rand = new Random();

    public GameLogic() {
        setupGraph();
    }

    public void setupGraph() {
        adj.clear();
        randomConnections.clear();

        for (int i = 0; i < TOTAL_NODES; i++) {
            adj.add(new ArrayList<>());
            if (i < TOTAL_NODES - 1) adj.get(i).add(i + 1);
        }
    }

    public void generateRandomConnections() {
        setupGraph();
        List<Integer> primes = new ArrayList<>();

        for (int i = 2; i < TOTAL_NODES - 5; i++) {
            if (isPrime(i)) primes.add(i - 1);
        }

        int count = 0;
        while (count < 5 && !primes.isEmpty()) {
            int start = primes.get(rand.nextInt(primes.size()));
            int jump = rand.nextInt(8) + 3;
            int end = start + jump;

            if (end < TOTAL_NODES && !randomConnections.containsKey(start)) {
                adj.get(start).add(end);
                randomConnections.put(start, end);
                count++;
            }
        }
    }

    public void generateRandomScores() {
        nodeScores.clear();
        for (int i = 0; i < 8; i++) {
            int idx = rand.nextInt(TOTAL_NODES - 2) + 1;
            int scoreVal = (rand.nextInt(5) + 1) * 10;
            nodeScores.put(idx, scoreVal);
        }
    }

    public List<Integer> getShortestPath(int start, int end) {
        if (start == end) return new ArrayList<>();

        int[] dist = new int[TOTAL_NODES];
        int[] parent = new int[TOTAL_NODES];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[start] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int u = pq.poll()[0];
            if (u == end) break;

            for (int v : adj.get(u)) {
                if (dist[u] + 1 < dist[v]) {
                    dist[v] = dist[u] + 1;
                    parent[v] = u;
                    pq.add(new int[]{v, dist[v]});
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        int curr = end;
        while (curr != -1 && curr != start) {
            path.add(curr);
            curr = parent[curr];
        }
        Collections.reverse(path);
        return path;
    }

    public boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}