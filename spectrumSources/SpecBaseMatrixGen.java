import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SpecBaseMatrixGen {

    Random rng = new Random(19);
    final int SPECTRUM_SIZE = 12;
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

        protected int[][] genArray() {
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
            return a;
        }

        @Override
        public BaseMatrix genMatrix() {
            return new BaseMatrix(genArray());
        }
    }

    class RichardsonRestrictedGenerator extends RichardsonGenerator {

        @Override
        public BaseMatrix genMatrix() {
            nextMatrix:
            while (true) {
                int[][] a = genArray();
                int[] sum = new int[c];
                for (int i = 0; i < r; i++) {
                    for (int j = 0; j < c; j++) {
                        if (a[i][j] != -1)
                            sum[j] += 1;
                    }
                }
                for (int j = 0; j < c; j++)
                    if (sum[j] != J)
                        continue nextMatrix;

                // There is J not zeros in every column - good matrix
                return new BaseMatrix(a);
            }
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
        int id;
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

    class Node {
        TreeSet<BaseMatrix> specs;
        Map<Long, Node> nodes;
        int level;

        public Node(int level) {
            this.level = level;
            nodes = new HashMap<>();
            specs = new TreeSet<>(new Comparator<BaseMatrix>() {
                @Override
                public int compare(BaseMatrix b1, BaseMatrix b2) {
                    long[] o1 = b1.spectrum;
                    long[] o2 = b2.spectrum;
                    for (int i = 0; i < o1.length; i++) {
                        if (o1[i] != o2[i]) {
                            return Long.compare(o1[i], o2[i]);
                        }
                    }
                    return 0;
                }
            });
        }
    }

    void insert(Node node, BaseMatrix mtx) {
        int index = node.level;
        node.specs.add(mtx);
        if (index < mtx.spectrum.length) {
            if (!node.nodes.containsKey(mtx.spectrum[index])) {
                node.nodes.put(mtx.spectrum[index], new Node(index + 2));
            }
            Node childNode = node.nodes.get(mtx.spectrum[index]);
            insert(childNode, mtx);
        }
    }

    void fillLevelNodes(Node node, int depth, ArrayList<Node> levelNodes) {
        if (depth == 0) {
            levelNodes.add(node);
        } else {
            for (Node subNode : node.nodes.values()) {
                fillLevelNodes(subNode, depth - 1, levelNodes);
            }
        }
    }

    void run(String[] args) {
        /*if (args.length != 5) {
            System.err.println("usage: SpecBaseMatrixGen [Gallager/Richardson/RichardsonRestricted/Quasicyclic] [size] [M] [numberToGen:printTop:printBottom] [directoryName]");
            return;
        }
        String genTypeArg = args[0];
        String sizeArg = args[1];
        String mArg = args[2];
        String printArg = args[3];
        String dirArg = args[4];*/

        String genTypeArg = "Richardson";
        String sizeArg = "4";
        String mArg = "3";
        String printArg = args[0];
        String dirArg = args[1];

        MatrixGenerator matrixGenerator;
        if (genTypeArg.equals("Gallager")) {
            matrixGenerator = new GallagerGenerator();
        } else if (genTypeArg.equals("Richardson")) {
            matrixGenerator = new RichardsonGenerator();
        } else if (genTypeArg.equals("RichardsonRestricted")) {
            matrixGenerator = new RichardsonRestrictedGenerator();
        } else if (genTypeArg.equals("Quasicyclic")) {
            matrixGenerator = new QuasicyclicGenerator();
        } else {
            System.err.println("Not supported generator type");
            return;
        }

        int size = Integer.parseInt(sizeArg);
        J = size;
        K = 2 * size;
        M = Integer.parseInt(mArg);
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
        Node root = new Node(4);

        long time = System.currentTimeMillis();
        for (int t = 0; t < numberToGen; t++) {
            if (t != 0 && t % 1000 == 0) {
                System.err.print(t / 1000 + " ");
                long cur = System.currentTimeMillis();
                System.err.println((cur - time) + " ms");
                time = cur;
            }
            BaseMatrix m = matrixGenerator.genMatrix();
            m.id = t;
            insert(root, m);
        }
        for (int depth = 1; depth <= 4; depth++) {
            Path depthDir = Paths.get(dirArg + File.separator + "depth" + depth);
            ArrayList<Node> levelNodes = new ArrayList<>();
            fillLevelNodes(root, depth, levelNodes);
            Collections.sort(levelNodes, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return Integer.compare(o2.specs.size(), o1.specs.size());
                }
            });
            for (int i = 0; i < Math.min(7, levelNodes.size()); i++) {
                Node node = levelNodes.get(i);
                Path nodeDir = Paths.get(depthDir.toString() + File.separator + "node" + i);
                int test = 0;
                for (BaseMatrix mtx : node.specs) {
                    File file = new File(nodeDir.toString() + File.separator + "test" + test + ".mtx");
                    ++test;
                    file.getParentFile().mkdirs();
                    mtx.write(file.toString());
                    if (test >= 100)
                        break;
                }
            }
        }
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory in mb: " + memory / 1024 / 1024);
    }

    public static void main(String[] args) {
        new SpecBaseMatrixGen().run(args);
    }
}
