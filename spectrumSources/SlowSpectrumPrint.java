import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class SlowSpectrumPrint {

    private class Edge {
        int from, to;
        int c;
        Edge backEdge;
        int edgeNum;

        public Edge(int from, int to, int c, int edgeNum) {
            this.from = from;
            this.to = to;
            this.c = c;
            this.edgeNum = edgeNum;
        }
    }

    private class Path {
        private Edge[] es;
        private int hash;

        public Path(Edge[] es) {
            this.es = es;
            hash = Arrays.hashCode(es);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Path path = (Path) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(es, path.es);
        }
    }

    private ArrayList<Edge>[] g;
    private ArrayList<Edge> edges;
    private int M;

    private void addBiEdge(int from, int to, int c, int M) {
        Edge e1 = new Edge(from, to, c, edges.size());
        edges.add(e1);
        int c2 = (c == 0 ? 0 : M - c);
        Edge e2 = new Edge(to, from, c2, edges.size());
        edges.add(e2);

        e1.backEdge = e2;
        e2.backEdge = e1;
        g[from].add(e1);
        g[to].add(e2);
    }

    private int MAX_PATH_LEN;
    private int SPECTRUM_SIZE;
    long[] spectrum;

    Edge[] smallestShift(Edge[] path) {
        int bestStart = 0;
        for (int start = 1; start < path.length; start++) {
            for (int i = 0; i < path.length; i++) {
                int i1 = (start + i) % path.length;
                int i2 = (bestStart + i) % path.length;
                if (path[i1].edgeNum != path[i2].edgeNum) {
                    if (path[i1].edgeNum < path[i2].edgeNum) {
                        bestStart = start;
                    }
                    break;
                }
            }
        }
        Edge[] shiftedPath = new Edge[path.length];
        for (int i = 0; i < path.length; i++) {
            shiftedPath[i] = path[(bestStart + i) % path.length];
        }
        return shiftedPath;
    }

    Edge[] connonize(Edge[] path) {
        Edge[] reverse = new Edge[path.length];
        for (int i = 0; i < path.length; i++) {
            reverse[reverse.length - 1 - i] = path[i].backEdge;
        }
        path = smallestShift(path);
        reverse = smallestShift(reverse);
        for (int i = 0; i < path.length; i++) {
            if (path[i].edgeNum != reverse[i].edgeNum) {
                if (path[i].edgeNum < reverse[i].edgeNum) {
                    return path;
                } else {
                    return reverse;
                }
            }
        }
        return null;
    }

    SolveReport solve() {
        int n = g.length;
        spectrum = new long[SPECTRUM_SIZE + 1];
        HashSet<Path>[] hs = new HashSet[SPECTRUM_SIZE + 1];
        for (int i = 0; i <= SPECTRUM_SIZE; i++) {
            hs[i] = new HashSet<>();
        }
        //System.out.println(M + " " + n);
        for (int root = 0; root < n; root++) {
            ArrayList<Edge[]>[][][] paths;
            paths = new ArrayList[MAX_PATH_LEN + 1][M][n];
            for (int i = 0; i <= MAX_PATH_LEN; i++) {
                for (int j = 0; j < M; j++) {
                    for (int k = 0; k < n; k++) {
                        paths[i][j][k] = new ArrayList<>();
                    }
                }
            }
            for (Edge e : g[root]) {
                paths[1][e.c][e.to].add(new Edge[]{e});
            }
            for (int len = 2; len <= MAX_PATH_LEN; len++) {
                for (int w = 0; w < M; w++) {
                    for (int v = 0; v < n; v++) {
                        for (Edge[] path : paths[len - 1][w][v]) {
                            Edge lastEdge = path[path.length - 1];
                            for (Edge e : g[v]) {
                                if (e != lastEdge.backEdge) {
                                    Edge[] newPath = new Edge[len];
                                    for (int i = 0; i < len - 1; i++)
                                        newPath[i] = path[i];
                                    newPath[len - 1] = e;
                                    paths[len][(w + e.c) % M][e.to].add(newPath);
                                }
                            }
                        }
                    }
                }
                for (int w = 0; w < M; w++) {
                    for (int v = 0; v < n; v++) {
                        for (Edge[] path1 : paths[len][w][v]) {
                            for (Edge[] path2 : paths[len][w][v]) {
                                if (path1[len - 1] != path2[len - 1] &&
                                        path1[0] != path2[0]) {
                                    Edge[] path = new Edge[2 * len];
                                    int p = 0;
                                    for (int i = 0; i < len; i++)
                                        path[p++] = path1[i];
                                    for (int i = len - 1; i >= 0; i--)
                                        path[p++] = path2[i].backEdge;
                                    path = connonize(path);
                                    hs[2 * len].add(new Path(path));
                                }
                            }
                        }
                    }
                }
            }

        }

        for (int i = 0; i <= SPECTRUM_SIZE; i++) {
            if (hs[i].size() != 0) {
                TreeSet<String> strs = new TreeSet<>();
                spectrum[i] = hs[i].size();
                for (Path p : hs[i]) {
                    String ps = "";
                    for (Edge e : p.es) {
                        int x = e.c;
                        String s = Integer.toString(x);
                        if (e.from > e.to)
                            s = "-" + Integer.toString((M - x) % M);
                        ps += s + " ";
                    }
                    strs.add(ps);
                }
                for (String s : strs)
                    System.out.println(s);
            }
        }

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
                                addBiEdge(from, to, c, M);
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
                        addBiEdge(from[i], to[i], (w[i] % M + M) % M, M);
                    }
                } else {
                    System.err.println("Wrong matrix format: " + filename + ". Ignoring...");
                    continue;
                }

                SolveReport report = solve();

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

    public SlowSpectrumPrint(int spectrumSize) {
        if (spectrumSize % 2 != 0)
            throw new AssertionError("spectrum size should be even");
        this.SPECTRUM_SIZE = spectrumSize;
        this.MAX_PATH_LEN = SPECTRUM_SIZE / 2;
    }

    public static void main(String[] args) {
        final int DEFAULT_SPECTRUM_SIZE = 12;
        new SlowSpectrumPrint(DEFAULT_SPECTRUM_SIZE).run(args);
    }

}
