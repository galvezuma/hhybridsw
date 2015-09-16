package hhybridsw.outputprocessors;

import hhybridsw.Launcher;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 *
 * @author galvez
 */
public class SwaphiProcessor extends Launcher {

    /**
     * Default constructor to be used in newInstance().
     */
    public SwaphiProcessor() {
    }
    
    /**
     * Shows on console the interesting part of the results of the SWAPHI execution.
     * @throws java.lang.Exception
     */
    @Override
    public void extractData() throws Exception {
        BufferedReader in = new BufferedReader(new StringReader(internalOutput.toString()));
        String l;
        while ((l = in.readLine()) != null) {
            if (l.startsWith("Runtime:")) {
                l = removeLastWord(l);
                l = takeLastWord(l);
                gigaCUPS = Double.parseDouble(l);
            } else if (l.startsWith("score")) {
                int initIndex = l.indexOf(" ", 8)+1;
                //getHits().add(l.substring(initIndex));
            }
        }
        in.close();
    }
    private static String removeLastWord(String text){
        String ret = text.trim();
        ret = ret.substring(0, ret.lastIndexOf(" "));
        return ret.trim();
    }
    private static String takeLastWord(String text){
        String ret = text.trim();
        ret = ret.substring(ret.lastIndexOf(" ")+1);
        return ret;
    }
}

/*
Number of Xeon Phi devices installed: 1
Check the usability of each Xeon Phi device
Inter-task parallelization is used
Load scoring matrix
load database from file
#file length: 600000000
Number of sequences for Intel Xeon Phi 0: 200000
numSeqs: 200000 numChunks: 12500 minLength: 3000 maxLength: 3000 totalNumAminoAcids: 600000000
Taken 0.140040 seconds to load the database
Run the alignment with 1 Xeon Phis (with each having 240 threads)
micIndex: 0 first: 0 numChunks 2796 firstAddrOff: 0 numBytesChunks 134208000
micIndex: 0 first: 2796 numChunks 2796 firstAddrOff: 134208000 numBytesChunks 134208000
Number of Xeon Phi devices installed: 1
Check the usability of each Xeon Phi device
Inter-task parallelization is used
Load scoring matrix
load database from file
#file length: 600000000
Number of sequences for Intel Xeon Phi 0: 200000
numSeqs: 200000 numChunks: 12500 minLength: 3000 maxLength: 3000 totalNumAminoAcids: 600000000
Taken 0.133558 seconds to load the database
Run the alignment with 1 Xeon Phis (with each having 240 threads)
micIndex: 0 first: 0 numChunks 2796 firstAddrOff: 0 numBytesChunks 134208000
micIndex: 0 first: 2796 numChunks 2796 firstAddrOff: 134208000 numBytesChunks 134208000
micIndex: 0 first: 5592 numChunks 2796 firstAddrOff: 268416000 numBytesChunks 134208000
micIndex: 0 first: 8388 numChunks 2796 firstAddrOff: 402624000 numBytesChunks 134208000
micIndex: 0 first: 11184 numChunks 1316 firstAddrOff: 536832000 numBytesChunks 63168000
Xeon Phi 0 takes: 9.935149 seconds
Query (850 amino acids): tr|Q38941|Q38941_ARATH
score 85: 54836L3000
score 77: 165929L3000
score 75: 65879L3000
score 74: 28580L3000
score 74: 34024L3000
score 74: 133677L3000
score 73: 63578L3000
score 73: 14990L3000
score 72: 191315L3000
score 72: 114115L3000
Runtime: 10.046178 seconds 50.765574 GCUPS

*/
