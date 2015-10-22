package hhetersw;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author galvez
 */
public class HitSet {
    private String query;
    private final HashSet<String> targetSet;
    private double gigaCUPS = 0.0;
    private double time = 0.0;
    
    public HitSet(String query){
        this.query = query;
        this.targetSet = new HashSet<>();
    }
    
    public HitSet(String query, double gigaCUPS){
        this(query);
        this.gigaCUPS = gigaCUPS;
    }

    private HitSet(HitSet h) {
        this.query = h.query;
        this.targetSet = new HashSet<>(h.targetSet);
        this.gigaCUPS = h.gigaCUPS;
    }
    
    /*
    * Two HitSet should be equal iif their query strings are equal.
    * However, different algorithms retains different parts of the gi
    * from the fasta file, e.g.:
    * tr|Q38941|Q38941_ARATH Similarity to protein encoded by GenBank Accession Number U41815 (F23A5.3 protein) OS=Arabidopsis thaliana GN=F23A5.3 PE=4 SV=1 
    * tr|Q38941|Q38941_ARATH Similarity to protein encoded by GenB
    * tr|Q38941|Q38941_ARATH
    * Therefore, we consider they are equal if one is a prefix of the other, no matter the order.
    */
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        else if (!(obj instanceof HitSet)) return false;
        else return anyPrefix(query, ((HitSet)obj).query);
    }
    
    private boolean anyPrefix(String a, String b) {
        return a.startsWith(b) || b.startsWith(a);
    }

    public String getQuery() {
        return query;
    }

    public HashSet<String> getTargetSet() {
        return targetSet;
    }

    public void setGigaCups(double s) {
        this.gigaCUPS = s;
    }
    
    public double getGigaCups(){
        return this.gigaCUPS;
    }
    
    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
    
    public void union(HitSet h) throws Exception {
        if (! anyPrefix(query, h.query)) throw new Exception("Unmatching keys in union of HitSets");
        // The longest query text is retained
        if (h.query.length() > query.length())
            query = h.query;
        targetSet.addAll(h.targetSet);
        double tempGCUP = (gigaCUPS * time) + (h.gigaCUPS * h.time);
        time = Double.max(time, h.time);
        gigaCUPS = tempGCUP / time;
    }
    
    @Override
    public String toString(){
        String ret = "Query: " + query + "\nGigaCUPS: " + gigaCUPS + "\nTime elapsed: " + time + "\n";
        for(String s: targetSet)
            ret += s + "\n";
        return ret;
    }
    
    /*
    * Changes the first parameter and empties the second one
    */
    public static void union(Set<HitSet> dest, Set<HitSet> b) throws Exception {
        // Common hitsets are merged into dest
        for(HitSet h: dest) {
            for(HitSet h2: b){
                if (h.equals(h2)){
                    h.union(h2);
                    b.remove(h2);
                    break;
                }
            }
        }
        // Hitsets contained in b but not in dest are added at the end.
        dest.addAll(b);
    }

}
