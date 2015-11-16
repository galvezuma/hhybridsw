package hhetersw.utils;

/**
 *
 * @author SICUMA
 */
public  class Protein implements Comparable {
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
