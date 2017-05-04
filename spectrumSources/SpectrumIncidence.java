import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class SpectrumIncidence {

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
    private int M;

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
        int c2 = M - c;
        if (c == 0)
            c2 = 0;
        Edge e2 = new Edge(to, from, c2, edges.size(), M, spectrumSize);
        edges.add(e2);

        e1.backEdge = e2;
        e2.backEdge = e1;
        g[from].add(e1);
        g[to].add(e2);
    }

    // params
    private int SPECTRUM_SIZE;

    SolveReport solve() {
        long[][] cntCyclesThroughEdge = new long[M][SPECTRUM_SIZE + 1];
        for (int root = 0; root < g.length; root++) {
            for (Edge eStart : g[root]) {
                clearDp();

                eStart.dp[0][0] = 1;

                for (int len = 1; len <= SPECTRUM_SIZE; len++) {
                    for (Edge eIncoming : edges) {
                        for (int w = 0; w < M; w++) {
                            long cntPaths = eIncoming.dp[len - 1][w];
                            if (cntPaths != 0) {
                                for (Edge e : g[eIncoming.to]) {
                                    if (e.edgeNum != eIncoming.backEdge.edgeNum) {
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

                    for (int w = 0; w < M; w++)
                        cntCyclesThroughEdge[w][len] += eStart.dp[len][w];
                }
            }
        }
        // Mobius inversion
        int[] mu = new int[SPECTRUM_SIZE + 1];
        mu[1] = 1;
        nextX:
        for (int i = 2; i < mu.length; i++) {
            int x = i;
            int r = 0;
            for (int y = 2; y <= x; y++) {
                if (x % (y * y) == 0) {
                    continue nextX;
                } else if (x % y == 0) {
                    x /= y;
                    r++;
                }
            }
            mu[i] = (r % 2 == 0) ? 1 : -1;
        }
        long[] spectrum = new long[SPECTRUM_SIZE + 1];
        long[][] g = new long[SPECTRUM_SIZE + 1][M]; // f[i][j] - count cycles with period and length i and weight j
        for (int w = 0; w < M; w++) {
            for (int period = 1; period <= SPECTRUM_SIZE; period++) {
                for (int d = 1; d <= period; d++) {
                    if (period % d == 0) {
                        for (int subW = 0; subW < M; subW++) {
                            if (subW * d % M == w)
                                g[period][w] += mu[d] * cntCyclesThroughEdge[subW][period / d];
                        }
                    }
                }
                if (g[period][w] % period != 0) {
                    throw new AssertionError();
                }
                g[period][w] /= period;
            }
        }
        // end Mobius inversion

        for (int period = 1; period <= SPECTRUM_SIZE; period++) {
            for (int num = 1; num * period <= SPECTRUM_SIZE; num++) {
                for (int w = 0; w < M; w++) {
                    if (w * num % M == 0) {
                        spectrum[num * period] += g[period][w];
                    }
                }
            }
        }
        for (int i = 2; i < spectrum.length; i++) {
            if (spectrum[i] % 2 != 0) {
                System.out.println(i);
                throw new AssertionError("Division by 2 error");
            }
            spectrum[i] /= 2;
        }

        // Report
        SolveReport report = new SolveReport();
        report.spectrum = spectrum;
        return report;
    }

    class SolveReport {
        long[] spectrum;

        public SolveReport() {
        }
    }

    private void run(String[] filesAndFolders) {
        PrintWriter out = new PrintWriter(System.out);

        final int MAX_ENTRIES_TO_SHOW = 8;
        out.println("Results");
        out.print("Filename ");
        for (int i = 1; i <= MAX_ENTRIES_TO_SHOW; i++)
            out.print("spectrum" + i + " ");
        out.println();

        Queue<String> q = new ArrayDeque<>();
        q.addAll(Arrays.asList(filesAndFolders));
        long resultTime = 0;
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

                long startTime = System.currentTimeMillis();

                // Input and init
                if (matrixFormat.equals("proto_matrix")) {
                    int J = in.nextInt();
                    int K = in.nextInt();
                    M = in.nextInt(); // Module of expansion
                    int[][] ws = new int[J][K];
                    for (int i = 0; i < J; i++) {
                        for (int j = 0; j < K; j++) {
                            ws[i][j] = in.nextInt();
                        }
                    }
                    int n = J + K; // Count vertices
                    g = new ArrayList[n];
                    edges = new ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        g[i] = new ArrayList<>();
                    }
                    for (int i = 0; i < J; i++) {
                        for (int j = 0; j < K; j++) {
                            int from = i;
                            int to = J + j;
                            int c = ws[i][j];
                            if (c != -1) {
                                addBiEdge(from, to, c, M, SPECTRUM_SIZE);
                            }
                        }
                    }
                } else if (matrixFormat.equals("incidence_matrix")) {
                    int V = in.nextInt();
                    int E = in.nextInt();
                    M = in.nextInt();
                    g = new ArrayList[V];
                    edges = new ArrayList<>();
                    for (int i = 0; i < V; i++) {
                        g[i] = new ArrayList<>();
                    }
                    int[] from = new int[E];
                    int[] to = new int[E];
                    int[] w = new int[E];
                    Arrays.fill(from, -1);
                    Arrays.fill(to, -1);
                    for (int i = 0; i < V; i++) {
                        for (int j = 0; j < E; j++) {
                            String s = in.next();
                            if (s.equals("O")) {
                                // None
                            } else {
                                int x = Integer.parseInt(s);
                                if (from[j] == -1) {
                                    from[j] = i;
                                    w[j] -= x;
                                } else {
                                    to[j] = i;
                                    w[j] += x;
                                }
                            }
                        }
                    }
                    for (int i = 0; i < E; i++) {
                        addBiEdge(from[i], to[i], (w[i] % M + M) % M, M, SPECTRUM_SIZE);
                    }
                } else {
                    System.err.println("Wrong matrix format: " + filename + ". Ignoring...");
                    continue;
                }

                SolveReport report = solve();

                long time = System.currentTimeMillis() - startTime;
                resultTime += time;
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
        System.out.println("took time " + resultTime);
        out.close();
    }

    public SpectrumIncidence(int spectrumSize) {
        this.SPECTRUM_SIZE = spectrumSize;
    }

    public static void main(String[] args) {
        final int DEFAULT_SPECTRUM_SIZE = 20;
        new SpectrumIncidence(DEFAULT_SPECTRUM_SIZE).run(args);
    }

}
