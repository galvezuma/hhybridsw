package hhybridsw.outputprocessors;

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
     * Shows on console the interesting part of the results of the CUDASW++ execution.
     * @throws java.lang.Exception
     */
    @Override
    public void extractData() throws Exception {
        BufferedReader in = new BufferedReader(new StringReader(internalOutput.toString()));
        String l;
        boolean lookingGCUPS = true;
        boolean readingHits = false;
        while ((l = in.readLine()) != null) {
            if (lookingGCUPS && l.contains("GCUPS:")) {
                gigaCUPS = Double.parseDouble(l.substring(l.lastIndexOf("GCUPS:")+6));
                lookingGCUPS = false;
                readingHits = true;
                continue;
            }
            if (readingHits && l.startsWith("score:")) {
                getHits().add(l.substring(l.lastIndexOf("--")+2));
            }
        }
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
