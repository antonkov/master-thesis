import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by antonkov on 2/1/17.
 */
public class RandomMarking {

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

    void run(String[] args) throws FileNotFoundException {
        int start = 0;
        if (args.length < 3) {
            System.err.println("usage: RandomMarking [numberSamples] [M] [outFolder] [filenames]*");
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
            int tried = 0;
            int sample = 0;
            int numZeros = 0;
            while (Math.pow(10, numZeros) < numberSamples)
                numZeros++;
            while (sample < numberSamples) {
                tried++;
                String baseName = file.getName().split("\\.")[0];
                String name = String.format("%s/%s_%0" + numZeros + "d.mtx", outFolder, baseName, sample);
                int[][] hMarked = markMatrix(hd, M);
                if (USE_FILTER) {
                    Spectrum.SolveReport report = new Spectrum(20).solve(r, c, M, hMarked);
                    if (!filter(report.spectrum)) {
                        continue;
                    }
                }
                try (PrintWriter out = new PrintWriter(name)) {
                    out.println("proto_matrix"); // matrix format
                    out.println(r + " " + c);
                    out.println(M);
                    for (int i = 0; i < r; i++) {
                        for (int j = 0; j < c; j++) {
                            out.print(hMarked[i][j] + " ");
                        }
                        out.println();
                    }
                    sample++;
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        new RandomMarking().run(args);
    }
}
