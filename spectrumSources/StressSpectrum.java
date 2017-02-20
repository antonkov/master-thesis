import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class StressSpectrum {

    private void run(int spectrumSize, String[] filesAndFolders) {
        PrintWriter out = new PrintWriter(System.out);

        System.out.println("Results");

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

                long[] specFast = new Spectrum(spectrumSize).solve(J, K, M, ws).spectrum;
                long[] specSlow = new SlowSpectrum(spectrumSize).solve(J, K, M, ws).spectrum;
                for (int i = 0; i < spectrumSize; i++) {
                    if (specFast[i] != specSlow[i]) {
                        throw new AssertionError("Error: " +  filename + " at " + i + ": should be " + specSlow[i] + " but " + specFast[i]);
                    }
                }

                System.out.println(filename + " ok");
            } catch (FileNotFoundException e) {
                System.err.println("File " + filename + " not found. Ignoring...");
            }
        }

        out.close();
    }

    public static void main(String[] args) {
        int spectrumSize = Integer.parseInt(args[0]);
        String[] files = new String[args.length - 1];
        for (int i = 1; i < args.length; i++)
            files[i - 1] = args[i];
        new StressSpectrum().run(spectrumSize, files);
    }

}
