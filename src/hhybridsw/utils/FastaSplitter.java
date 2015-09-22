package hhybridsw.utils;

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
import java.util.Iterator;
import java.util.List;

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

    private static final String FILENAME = "uniprot_sprot.fasta";
    private static final String FILEPATH = "D:\\swiss_prot";

    public static void main(String[] args) throws Exception {
        currentStatus = Status.START;
        Path path = Paths.get(FILEPATH, FILENAME);
        //The stream hence file will also be closed here
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null);
        //outputOrdered(result);
        outputSplitted(1.0, 43.0, 100.0);
    }

    public static void process(String database, Double ... pcts) throws Exception {
        currentStatus = Status.START;
        Path path = Paths.get(database);
        //The stream hence file will also be closed here
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> processLine(s));
        } catch (Exception e) { e.printStackTrace(); }
        currentStatus = Status.END;
        processLine(null);
        //outputOrdered(result);
        outputSplitted(pcts);
            Process process = Runtime.getRuntime().exec("createdatabase.sh");
            process.waitFor();
    }
    
    /*
    * It destroys the result
    */
    private static void outputOrdered(PriorityQueue<Protein> result) throws IOException {
        try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(FILEPATH+File.separatorChar+FILENAME+".sal")))){
            output.println(result.size());
            /* Esto noproduce el resultado ordenado 
            resultado.stream().forEachOrdered(s -> salida.println(s));
            */
            while(result.size() != 0)
                output.println(result.remove());
        }
    }
    
    private static void outputSplitted(Double ... pct) throws IOException {
        int aminoAcidsWritten = 0;
        Iterator<Protein> it = result.iterator();
        for(int i=0; i<pct.length; i++){
            try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(FILEPATH+File.separatorChar+FILENAME+"_"+i+".sal")))){
                do {
                    Protein p = it.next();
                    output.printf("%s\n%s\n", p.name, p.aminoacids);
                    aminoAcidsWritten += p.length;
                } while(pct[i] > (long)aminoAcidsWritten*100/numAminoAcids);
                out.println("Written: "+aminoAcidsWritten+" from "+numAminoAcids+"(Pct: "+((long)aminoAcidsWritten*100/numAminoAcids)+"%)"+pct[i]);
            }
        }
    }
    
    private static int currentLine = 0;
    private static void processLine(String s) {
        if ((++currentLine % 1000) == 0)
            out.println(currentLine);
        if (currentStatus == Status.START){
            if (s.startsWith(">")){
                currentProtein = new Protein();
                currentProtein.name = s;
                currentStatus = Status.READING_PROTEIN;
            } else {
                err.println("Invalid file format. It should start with >.");
                System.exit(1);
            }
        } else if (currentStatus == Status.END) {
            currentLine = 0;
            if (currentProtein != null){
                currentProtein.length = currentProtein.aminoacids.length();
                result.add(currentProtein);
                numAminoAcids += currentProtein.length;
                currentProtein = null;
            } else {
                err.println("Empty file.");
                System.exit(1);
            }
        } else { // if (currentStatus == Status.READING_PROTEIN){
            if (currentProtein != null){
                if (s.startsWith(">")){
                    currentProtein.length = currentProtein.aminoacids.length();
                    result.add(currentProtein);
                    result.add(currentProtein);
                    numAminoAcids += currentProtein.length;
                    currentProtein = new Protein();
                    currentProtein.name = s;
                } else {
                    currentProtein.aminoacids.append(s);
                }
            } else {
                err.println("Unexpected error. Current protein is null.");
                System.exit(1);
            }
        }
    }

}

