package hhybridsw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author galvez
 */
abstract public class Launcher extends Thread {
    
    // This stores the complete output of a program
    protected StringBuffer internalOutput = new StringBuffer();
    
    private String id;
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
            process = new ProcessBuilder(line).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String l;

            while ((l = br.readLine()) != null) {
              internalOutput.append(l+"\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    abstract public void extractData();

    public void setId(String id) {
        this.id = id;
    }

    public void setLine(String line) {
        this.line = line;
    }
    
}
