import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by antonkov on 6/2/17.
 */
public class ComparableMarking {

    final boolean USE_FILTER = false;
    Random rng = new Random(19);

    boolean filter(long[] spectrum) {
        return spectrum[4] == 0;
    }

    int[][] markMatrix(int[][] hd, int M) {
        int[][] hMarked = new int[hd.length][hd[0].length];
        for (int i = 0; i < hd.length; i++) {
            for (int j = 0; j < hd[i].length; j++) {
                if (hd[i][j] != -1) {
                    hMarked[i][j] = rng.nextInt(M);
                } else {
                    hMarked[i][j] = -1;
                }
            }
        }
        return hMarked;
    }

    void dfs(int v, boolean[][] g, int[] d, int[] to) {
        if (d[v] != -1)
            return;
        d[v] = 0;
        to[v] = -1;
        for (int u = 0; u < g.length; u++) {
            if (g[v][u]) {
                dfs(u, g, d, to);
                if (d[u] + 1 > d[v]) {
                    d[v] = d[u] + 1;
                    to[v] = u;
                }
            }
        }
    }

    ArrayList<int[][]> calcSamples(int[][][] mtx, long[][] specs) {
        int n = mtx.length;
        ArrayList<int[][]> samples = new ArrayList<>();
        boolean[][] g = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                boolean greater = false;
                boolean less = false;
                for (int ii = 0; ii < specs[i].length; ii++) {
                    if (specs[i][ii] > specs[j][ii]) {
                        greater = true;
                        break;
                    }
                    if (specs[i][ii] < specs[j][ii]) {
                        less = true;
                    }
                }
                if (greater) {
                    continue;
                }
                g[i][j] = less;
            }
        }
        int start = 0;
        int[] to = new int[n];
        int[] d = new int[n];
        Arrays.fill(d, -1);
        for (int i = 0; i < n; i++) {
            if (d[i] == -1) {
                dfs(i, g, d, to);
                if (d[i] > d[start]) {
                    start = i;
                }
            }
        }
        while (start != -1) {
            samples.add(mtx[start]);
            start = to[start];
        }
        return samples;
    }

    void run(String[] args) throws FileNotFoundException {
        int start = 0;
        if (args.length < 3) {
            System.err.println("usage: ComparableMarking [numberSamplesToChooseFrom] [M] [outFolder] [filenames]*");
            return;
        }
        int numberSamples = Integer.parseInt(args[start++]);
        int M = Integer.parseInt(args[start++]);
        String outFolder = args[start++];
        for (int test = start; test < args.length; test++) {
            String filename = args[test];
            File file = new File(filename);
            Scanner in = new Scanner(file);
            String inputType = in.next(); // should be proto_matrix
            if (!inputType.equals("proto_matrix")) {
                System.err.println("Wrong matrix format");
                return;
            }
            int r = in.nextInt(), c = in.nextInt(); // J K
            in.nextInt(); // M = 1
            int[][] hd = new int[r][c];
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    hd[i][j] = in.nextInt();
                }
            }

            int[][][] mtx = new int[numberSamples][][];
            long[][] specs = new long[numberSamples][];
            for (int i = 0; i < numberSamples; i++) {
                mtx[i] = markMatrix(hd, M);
                specs[i] = new TannerSpectrumFinderTable().solve(r,c, M, mtx[i]).spectrum;
                if (USE_FILTER) {
                    if (!filter(specs[i])) {
                        i--;
                    }
                    continue;
                }
                if (i % 1000 == 0) {
                    System.err.println(i + " found");
                }
            }
            System.err.println("" + numberSamples + " matrices found.");
            ArrayList<int[][]> samples = calcSamples(mtx, specs);

            int cntZeros = 0;
            while (Math.pow(10, cntZeros) < samples.size())
                cntZeros++;
            int sample = 0;
            for (int[][] H : samples) {
                String baseName = file.getName().split("\\.")[0];
                String name = String.format("%s/%s_%0" + cntZeros + "d.mtx", outFolder, baseName, sample);

                try (PrintWriter out = new PrintWriter(name)) {
                    out.println("proto_matrix"); // matrix format
                    out.println(r + " " + c);
                    out.println(M);
                    for (int i = 0; i < r; i++) {
                        for (int j = 0; j < c; j++) {
                            out.print(H[i][j] + " ");
                        }
                        out.println();
                    }
                    sample++;
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        new ComparableMarking().run(args);
    }
}
