package hhybridsw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author galvez
 */
abstract public class Launcher extends Thread {
    
    // This stores the complete output of a program
    protected StringBuffer internalOutput = new StringBuffer();
    protected double gigaCUPS = 0.0;
    protected List<String> hits = new ArrayList<>();
    private double timeTaken = 0;
    
    private String key;
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
            process = Runtime.getRuntime().exec(line);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            // This takes asynchronously the error of the command into a StringBuffer
            new Thread(){
                @Override
                public void run(){
                    String l;
                    try {
                        while ((l = err.readLine()) != null)
                            appendTointernalOutput(l+"\n");
                        err.close();
                    } catch (IOException ex) { ex.printStackTrace(); }
                }
            }.start();
            // This takes the output of the command into a StringBuffer
            String l;
            while ((l = in.readLine()) != null) {
                appendTointernalOutput(l+"\n");
            }
            in.close();
            System.out.println(internalOutput);
            timeTaken = (System.currentTimeMillis() - initTime) / 1000.0;
            extractData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    /**
     * @return the gigaCUPS
     */
    public double getGigaCUPS() {
        return gigaCUPS;
    }

    /**
     * @return the hits
     */
    public List<String> getHits() {
        return hits;
    }

    /**
     * @return the timeTaken
     */
    public double getTimeTaken() {
        return timeTaken;
    }
    
}
