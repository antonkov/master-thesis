import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class BaseMatrixGen {

    Random rng = new Random(19);
    final int SPECTRUM_SIZE = 20;
    int J, K;
    int M;
    int r, c;

    interface MatrixGenerator {
        BaseMatrix genMatrix();
    }

    class GallagerGenerator implements MatrixGenerator {

        private int[] randomPermutation(int size) {
            ArrayList<Integer> range = new ArrayList<>();
            for (int i = 0; i < size; i++)
                range.add(i);
            Collections.shuffle(range, rng);
            int[] result = new int[size];
            int p = 0;
            for (int x : range)
                result[p++] = x;
            return result;
        }

        @Override
        public BaseMatrix genMatrix() {
            int[][] a = new int[r][c];
            for (int[] aa : a)
                Arrays.fill(aa, -1);
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < K; j++) {
                    a[i][i * K + j] = 0;
                }
            }
            for (int stripStart = M; stripStart < r; stripStart += M) {
                int[] perm = randomPermutation(c);
                for (int j = 0; j < c; j++) {
                    for (int i = 0; i < M; i++) {
                        a[stripStart + i][j] = a[i][perm[j]];
                    }
                }
            }
            return new BaseMatrix(a);
        }
    }

    class RichardsonGenerator implements MatrixGenerator {

        @Override
        public BaseMatrix genMatrix() {
            int[][] a = new int[r][c];
            for (int[] aa : a)
                Arrays.fill(aa, -1);

            ArrayList<Integer> indicesList = new ArrayList<>();
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < K; j++)
                    indicesList.add(i);
            }
            Collections.shuffle(indicesList, rng);
            for (int j = 0; j < c; j++) {
                for (int i = 0; i < J; i++) {
                    int index = indicesList.get(j * J + i);
                    a[index][j] = 0;
                }
            }
            return new BaseMatrix(a);
        }
    }

    class QuasicyclicGenerator implements MatrixGenerator {

        @Override
        public BaseMatrix genMatrix() {
            int[][] a = new int[r][c];
            for (int[] aa : a)
                Arrays.fill(aa, -1);
            for (int i0 = 0; i0 < r; i0 += M) {
                for (int j0 = 0; j0 < c; j0 += M) {
                    int h = rng.nextInt(M);
                    for (int i = 0; i < M; i++) {
                        int j = (i + h) % M;
                        a[i0 + i][j0 + j] = 0;
                    }
                }
            }
            return new BaseMatrix(a);
        }
    }

    class BaseMatrix implements Comparable<BaseMatrix> {
        int[][] a;
        long[] spectrum;

        public BaseMatrix(int[][] a, long[] spectrum) {
            this.a = a;
            this.spectrum = spectrum;
        }

        public BaseMatrix(int[][] a) {
            this.a = a;
            this.spectrum = new Spectrum(SPECTRUM_SIZE).solve(a.length, a[0].length, 1, a).spectrum;
        }

        @Override
        public int compareTo(BaseMatrix o) {
            for (int i = 0; i < spectrum.length; i++) {
                if (spectrum[i] != o.spectrum[i]) {
                    return Long.compare(spectrum[i], o.spectrum[i]);
                }
            }
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[i].length; j++) {
                    if (a[i][j] != o.a[i][j]) {
                        return Integer.compare(a[i][j], o.a[i][j]);
                    }
                }
            }
            return 0;
        }

        void write(String filename) {
            try (PrintWriter out = new PrintWriter(filename)) {
                out.println("proto_matrix");
                out.println(r + " " + c);
                out.println(1); // M: Base matrix, doesn't have marking yet
                for (int i = 0; i < r; i++) {
                    for (int j = 0; j < c; j++) {
                        out.print(a[i][j] + " ");
                    }
                    out.println();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void run(String[] args) {
        if (args.length != 4) {
            System.err.println("usage: BaseMatrixGen [Gallager/Richardson/Quasicyclic] [size] [numberToGen:printTop:printBottom] [directoryName]");
            return;
        }
        String genTypeArg = args[0];
        String sizeArg = args[1];
        String printArg = args[2];
        String dirArg = args[3];

        MatrixGenerator matrixGenerator;
        if (genTypeArg.equals("Gallager")) {
            matrixGenerator = new GallagerGenerator();
        } else if (genTypeArg.equals("Richardson")) {
            matrixGenerator = new RichardsonGenerator();
        } else if (genTypeArg.equals("Quasicyclic")) {
            matrixGenerator = new QuasicyclicGenerator();
        } else {
            System.err.println("Not supported generator type");
            return;
        }

        int size = Integer.parseInt(sizeArg);
        J = size;
        K = 2 * size;
        M = 4;
        r = M * J;
        c = M * K;

        String[] nums = printArg.split(":");
        int numberToGen = Integer.parseInt(nums[0]);
        int printTop = numberToGen;
        int printBottom = 0;
        if (nums.length > 1)
            printTop = Integer.parseInt(nums[1]);
        if (nums.length > 2)
            printBottom = Integer.parseInt(nums[2]);
        if (printTop + printBottom > numberToGen) {
            printTop = numberToGen;
            printBottom = 0;
        }

        String directoryToSave = dirArg;

        TreeSet<BaseMatrix> top = new TreeSet<>();
        TreeSet<BaseMatrix> bottom = new TreeSet<>();

        for (int t = 0; t < numberToGen; t++) {
            BaseMatrix m = matrixGenerator.genMatrix();
            top.add(m);
            bottom.add(m);
            if (top.size() > printTop)
                top.pollLast();
            if (bottom.size() > printBottom)
                bottom.pollFirst();
        }

        int topNumber = 1;
        for (BaseMatrix mtx : top) {
            String filename = String.format(directoryToSave + "/top%d.mtx", topNumber++);
            mtx.write(filename);
        }

        int bottomNumber = 1;
        for (BaseMatrix mtx : bottom) {
            String filename = String.format(directoryToSave + "/bot%d.mtx", bottomNumber++);
            mtx.write(filename);
        }
    }

    public static void main(String[] args) {
        new BaseMatrixGen().run(args);
    }
}
