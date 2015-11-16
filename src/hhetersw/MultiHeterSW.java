package hhetersw;

import hhetersw.utils.AlgorithmTuple;
import hhetersw.utils.FastaSplitter;
import hhetersw.utils.Protein;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sicuma
 */
public class MultiHeterSW extends HHeterSW {
    private static final String FILENAME = "aSequence.fa";

    public static void main(String args[]) throws Exception {
        Options options = new Options(args);
        String queryFile = options.getInput();
        String filename = FILENAME;
        options.setInput(filename);
        HashMap<String, AlgorithmTuple> algorithms = loadExecutions(options);
        FastaSplitter.splitInMemory(queryFile, false, (Double[])null);
        List<Protein> result = FastaSplitter.getResult();
        for(Protein p: result){
            save(filename, p);
            List<Launcher> launchers = new ArrayList<>();
            prepareExecuteAndWaitLaunchers(algorithms, launchers);
            showResults(launchers, System.out);
        }
    }
    private static void save(String filename, Protein p) throws IOException{
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            String fasta = p.toString();
            fasta = fasta.substring(fasta.indexOf('\n')+1);
            out.print(fasta);
        }
    }
}
