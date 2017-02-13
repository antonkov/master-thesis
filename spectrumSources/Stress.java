import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class SpectrumFix {

    private class Edge {
        int from, to;
        int c;
        Edge backEdge;
        int edgeNum;
        long[][] dp;

        public Edge(int from, int to, int c, int edgeNum, int M, int spectrumSize) {
            this.from = from;
            this.to = to;
            this.c = c;
            this.edgeNum = edgeNum;
            this.dp = new long[spectrumSize + 1][M];
        }
    }

    private ArrayList<Edge>[] g;
    private ArrayList<Edge> edges;

    private void clearDp() {
        for (Edge e : edges) {
            for (long[] a : e.dp) {
                Arrays.fill(a, 0);
            }
        }
    }

    private void addBiEdge(int from, int to, int c, int M, int spectrumSize) {
        Edge e1 = new Edge(from, to, c, edges.size(), M, spectrumSize);
        edges.add(e1);
        Edge e2 = new Edge(to, from, M-c, edges.size(), M, spectrumSize);
        edges.add(e2);

        e1.backEdge = e2;
        e2.backEdge = e1;
        g[from].add(e1);
        g[to].add(e2);
    }

    private void removeBiEdge(Edge e) {
        Edge backEdge = e.backEdge;
        edges.remove(e);
        edges.remove(backEdge);
        g[e.from].remove(e);
        g[backEdge.from].remove(backEdge);
    }

    final int spectrumSize = 20;

    SolveReport solve(int J, int K, int M, int[][] ws) {
        int n = J + K; // Count vertices
        int m = J * K; // Count edges
        g = new ArrayList[n + 1];
        edges = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            g[i] = new ArrayList<>();
        }
        for (int i = 0; i < J; i++) {
            for (int j = 0; j < K; j++) {
                int from = i;
                int to = J + j;
                int c = ws[i][j];
                if (c != -1) {
                    addBiEdge(from, to, c, M, spectrumSize);
                }
            }
        }

        // Solve
        long[] spectrum = new long[spectrumSize + 1];
        for (int root = 0; root < n; root++) {
            for (Edge eStart : g[root]) {
                if (eStart.to < root)
                    continue;

                clearDp();

                eStart.dp[1][eStart.c] = 1;

                for (int len = 2; len <= spectrumSize; len++) {
                    for (Edge eIncoming : edges) {
                        for (int w = 0; w < M; w++) {
                            long cntPaths = eIncoming.dp[len - 1][w];
                            if (cntPaths != 0) {
                                for (Edge e : g[eIncoming.to]) {
                                    if (e.edgeNum != eIncoming.backEdge.edgeNum && e.to >= root) {
                                        int wNext = w + e.c;
                                        if (wNext >= M) {
                                            wNext -= M;
                                        }
                                        e.dp[len][wNext] += cntPaths;
                                    }
                                }
                            }
                        }
                    }

                    for (Edge e : g[root]) {
                        if (e.edgeNum != eStart.edgeNum) {
                            Edge incoming = e.backEdge;
                            spectrum[len] += incoming.dp[len][0];
                        }
                    }
                }
            }
        }
        for (int i = 0; i < spectrum.length; i++)
            spectrum[i] /= 2; // can go on cycle in two directions

        // Report
        SolveReport report = new SolveReport();
        report.spectrum = spectrum;
        return report;
    }

    class SolveReport {
        long[] spectrum;

        public SolveReport() {}
    }

    private void run(String[] filesAndFolders) {
        PrintWriter out = new PrintWriter(System.out);

        final int MAX_ENTRIES_TO_SHOW = 20;
        out.println("Results");
        out.print("Filename ");
        for (int i = 1; i <= MAX_ENTRIES_TO_SHOW; i++)
            out.print("spectrum" + i + " ");
        out.println();

        Queue<String> q = new ArrayDeque<>();
        q.addAll(Arrays.asList(filesAndFolders));
        while (!q.isEmpty()) {
            String filename = q.poll();
            {
                // check if filename is directory
                File file = new File(filename);
                if (file.isDirectory()) {
                    for (File child : file.listFiles()) {
                        q.add(child.getAbsolutePath());
                    }
                    continue;
                }
            }
            try {
                Scanner in = new Scanner(new File(filename));
                String matrixFormat = in.next();
                if (!matrixFormat.equals("proto_matrix")) {
                    System.err.println("Wrong matrix format: " + filename + ". Ignoring...");
                }

                long startTime = System.currentTimeMillis();

                // Input and init
                int J = in.nextInt();
                int K = in.nextInt();
                int M = in.nextInt(); // Module of expansion
                int[][] ws = new int[J][K];
                for (int i = 0; i < J; i++) {
                    for (int j = 0; j < K; j++) {
                        ws[i][j] = in.nextInt();
                    }
                }

                SolveReport report = solve(J, K, M, ws);

                long time = System.currentTimeMillis() - startTime;
                out.print(filename + " ");
                int shown = 0;
                for (int i = 1; i < report.spectrum.length; i++) {
                    if (report.spectrum[i] != 0 && shown < MAX_ENTRIES_TO_SHOW) {
                        out.print(i + ":" + report.spectrum[i] + " ");
                        shown++;
                    }
                }
                out.println();
            } catch (FileNotFoundException e) {
                System.err.println("File " + filename + " not found. Ignoring...");
            }
        }

        out.close();
    }

    public static void main(String[] args) {
        new SpectrumFix().run(args);
    }

}
