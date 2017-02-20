import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.*;

public class BigIntegerSpectrumStress {

    private class Edge {
        int from, to;
        int c;
        Edge backEdge;
        int edgeNum;
        BigInteger[][] dp;
        Edge[][] prev;

        public Edge(int from, int to, int c, int edgeNum, int M, int spectrumSize) {
            this.from = from;
            this.to = to;
            this.c = c;
            this.edgeNum = edgeNum;
            this.dp = new BigInteger[spectrumSize + 1][M];
            for (BigInteger[] bi: dp) {
                Arrays.fill(bi, BigInteger.ZERO);
            }
            this.prev = new Edge[spectrumSize + 1][M];
        }
    }

    private ArrayList<Edge>[] g;
    private ArrayList<Edge> edges;

    private void clearDp() {
        for (Edge e : edges) {
            for (BigInteger[] a : e.dp) {
                Arrays.fill(a, BigInteger.ZERO);
            }
        }
    }

    private void addBiEdge(int from, int to, int c, int M, int spectrumSize) {
        Edge e1 = new Edge(from, to, c, edges.size(), M, spectrumSize);
        edges.add(e1);
        int c2 = M - c;
        if (c == 0)
            c2 = 0;
        Edge e2 = new Edge(to, from,  c2, edges.size(), M, spectrumSize);
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

    final int spectrumSize = 40;

    void print(Edge e, int len, int w, int M) {
        if (len > 1) {
            print(e.prev[len][w], len - 1, (w - e.c + M) % M, M);
        }
        System.out.print(e.edgeNum + "(" + e.from + ", " + e.to + ") ");
    }

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

        int[] primes = new int[spectrumSize + 1];
        int primesPtr = 0;
        for (int i = 2; i <= spectrumSize; i++) {
            boolean isPrime = true;
            for (int j = 2; j * j <= i; j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime)
                primes[primesPtr++] = i;
        }
        // Solve
        BigInteger[] spectrum = new BigInteger[spectrumSize + 1];
        Arrays.fill(spectrum, BigInteger.ZERO);
        for (int root = 0; root < n; root++) {
            for (Edge eStart : g[root]) {
                clearDp();

                eStart.dp[1][eStart.c] = BigInteger.ONE;

                BigInteger[][] numberCycles = new BigInteger[M][spectrumSize + 1];
                for (BigInteger[] bi : numberCycles)
                    Arrays.fill(bi, BigInteger.ZERO);

                for (int len = 2; len <= spectrumSize; len++) {
                    for (Edge eIncoming : edges) {
                        for (int w = 0; w < M; w++) {
                            BigInteger cntPaths = eIncoming.dp[len - 1][w];
                            if (!cntPaths.equals(BigInteger.ZERO)) {
                                for (Edge e : g[eIncoming.to]) {
                                    if (e.edgeNum != eIncoming.backEdge.edgeNum) {
                                        int wNext = w + e.c;
                                        if (wNext >= M) {
                                            wNext -= M;
                                        }
                                        e.dp[len][wNext] = e.dp[len][wNext].add(cntPaths);
                                        e.prev[len][wNext] = eIncoming;
                                    }
                                }
                            }
                        }
                    }

                    for (Edge e : g[root]) {
                        if (e.edgeNum != eStart.edgeNum) {
                            Edge incoming = e.backEdge;
                            for (int w = 0; w < M; w++)
                                numberCycles[w][len] = numberCycles[w][len].add(incoming.dp[len][w]);
                        }
                    }
                }
                int[] add = new int[spectrumSize + 1];
                add[1] = 1;
                for (int i = 2; i <= spectrumSize; i++) {
                    add[i] = i;
                    for (int j = 2; j <= i; j++) {
                        if (i % j == 0) {
                            add[i] -= add[i / j];
                        }
                    }
                }
                /* Definition of add: check[i] = i
                int[] check = new int[spectrumSize + 1];
                for (int i = 1; i < add.length; i++) {
                    for (int j = 1; j * i <= spectrumSize; j++) {
                        check[j * i] += add[i];
                    }
                }
                */
                BigInteger[] withRepetitions = new BigInteger[spectrumSize + 1];
                Arrays.fill(withRepetitions, BigInteger.ZERO);
                // Magic!!!
                for (int len = 1; len <= spectrumSize; len++) {
                    for (int sCnt = 1; sCnt <= len; sCnt++) {
                        if (len % sCnt == 0) {
                            for (int sw = 0; sw < M; sw++) {
                                if (sw * sCnt % M == 0) {
                                    withRepetitions[len] = withRepetitions[len].add(BigInteger.valueOf(add[sCnt]).multiply(numberCycles[sw][len / sCnt]));
                                }
                            }
                        }
                    }
                }

                for (int i = 0; i < withRepetitions.length; i++)
                    spectrum[i] = spectrum[i].add(withRepetitions[i]);
            }
        }
        for (int i = 2; i < spectrum.length; i++) {
            if (!spectrum[i].mod(BigInteger.valueOf(2 * i)).equals(BigInteger.ZERO)) {
                System.out.println(i);
                throw new AssertionError();
            }
            spectrum[i] = spectrum[i].divide(BigInteger.valueOf(2 * i));
        }

        // Report
        SolveReport report = new SolveReport();
        report.spectrum = spectrum;
        return report;
    }

    class SolveReport {
        BigInteger[] spectrum;

        public SolveReport() {}
    }

    private void run(String[] filesAndFolders) {
        PrintWriter out = new PrintWriter(System.out);

        final int MAX_ENTRIES_TO_SHOW = 3;
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
                    if (!report.spectrum[i].equals(BigInteger.ZERO) && shown < MAX_ENTRIES_TO_SHOW) {
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
        new BigIntegerSpectrumStress().run(args);
    }

}
