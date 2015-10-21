package hhybridsw.outputprocessors;

import hhybridsw.HitSet;
import hhybridsw.Launcher;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 *
 * @author galvez
 */
public class CudaSWProcessor extends Launcher {

    /**
     * Default constructor to be used in newInstance().
     */
    public CudaSWProcessor() {
    }
    
    /**
     * As a result of the CUDASW++ execution on a list of query sequences and a database,
     * every query sequence produces a HitSet, i.e. a set of hits in the database.
     * This function inserts into a set of Hitsets all the hits found by the algorithm.
     * The data comes from the output of the algorithm execution.
     * @throws java.lang.Exception
     */
    @Override
    public void extractData() throws Exception {
        BufferedReader in = new BufferedReader(new StringReader(internalOutput.toString()));
        String l;
        HitSet hs = null;
        while ((l = in.readLine()) != null) {
            if (l.contains("query:")) {
                if (hs != null) hits.add(hs);
                String name = l.substring(6); // We read the name of the query
                l = in.readLine();
                // We take the gigaCUPS as a number, just in case.
                double gigaCUPS = Double.parseDouble(l.substring(l.lastIndexOf("GCUPS:")+6));
                // We take the time as a number, just in case.
                // Length: 850 --- time: 349.779 (s) GCUPS: 1.45806
                double time = Double.parseDouble(l.substring(l.lastIndexOf("time:")+5, l.lastIndexOf("(s)")-1));
                // A new HitSet is created
                hs = new HitSet(name, gigaCUPS);
                hs.setTime(time);
                continue;
            }
            if (l.startsWith("score:")) {
                hs.getTargetSet().add(l.substring(l.lastIndexOf("--")+2));
            }
        }
        hits.add(hs);
        in.close();
    }
    
}

/*
*************************************************
the scoring matrix () can not be found
the default scoring matrix (BLOSUM62) is used
*************************************************
*************************************************
the scoring matrix () can not be found
the default scoring matrix (BLOSUM62) is used
*************************************************
--------------------------------
---------device(0)-------------
---------------------------------
name:GeForce 210
multiprocessor count:2
clock rate:1402000
shared memory:16384
global  memory:1073020928
registers per block:16384
Only 1 devices with compute capability >= 1.2
Using single-GPU (ID 0 ) to perform Smith-Waterman
/**********************************//*
	Model:			SIMT scalar
	Scoring matrix:			
	Gap Open penalty:		10
	Gap Extension penalty:		2
/**********************************//*
Loading database sequences from file into host memory...
Loading Stage 1 ---- width:7680 height:20278 size:594 (MB)
Loading database successfully
numSeqs: 200000 numThreshold: 200000
maxSeqLength: 3000 totalAminoAcids: 600000000
******************************
******************************
query:tr|Q38941|Q38941_ARATH Similarity to protein encoded by GenBank Accession Number U41815 (F23A5.3 protein) OS=Arabidopsis thaliana GN=F23A5.3 PE=4 SV=1 
Length: 850 --- time: 349.779 (s) GCUPS: 1.45806
----------Display the top 10 ----------
score: 85 -- 54836L3000
score: 77 -- 165929L3000
score: 75 -- 65879L3000
score: 74 -- 28580L3000
score: 74 -- 34024L3000
score: 74 -- 133677L3000
score: 73 -- 14990L3000
score: 73 -- 63578L3000
score: 72 -- 114115L3000
score: 72 -- 191315L3000
Reaching the end of the query file!
Finished!

*/
