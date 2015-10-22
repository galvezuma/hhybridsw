package hhetersw.outputprocessors;

import hhetersw.HitSet;
import hhetersw.Launcher;
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
        String l, l2, name = "";
        HitSet hs = null;
        while ((l = in.readLine()) != null) {
            if (l.startsWith("Query")) {
                name = l.substring(l.indexOf(":")+2);
                hs = new HitSet(name);
            }
            if (l.startsWith("Runtime:")) {
                // Runtime: 2.985968 seconds 37.977636 GCUPS
                l2 = removeLastWord(l);
                l = takeLastWord(l2);
                hs.setGigaCups(Double.parseDouble(l));
                l2 = removeLastWord(l2);
                l2 = removeLastWord(l2);
                l = takeLastWord(l2);
                hs.setTime(Double.parseDouble(l));
                hits.add(hs);
            } else if (l.startsWith("score")) {
                int initIndex = l.indexOf(" ", 8)+1;
                hs.getTargetSet().add(l.substring(initIndex));
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
Taken 0.140870 seconds to load the database
Run the alignment with 1 Xeon Phis (with each having 240 threads)
micIndex: 0 first: 0 numChunks 2796 firstAddrOff: 0 numBytesChunks 134208000
micIndex: 0 first: 2796 numChunks 2796 firstAddrOff: 134208000 numBytesChunks 134208000
micIndex: 0 first: 5592 numChunks 2796 firstAddrOff: 268416000 numBytesChunks 134208000
micIndex: 0 first: 8388 numChunks 2796 firstAddrOff: 402624000 numBytesChunks 134208000
micIndex: 0 first: 11184 numChunks 1316 firstAddrOff: 536832000 numBytesChunks 63168000
Xeon Phi 0 takes: 2.937965 seconds
Query (189 amino acids): gi|124460|sp|P05013.1|IFNA6_HUMAN
score 73: 160314L3000
score 71: 69817L3000
score 70: 159365L3000
score 70: 141325L3000
score 69: 104583L3000
score 68: 178485L3000
score 67: 41647L3000
score 66: 48679L3000
score 66: 21236L3000
score 66: 40008L3000
Runtime: 2.985968 seconds 37.977636 GCUPS
Xeon Phi 0 takes: 0.000000 seconds
Query (464 amino acids): gi|113936|sp|P01008.1|ANT3_HUMAN
score 73: 200000L3000
score 71: 199999L3000
score 70: 199998L3000
score 70: 199997L3000
score 69: 199996L3000
score 68: 199995L3000
score 67: 199994L3000
score 66: 199993L3000
score 66: 199992L3000
score 66: 199991L3000
Runtime: 0.009731 seconds 28610.139501 GCUPS
*/
