package hhetersw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author galvez
 */
abstract public class Launcher extends Thread {
    
    // This stores the complete output of a program
    protected StringBuffer internalOutput = new StringBuffer();
    protected double gigaCUPS = 0.0;
    private double timeAlgorithm = 0.0;
    protected Set<HitSet> hits = new HashSet<>();
    private double timeTaken = 0;
    
    protected String key;
    private String line;

    /**
     * Default constructor to be used in children classes ().
     */
    public Launcher() {
    }
    
    @Override
    public void run(){
        Process process;
        try {
            long initTime = System.currentTimeMillis();
            System.out.println("Executing: " + line);
            process = Runtime.getRuntime().exec(line);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
            // This takes asynchronously the error of the command into a StringBuffer
            new Thread(){
                @Override
                public void run(){
                    String l;
                    try {
                        while ((l = err.readLine()) != null){
                            appendTointernalOutput(l+"\n");
                            if (userActionRequired(l))
                                executeUserAction(l, out);
                        }
                        err.close();
                    } catch (IOException ex) { ex.printStackTrace(); }
                }
            }.start();
            // This takes the output of the command into a StringBuffer
            String l;
            while ((l = in.readLine()) != null) {
                appendTointernalOutput(l+"\n");
                if (userActionRequired(l))
                    executeUserAction(l, out);
            }
            in.close();
            out.close();
            //System.out.println(internalOutput);
            timeTaken = (System.currentTimeMillis() - initTime) / 1000.0;
            extractData();
            calculateTimeAndCUPS();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }   

    protected boolean userActionRequired(String l) {
        return false;
    }

    @SuppressWarnings("empty-statement")
    protected void executeUserAction(String l, PrintWriter out) {
        ; // Empty
    }
    
    protected void calculateTimeAndCUPS() {
        double totalTime = 0.0;
        double totalGigaCups = 0.0;
        for(HitSet tempHs: hits){
            double auxTotal = totalGigaCups * totalTime;
            double auxPart  = tempHs.getGigaCups() * tempHs.getTime();
            totalTime += tempHs.getTime();
            totalGigaCups = (auxTotal + auxPart) / totalTime;
        }
        gigaCUPS = totalGigaCups;
        timeAlgorithm = totalTime;
    }
    
    private synchronized void appendTointernalOutput(String text) {
        internalOutput.append(text);
    }

    abstract public void extractData() throws Exception;

    public void setKey(String id) {
        this.key = id;
    }
    
    public String getKey() {
        return key;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public double getGigaCUPS() {
        return gigaCUPS;
    }

    public Set<HitSet> getHits() {
        return hits;
    }

    public double getTimeTaken() {
        return timeTaken;
    }

    public double getTimeAlgorithm() {
        return timeAlgorithm;
    }
    
}
