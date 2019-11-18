package ass.view;

import java.util.HashSet;
import java.util.Hashtable;

public class MasterTables {
    public MasterTables() {
        super();
    }
    protected static Hashtable<String, String> mriTable = 
                 new Hashtable<String, String>();
    protected static Hashtable<String, String> nonMriTable = 
                    new Hashtable<String, String>();
    protected static HashSet<String> pseudoTable = 
                    new HashSet<String>();
    
    protected static Hashtable<String, String> addressTable = 
                    new Hashtable<String, String>();
    
   
    
   public static  void initTables()
   {
       // set psudeo values
       pseudoTable.add("ORG");
        pseudoTable.add("END");
        pseudoTable.add("DEC");
        pseudoTable.add("HEX");
       // set mriTable Values
        mriTable.put("AND", "000");
        mriTable.put("ADD", "001");
        mriTable.put("LDA", "010");
        mriTable.put("STA", "011");
        mriTable.put("BUN", "100");
        mriTable.put("BSA", "101");
        mriTable.put("ISZ", "110");
        // set nonMriTable
        nonMriTable.put("CLA", "0111100000000000");
        nonMriTable.put("CLE", "0111010000000000");
        nonMriTable.put("CMA", "0111001000000000");
        nonMriTable.put("CME", "0111000100000000");
        nonMriTable.put("CIR", "0111000010000000");
        nonMriTable.put("CIL", "0111000001000000");
        nonMriTable.put("INC", "0111000000100000");
        nonMriTable.put("SPA", "0111000000010000");
        nonMriTable.put("SNA", "0111000000001000");
        nonMriTable.put("SZA", "0111000000000100");
        nonMriTable.put("SZE", "0111000000000010");
        nonMriTable.put("HLT", "0111000000000001");
        nonMriTable.put("INP", "1111100000000000");
        nonMriTable.put("OUT", "1111010000000000");
        nonMriTable.put("SKI", "1111001000000000");
        nonMriTable.put("SKO", "1111000100000000");
        nonMriTable.put("ION", "1111000010000000");
        nonMriTable.put("IOF", "1111000001000000");
    }
}
