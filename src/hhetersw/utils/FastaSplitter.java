/*
 *  This class is used to partition a big file into several ones.
 * It may be used with two purposes:
 * 1) To use it from the main program in order to split the database in partitions
 *    required for the algorithms.
 * 2) To split a database whose size is known in order to create test files.
 */
package hhetersw.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.stream.Stream;
import static java.lang.System.err;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class FastaSplitter {

    private static final int THRESHOLD_FILE_SIZE = 300*1024*1024;
    private static String databaseName;

    private static enum Status {START, READING_PROTEIN, END}
    private static Status currentStatus = Status.START;
    //private static PriorityQueue<Protein> result = new PriorityQueue<>();
    private static final List<Protein> result = new ArrayList<>();
    private static long numAminoAcids = 0;
    private static long totalLengthAminoAcids = 0;
    private static Protein currentProtein = null;

    /*
    Release 2015_09 of 16-Sep-2015 of UniProtKB/TrEMBL contains 50825784 sequence entries,
    comprising 16880602444 amino acids.
    */
    private static final String FILENAME = "uniprot_trembl.fasta_1-128.out";
    private static final String FILEPATH = "D:\\swiss_prot";
    private static final long NUM_AMINOACIDS = 16_880_602_444L;
    private static long aminoAcidsWritten = -1;

    
    public static void main(String[] args) throws Exception {
        //System.out.println(calculateNumAminoAcids(FILEPATH+File.separator+FILENAME));
        //main_big(args);
        split(FILEPATH+File.separatorChar+FILENAME, 40.0,70.0,100.0);
    }
    public static void main_big(String[] args) throws Exception {
        numAminoAcids = 0;
        currentStatus = Status.START;
        Path path = Paths.get(FILEPATH, FILENAME);
        //The stream hence file will also be closed here
        setSizeFiles(FILEPATH+File.separatorChar+FILENAME, 50.0);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s, p -> saveToFile(p)));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null, p -> saveToFile(p));
    }
    
    public static void main_little(String[] args) throws Exception {
        numAminoAcids = 0;
        getResult().clear();
        currentStatus = Status.START;
        Path path = Paths.get(FILEPATH, FILENAME);
        //The stream hence file will also be closed here
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s, p -> addToResult(p)));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null, p -> addToResult(p));
        //outputOrdered(result);
        outputSplitted(FILEPATH+File.separator+FILENAME, 19.0, 43.0, 100.0);
    }

    public static void process(String database, Collection<AlgorithmTuple> values) throws Exception {
        Process process;
        // Removes any previous database
        for (AlgorithmTuple lt: values) {
            process = Runtime.getRuntime().exec(new String[] { "sh", "-c", lt.databaseDestroyCommandLine}, null, new File(database).getParentFile());
            out.println("Executing: " + lt.databaseDestroyCommandLine);
            process.waitFor();
        }
        // Prepares percentages to split the database
        Double pcts[] = new Double[values.size()];
        int idx=0;
        double accumulated = 0.0;
        for (AlgorithmTuple lt: values)
            pcts[idx++] = (accumulated += lt.splitPercentage);
        // Splits the database
        numAminoAcids = 0;
        getResult().clear();
        FastaSplitter.split(database, pcts);
        // Creates databases 
        for (AlgorithmTuple lt: values) {
            process = Runtime.getRuntime().exec(new String[] { "sh", "-c", lt.databaseCreateCommandLine}, null, new File(database).getParentFile());
            out.println("Executing: " + lt.databaseCreateCommandLine);
            process.waitFor();
        }
    }

    public static void split(String database, Double ... pcts) throws Exception {
        if (new File(database).length() < THRESHOLD_FILE_SIZE) {
            System.out.println("Splitting in memory...");
            FastaSplitter.splitInMemory(database, true, pcts);
        } else {
            System.out.println("Splitting on the fly...");
            databaseName = database;
            FastaSplitter.splitOnTheFly(pcts);
        }
    }
    
    public static void splitOnTheFly(Double ... pcts) throws Exception {
        totalLengthAminoAcids = calculateNumAminoAcids(databaseName);
        System.out.println("Num amino acids: "+totalLengthAminoAcids);
        currentStatus = Status.START;
        Path path = Paths.get(databaseName);
        //The stream hence file will also be closed here
        setSizeFiles(databaseName, pcts);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s, p -> saveToFile(p)));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null, p -> saveToFile(p));
        if (bigFile != null) {
            bigFile.close();
            System.out.println("File "+fileIdx+" has been created up to "+aminoAcidsWritten+" amino acids.");
        }
    }
    
    public static void splitInMemory(String database, boolean saveToFile, Double ... pcts) throws Exception {
        currentStatus = Status.START;
        Path path = Paths.get(database);
        //The stream hence file will also be closed here
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s, p -> addToResult(p)));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null, p -> addToResult(p));
        // outputOrdered(result);
        if (saveToFile)
            outputSplitted(database, pcts);
    }
    
    /*
    * It destroys the result
    */
    private static void outputOrdered(PriorityQueue<Protein> result) throws IOException {
        try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(FILEPATH+File.separatorChar+FILENAME+".out")))){
            output.println(result.size());
            /* This does not produce a sorted result 
            result.stream().forEachOrdered(s -> output.println(s));
            */
            while(result.size() != 0)
                output.println(result.remove());
        }
    }
    
    private static void outputSplitted(String database, Double ... pct) throws IOException {
        aminoAcidsWritten = 0;
        Iterator<Protein> it = getResult().iterator();
        for(int i=0; i<pct.length; i++){
            try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(database+"_"+i+".out", false)))){
                do {
                    Protein p = it.next();
                    output.printf("%s\n%s", p.name, p.aminoacids);
                    aminoAcidsWritten += p.length;
                } while((pct[i] > (double)aminoAcidsWritten*100/numAminoAcids) && it.hasNext());
                out.println("Written: "+aminoAcidsWritten+" from "+numAminoAcids+" (Pct: "+((double)aminoAcidsWritten*100/numAminoAcids)+"%) "+pct[i]);
            }
        }
        getResult().clear();
    }
    
    private static void processLine(String s, Consumer<Protein> cons) {
        //if ((++currentLine % 100000) == 0)
        //    out.print(currentLine+"\r");
        if (currentStatus == Status.START){
            if (s.startsWith(">")){
                currentProtein = new Protein();
                currentProtein.name = s;
                currentProtein.length = 0;
                currentStatus = Status.READING_PROTEIN;
            } else {
                err.println("Invalid file format. It should start with >.");
                System.exit(1);
            }
        } else if (currentStatus == Status.END) {
            if (currentProtein != null){
                numAminoAcids += currentProtein.length;
                cons.accept(currentProtein);
                currentProtein = null;
            } else {
                err.println("Empty file.");
                System.exit(1);
            }
        } else { // if (currentStatus == Status.READING_PROTEIN){
            if (currentProtein != null){
                if (s.startsWith(">")){
                    numAminoAcids += currentProtein.length;
                    cons.accept(currentProtein);
                    currentProtein = new Protein();
                    currentProtein.name = s;
                    currentProtein.length = 0;
                } else {
                    currentProtein.length += s.length();
                    currentProtein.aminoacids.append(s).append("\n");
                }
            } else {
                err.println("Unexpected error. Current protein is null.");
                System.exit(1);
            }
        }
    }
    
    // This adds proteins to a Collection to allow writing them later.
    private static void addToResult(Protein p){
        getResult().add(p);
    }

    /*
    * This adds the proteins to a file as soon as they are picked up from the input file.
    */
    private static Double[] pcts = null;
    private static int fileIdx = -1;
    private static PrintWriter bigFile = null;
    private static void setSizeFiles(String database, Double ... p) throws IOException {
        pcts = p;
        fileIdx = 0;
        aminoAcidsWritten = 0;
        bigFile = new PrintWriter(new BufferedWriter(new FileWriter(database+"_"+fileIdx+".out")));
    }
    
    private static void saveToFile(Protein p) {
        if (bigFile == null) {
            System.out.println("Probably not 100% has been requested. Current file is null.");
            System.exit(1);
        }
        bigFile.printf("%s\n%s", p.name, p.aminoacids);
        aminoAcidsWritten += p.length;
        if (((double)aminoAcidsWritten)*100/totalLengthAminoAcids > pcts[fileIdx]) { // Check if we have to change file
            bigFile.close();
            System.out.println("File "+fileIdx+" has been created up to "+aminoAcidsWritten+" amino acids.");
            fileIdx++;
            if (fileIdx < pcts.length) {
                try {
                    bigFile = new PrintWriter(new BufferedWriter(new FileWriter(databaseName+"_"+fileIdx+".out")));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                bigFile = null;
            }
        }
    }
    
    private static long aminoAcidsRead = 0;
    public static long calculateNumAminoAcids(String database){
        aminoAcidsRead = 0;
        currentStatus = Status.START;
        Path path = Paths.get(database);
        //The stream hence file will also be closed here
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s, p -> sumAminoAcids(p)));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null, p -> sumAminoAcids(p));
        return aminoAcidsRead;
    }

    private static void sumAminoAcids(Protein p) {
        aminoAcidsRead += p.length;
    }
    
    public static List<Protein> getResult() {
        return result;
    }
}

