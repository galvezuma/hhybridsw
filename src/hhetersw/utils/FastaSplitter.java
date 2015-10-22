/*
 *  This class is used to partition a big file into several ones.
 * Se emplea con dos propósitos:
 * 1) Para usarla desde el programa principal con objeto de particionar la base
 *    de datos en los bloques necesarios para cada algoritmo.
 * 2) Para trocear una base de datos conocida con objeto de crear ficheros de
 *    prueba.
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

    private static enum Status {START, READING_PROTEIN, END}
    private static Status currentStatus = Status.START;
    //private static PriorityQueue<Protein> result = new PriorityQueue<>();
    private static List<Protein> result = new ArrayList<>();
    private static int numAminoAcids = 0;
    private static Protein currentProtein = null;
    private static class Protein implements Comparable {
        String name;
        StringBuffer aminoacids = new StringBuffer();
        int length = 0;
        @Override
        public int compareTo(Object o) {
            return this.length - ((Protein)o).length;
        }
        @Override
        public String toString(){
            return length+"\n"+name+"\n"+aminoacids;
        }
    }
    /*
    Release 2015_09 of 16-Sep-2015 of UniProtKB/TrEMBL contains 50825784 sequence entries,
    comprising 16880602444 amino acids.
    */
    private static final String FILENAME = "SRR292176.fasta";
    private static final String FILEPATH = "D:\\basura\\sratoolkit.2.5.2-win64\\bin";
    private static final long NUM_AMINOACIDS = 16_880_602_444L;
    private static long aminoAcidsWritten = -1;

    
    public static void main(String[] args) throws Exception {
        System.out.println(calculateNumAminoAcids(FILEPATH+File.separator+FILENAME));
        //main_big(args);
    }
    public static void main_big(String[] args) throws Exception {
        numAminoAcids = 0;
        currentStatus = Status.START;
        Path path = Paths.get(FILEPATH, FILENAME);
        //The stream hence file will also be closed here
        setSizeFiles(6.25);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s, p -> saveToFile(p)));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null, p -> saveToFile(p));
    }
    
    public static void main_little(String[] args) throws Exception {
        numAminoAcids = 0;
        result.clear();
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
        // Prepare percentages to split the database
        Double pcts[] = new Double[values.size()];
        int idx=0;
        double accumulated = 0.0;
        for (AlgorithmTuple lt: values)
            pcts[idx++] = (accumulated += lt.splitPercentage);
        // Splits the database
        numAminoAcids = 0;
        result.clear();
        FastaSplitter.split(database, pcts);
        // Creates databases 
        for (AlgorithmTuple lt: values) {
            process = Runtime.getRuntime().exec(new String[] { "sh", "-c", lt.databaseCreateCommandLine}, null, new File(database).getParentFile());
            out.println("Executing: " + lt.databaseCreateCommandLine);
            process.waitFor();
        }
    }

    public static void split(String database, Double ... pcts) throws Exception {
        currentStatus = Status.START;
        Path path = Paths.get(database);
        //The stream hence file will also be closed here
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s, p -> addToResult(p)));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null, p -> addToResult(p));
        // outputOrdered(result);
        outputSplitted(database, pcts);
    }
    
    /*
    * It destroys the result
    */
    private static void outputOrdered(PriorityQueue<Protein> result) throws IOException {
        try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(FILEPATH+File.separatorChar+FILENAME+".out")))){
            output.println(result.size());
            /* Esto noproduce el resultado ordenado 
            resultado.stream().forEachOrdered(s -> salida.println(s));
            */
            while(result.size() != 0)
                output.println(result.remove());
        }
    }
    
    private static void outputSplitted(String database, Double ... pct) throws IOException {
        aminoAcidsWritten = 0;
        Iterator<Protein> it = result.iterator();
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
        result.clear();
    }
    
    private static int currentLine = 0;
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
            currentLine = 0;
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
    
    // This adds proteins to a Collection to allow writing them ordered.
    private static void addToResult(Protein p){
        result.add(p);
    }

    /*
    * This adds the proteins to a file as soon as they are taken from input file.
    */
    private static double[] pcts = null;
    private static int fileIdx = -1;
    private static PrintWriter bigFile = null;
    private static void setSizeFiles(double ... p) throws IOException {
        pcts = p;
        fileIdx = 0;
        aminoAcidsWritten = 0;
        bigFile = new PrintWriter(new BufferedWriter(new FileWriter(FILEPATH+File.separatorChar+FILENAME+"_"+fileIdx+".out")));
    }
    
    private static void saveToFile(Protein p) {
        if (bigFile == null) {
            System.out.println("Probably not 100% has been requested. Current file is null.");
            System.exit(1);
        }
        bigFile.printf("%s\n%s", p.name, p.aminoacids);
        aminoAcidsWritten += p.length;
        if ((double)aminoAcidsWritten*100/NUM_AMINOACIDS > pcts[fileIdx]) { // Check if we have to change file
            bigFile.close();
            System.out.println("File "+fileIdx+" has been created.");
            fileIdx++;
            if (fileIdx < pcts.length) {
                try {
                    bigFile = new PrintWriter(new BufferedWriter(new FileWriter(FILEPATH+File.separatorChar+FILENAME+"_"+fileIdx+".out")));
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
}

