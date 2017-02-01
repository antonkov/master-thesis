import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by antonkov on 2/1/17.
 */
public class RandomMarking {

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
            Random rng = new Random(19);
            for (int sample = 0; sample < numberSamples; sample++) {
                String baseName = file.getName().split("\\.")[0];
                String name = String.format("%s/%s%03d.mtx", outFolder, baseName, sample);
                try (PrintWriter out = new PrintWriter(name)) {
                    out.println("proto_matrix"); // matrix format
                    out.println(r + " " + c);
                    out.println(M);
                    for (int i = 0; i < r; i++) {
                        for (int j = 0; j < c; j++) {
                            if (hd[i][j] != -1) {
                                out.print(rng.nextInt(M) + " ");
                            } else {
                                out.print(hd[i][j] + " ");
                            }
                        }
                        out.println();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        new RandomMarking().run(args);
    }
}
