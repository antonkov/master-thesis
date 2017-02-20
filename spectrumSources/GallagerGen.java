import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by antonkov on 1/31/17.
 */
public class GallagerGen {

    int J, K;
    int M;
    int r, c;

    int[] randomPermutation(int size) {
        ArrayList<Integer> range = new ArrayList<>();
        for (int i = 0; i < size; i++)
            range.add(i);
        Collections.shuffle(range);
        int[] result = new int[size];
        int p = 0;
        for (int x : range)
            result[p++] = x;
        return result;
    }

    void genMatrix(int[][] a) {
        for (int[] aa : a)
            Arrays.fill(aa, 0);
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                a[i][i * K + j] = 1;
            }
        }
        for (int stripStart = M; stripStart < r; stripStart += M) {
            int[] perm = randomPermutation(M);
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < c; j++) {
                    a[stripStart + i][j] = a[perm[i]][j];
                }
            }
        }
    }

    void run(String[] args) {
        if (args.length != 3) {
            System.err.println("usage: GallagerGen [size] [count] [directoryName]");
            return;
        }
        int size = Integer.parseInt(args[0]);
        int count = Integer.parseInt(args[1]);
        J = size;
        K = 2 * size;
        M = 4;
        r = M * J;
        c = M * K;
        String directoryToSave = args[2];

        int[][] a = new int[r][c];
        int len = 1;
        while (Math.pow(10, len) < count) len++;
        for (int t = 0; t < count; t++) {
            genMatrix(a);
            String filename = String.format(directoryToSave + "/%0" + len + "d.mtx", t);
            try (PrintWriter out = new PrintWriter(filename)) {
                out.println("proto_matrix");
                out.println(r + " " + c);
                out.println(1); // M: Base matrix, doesn't have marking yet
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
    }

    public static void main(String[] args) {
        new GallagerGen().run(args);
    }
}
