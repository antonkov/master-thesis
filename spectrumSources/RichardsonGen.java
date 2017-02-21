import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class RichardsonGen {

    int J, K;
    int M;
    int r, c;
    Random rng = new Random(19);

    void genMatrix(int[][] a) {
        for (int[] aa : a)
            Arrays.fill(aa, 0);

        ArrayList<Integer> indicesList = new ArrayList<>();
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < K; j++)
                indicesList.add(i);
        }
        Collections.shuffle(indicesList, rng);
        for (int j = 0; j < c; j++) {
            for (int i = 0; i < J; i++) {
                int index = indicesList.get(j * J + i);
                a[index][j] = 1;
            }
        }
    }

    void run(String[] args) {
        if (args.length != 3) {
            System.err.println("usage: RichardsonGen [size] [count] [directoryName]");
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
        new RichardsonGen().run(args);
    }
}
