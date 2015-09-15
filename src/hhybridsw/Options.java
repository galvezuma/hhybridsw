package hhybridsw;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author galvez
 */
class Options {
    
    private final HashMap<Character, String> opt = new HashMap<>();
    
    public Options(String[] args){
        for(int i=0; i<letters.length; i++){
            opt.put(letters[i], defaultValues[i]);
        }
        takeOptions(args);
        if ((getHelp() != null && getHelp().equals(Boolean.TRUE.toString())) || ! validate()){
            showHelp();
        }
    }
    
    private boolean validate(){
        boolean ret = true;
        for(int i=0; i<letters.length; i++){
            if(mustBeNotNull[i] && opt.get(letters[i]) == null){
                System.err.println(descriptions[i]+" (-"+letters[i]+") is mandatory");
                ret = false;
            }
            if(mustBeNumber[i] && ! isNumber(opt.get(letters[i]))){
                System.err.println(descriptions[i]+" (-"+letters[i]+") must be a number");
                ret = false;
            }
        }
        return ret;
    }
    
    @Override
    public String toString(){
        String ret = "";
        for(int i=0; i<letters.length; i++){
            ret += descriptions[i]+": "+opt.get(letters[i])+"\n";
        }
        return ret;
    }
    
    public final String getHelp(){
        return opt.get('h');
    }
    
    public final boolean isHelp(){
        return opt.get('h') != null;
    }
    
    public String getDatabase(){
        return opt.get('d');
    }
    
    public String getInput(){
        return opt.get('i');
    }
    
    public String getMatrix(){
        return opt.get('m');
    }
    
    public int getPenalty(){
        return Integer.parseInt(opt.get('p'));
    }
    
    public int getReward(){
        return Integer.parseInt(opt.get('r'));
    }
    
    public int getGapOpen(){
        return Integer.parseInt(opt.get('g'));
    }
    
    public int getGapExtend(){
        return Integer.parseInt(opt.get('e'));
    }

    private static final char[] letters = {'h', 'd', 'i', 'm', 'p', 'r', 'g', 'e'};
    private static final boolean[] mustBeNumber = {false, false, false, false, true, true, true, true};
    private static final boolean[] mustBeNotNull = {false, true, true, false, false, false, false, false};
    private static final String[] defaultValues = {null, null, null, "BLOSUM62", "-3", "1", "11", "1"};
    private static final String[] descriptions = {
        "Help",
        "Sequence Database name",
        "Query Input sequence filename",
        "Score Matrix name or filename",
        "Penalty for nucleotide mismatch",
        "Reward for nucleotide match",
        "Gap open penalty",
        "Gap extension penalty"
    };
    
    private void takeOptions(String[] args) {
        int pos = 0;
        while(pos < args.length){
            if (args[pos].length() != 2 || args[pos].charAt(0) != '-'){
                System.err.println("This is not an option("+args[pos]+"): Options must start with hyphen (-) followed by a single character, e.g. -i");
                pos++;
                opt.replace('h', "ERROR");
            } else if (contains(letters, args[pos].charAt(1))){
                char option = args[pos].charAt(1);
                pos++;
                if (option == 'h') {
                    // If we have to show Help due to an error then opt.get('h') != null and == "ERROR"
                    if (opt.get('h') == null) opt.replace('h', Boolean.TRUE.toString());
                } else if (pos < args.length) {
                    opt.replace(option, args[pos]);
                    pos++;
                } else {
                    System.err.println("Missing value in last option specified ("+args[pos-1]+")");
                    opt.replace('h', "ERROR");
                }
            } else {
                System.err.print("Invalid option("+args[pos]+"): Options are: -h");
                for(char c: letters) System.err.print(", -"+c);
                pos++;
                opt.replace('h', "ERROR");
            }
        }
    }
    
    public static boolean isNumber(String string) {
        try {
            Long.parseLong(string);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void showHelp() {
        System.err.println("\nOptions:");
        for(int i=0; i<letters.length; i++){
            System.err.println("-"+letters[i]+": "+descriptions[i]+((defaultValues[i] != null)?" ("+defaultValues[i]+")":""));
        }
    }

    private static boolean contains(char[] letters, char charAt) {
        for(char c: letters)
            if (c == charAt) return true;
        return false;
    }

    protected String replaceLetters(String line) throws Exception {
        for(char c: letters) {
            // Replaces [-c|--thing any] with --thing any data_associated_with_option_c
            Pattern p = Pattern.compile("\\[\\-"+c+"\\|");
            String[] parts = p.split(line);
            if (parts.length > 2) throw new Exception(line+". The same option (-"+c+") appears several times.");
            if (parts.length == 2) {
                line = parts[0] + parts[1].replaceFirst("\\]", opt.get(c));
            }
        }
        return line;
    }
}

/*
SWIPE 2.0.5 [Aug  9 2012 11:48:15]

Reference: T. Rognes (2011) Faster Smith-Waterman database searches
with inter-sequence SIMD parallelisation, BMC Bioinformatics, 12:221.

Usage: ./swipe [OPTIONS]
  -h, --help                 show help
  -d, --db=FILE              sequence database base name (required)
  -i, --query=FILE           query sequence filename (stdin)
  -M, --matrix=NAME/FILE     score matrix name or filename (BLOSUM62)
  -q, --penalty=NUM          penalty for nucleotide mismatch (-3)
  -r, --reward=NUM           reward for nucleotide match (1)
  -G, --gapopen=NUM          gap open penalty (11)
  -E, --gapextend=NUM        gap extension penalty (1)
  -v, --num_descriptions=NUM sequence descriptions to show (250)
  -b, --num_alignments=NUM   sequence alignments to show (100)
  -e, --evalue=REAL          maximum expect value of sequences to show (10.0)
  -k  --minevalue=REAL       minimum expect value of sequences to show (0.0)
  -c, --min_score=NUM        minimum score of sequences to show (1)
  -u, --max_score=NUM        maximum score of sequences to show (inf.)
  -a, --num_threads=NUM      number of threads to use [1-256] (1)
  -m, --outfmt=NUM           output format [0,7-9=plain,xml,tsv,tsv+] (0)
  -I, --show_gis             show gi numbers in results (no)
  -p, --symtype=NAME/NUM     symbol type/translation [0-4] (1)
  -S, --strand=NAME/NUM      query strands to search [1-3] (3)
  -Q, --query_gencode=NUM    query genetic code [1-23] (1)
  -D, --db_gencode=NUM       database genetic code [1-23] (1)
  -x, --taxidlist=FILE       taxid list filename (none)
  -N, --dump=NUM             dump database [0-2=no,yes,split headers] (0)
  -H, --show_taxid           show taxid etc in results (no)
  -o, --out=FILE             output file (stdout)

*/