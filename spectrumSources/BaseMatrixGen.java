import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by antonkov on 1/31/17.
 */
public class BaseMatrixGen {

    int wr = 6;
    int wc = 3;
    int r = -1; // to initialize
    int c = -1; // to initialize
    int[][] a;
    int[] sr;
    int[] sc;
    int ans = 0;
    boolean print = true;
    String directoryToSave;

    void gen(int ri, int ci) {
        if (ci == c) {
            for (int i = 1; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    if (a[i - 1][j] < a[i][j]) {
                        // bigger than previous - good
                        break;
                    }
                    if (a[i - 1][j] > a[i][j]) {
                        // smaller that previous - bad
                        return;
                    }
                }
            }
            if (print) {
                String filename = String.format("b%d_%d_%04d.mtx", r, c, ans);
                try (PrintWriter out = new PrintWriter(directoryToSave + "/" + filename)) {
                    out.println("proto_matrix"); // matrix format
                    out.println(r + " " + c); // J K
                    out.println(1); // M
                    for (int i = 0; i < r; i++) {
                        for (int j = 0; j < c; j++) {
                            int val = -1;
                            if (a[i][j] == 1) {
                                val = 0;
                            }
                            out.print(val + " ");
                        }
                        out.println();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            ans++;
            return;
        }
        if (ri == r) {
            if (ci != 0) {
                for (int i = 0; i < r; i++) {
                    if (a[i][ci - 1] < a[i][ci]) {
                        // bigger than previous - good
                        break;
                    }
                    if (a[i][ci - 1] > a[i][ci]) {
                        // smaller than previous - bad
                        return;
                    }
                }
            }
            gen(0, ci + 1);
            return;
        }
        if (sr[ri] + 1 <= wr && sr[ri] + c - ci >= wr &&
                sc[ci] + 1 <= wc && sc[ci] + r - ri >= wc) {
            sr[ri]++;
            sc[ci]++;
            a[ri][ci] = 1;
            gen(ri + 1, ci);
            sr[ri]--;
            sc[ci]--;
        }
        if (sr[ri] <= wr && sr[ri] + c - ci - 1 >= wr &&
                sc[ci] <= wc && sc[ci] + r - ri - 1 >= wc) {
            a[ri][ci] = 0;
            gen(ri + 1, ci);
        }
    }

    void run(String[] args) {
        if (args.length != 2) {
            System.err.println("usage: BaseMatrixGen [size] [directoryName]");
            return;
        }
        int size = Integer.parseInt(args[0]);
        r = size;
        c = 2 * size;
        directoryToSave = args[1];
        sr = new int[r];
        sc = new int[c];
        a = new int[r][c];
        gen(0, 0);
        System.out.println(ans);
    }

    public static void main(String[] args) {
        new BaseMatrixGen().run(args);
    }
}
