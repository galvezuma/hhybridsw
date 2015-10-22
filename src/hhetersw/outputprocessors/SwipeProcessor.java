package hhetersw.outputprocessors;

import hhetersw.HitSet;
import hhetersw.Launcher;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 *
 * @author galvez
 */
public class SwipeProcessor extends Launcher {

    /**
     * Default constructor to be used in newInstance().
     */
    public SwipeProcessor() {
    }
    
    /**
     * Shows on console the interesting part of the results of the mpiswipe execution.
     * @throws java.lang.Exception
     */
    @Override
    public void extractData() throws Exception {
        BufferedReader in = new BufferedReader(new StringReader(internalOutput.toString()));
        String l, name = "";
        boolean lookingGCUPS = false;
        boolean lookingHits = false;
        boolean readingHits = false;
        HitSet hs = null;
        while ((l = in.readLine()) != null) {
            if (l.contains("Query description:")) {
                if (hs != null) hits.add(hs);
                name = l.substring(19); // We read the name of the query
                lookingGCUPS = true;
                continue;
            }
            // Elapsed:           13.97s
            if (lookingGCUPS && l.startsWith("Elapsed:")) {
                double time = Double.parseDouble(l.substring(l.lastIndexOf("Elapsed:")+8, l.lastIndexOf("s")-1));
                // A new HitSet is created
                l = in.readLine();
                // Speed:             36.507 GCUPS
                double gigaCUPS = Double.parseDouble(l.substring(l.lastIndexOf("Speed:")+6, l.lastIndexOf("GCUPS")-1));
                // A new HitSet is created
                hs = new HitSet(name, gigaCUPS);
                hs.setTime(time);
                lookingGCUPS = false;
                lookingHits = true;
                continue;
            }
            if (lookingHits && l.contains("Sequences producing significant alignments")) {
                in.readLine(); // Skip blank line. Just after are the hits, if any.
                lookingHits = false;
                readingHits = true;
                continue;
            }
            if (readingHits) {
                if (l.isEmpty()) break; // No more hits
                hs.getTargetSet().add(removeLastWord(removeLastWord(l)));
            }
        }
        hits.add(hs);
        in.close();
    }
    private static String removeLastWord(String text){
        String ret = text.trim();
        ret = ret.substring(0, ret.lastIndexOf(" "));
        return ret.trim();
    }
}

/*
SWIPE 2.0.5 [Aug  9 2012 11:48:18]

Reference: T. Rognes (2011) Faster Smith-Waterman database searches
with inter-sequence SIMD parallelisation, BMC Bioinformatics, 12:221.

Database file:     ../data/simdb.fasta
Database title:    simdb.fasta
Database time:     Sep 7, 2015  9:57 PM
Database size:     600000000 residues in 200000 sequences
Longest db seq:    3000 residues
Query file name:   ../data/Queries/Q38941.fasta
Query length:      850 residues
Query description: tr|Q38941|Q38941_ARATH Similarity to protein encoded by GenB
                   ank Accession Number U41815 (F23A5.3 protein) OS=Arabidopsis
                    thaliana GN=F23A5.3 PE=4 SV=1                              
Score matrix:      BLOSUM62
Gap penalty:       11+1k
Max expect shown:  10
Min score shown:   1
Max matches shown: 250
Alignments shown:  100
Show gi's:         0
Show taxid's:      0
Threads:           4
Symbol type:       Amino acid

Searching..................................................done

Search started:    Tue, 15 Sep 2015 21:38:57 UTC
Search completed:  Tue, 15 Sep 2015 21:39:11 UTC
Elapsed:           13.97s
Speed:             36.507 GCUPS

                                                                 Score    E
Sequences producing significant alignments:                      (bits) Value

gnl|BL_ORD_ID|54835 54836L3000                                         40   0.28 
gnl|BL_ORD_ID|165928 165929L3000                                       36   6.8  

>gnl|BL_ORD_ID|54835 54836L3000                                                
          Length = 3000

 Score = 40.4 bits (93), Expect = 0.28 
 Identities = 35/154 (22%), Positives = 74/154 (48%), Gaps = 16/154 (10%)

Query:  408 VWDRKGKVQKELIDSAFEAPLSLHKELNHVEEEVRFGSFSLKLQNVVTDRVV----LSDI 463
            VW R GK++++++D      L +H +     + +  G +S+ + NV+ + V+    L D 
Sbjct: 1167 VWQRGGKIEZQMVDRBSHCGL-MHPKTACEHQMIMLGLYSMHMANVLQEEVLRYBWLKDT 1225

Query:  464 CRSYIGIIEKQLEVAGLSTSAKLFLM-------HQ-VMVWELIKVLFSER-QSTERLMYA 514
            C++ I +I    +      + K F++       H+   +WE  ++ F +R +  ER    
Sbjct: 1226 CKNRIMLIRCDNZYZHNXLNQKDFVVSXZFIZNHEXTQIWE-DEINFRZRCKDMERBQGQ 1284

Query:  515 ASDNEEDVMQDVKEDSAKID-TEALPLIRRAEFS 547
            A   ++D+M +  +   ++  TE  PL +  ++S
Sbjct: 1285 AEQFQDDMMGEQDZQDRQMGYTEKEPLHQNIBYS 1318


>gnl|BL_ORD_ID|165928 165929L3000                                              
          Length = 3000

 Score = 35.8 bits (81), Expect = 6.8  
 Identities = 16/47 (34%), Positives = 25/47 (53%), Gaps = 4/47 (8%)

Query:  688 DGFVSDNKHSDILYYLMLLHSKEEEEFGFLQTMFSAFSSTDDPLDYH 734
            D  + ++K  D L++L    SK++E    +   F+A   TDD  DYH
Sbjct: 2653 DAKICBBKQQDTLFFL----SKZZEHIWPVPWXFAAMHKTDDSXDYH 2695

*/
