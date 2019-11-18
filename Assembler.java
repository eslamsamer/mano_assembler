package ass.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Assembler {
    public Assembler() {
        super();
    }
    protected static ArrayList<String> lines = new ArrayList();;
    protected static ArrayList<String> errors =  new ArrayList();
    public static void firstPass(String code)
    {
        MasterTables.addressTable.clear();
        // read all lines
        lines.addAll(Arrays.asList(code.split("\\r?\\n")));
        // initialize LC
        int lc = 0;
        
        // loop all lines
         if(lines.size()==0)
        {
             errors.add("No Code to compile");
        }
        
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
            
            //remove comments before splitting
            if(line.contains("//"))
            {
             line =  line.replaceFirst(line.substring(line.indexOf("//")),""); 
            }
            
            String[] tokens = line.trim().split("\\s+");
            
            if(!validLineSyntax(line, i))
            {
                continue;
            }
            //right Label 
             else if(tokens[0].endsWith(",") )
            {
                MasterTables.addressTable.put(tokens[0].substring(0, tokens[0].length() - 1) , Integer.toBinaryString(lc));
                lc++;
            }
            
            // right ORG
            else if (tokens[0].equals("ORG"))
            {
                lc = getLC(i);
                if(lc == -1)
                errors.add("error at line " + (i+1) + " -- specified ORG address can't be used"); 
            }
           // right END
            else if(tokens[0].equals("END") )
            {
                return;
            }
            // not (END, ORG or Label)
            else if( !(tokens[0].equals("END")||tokens[0].equals("ORG")||tokens[0].endsWith(",") ))
            {
                lc++;
            }
           
            else 
            {
                errors.add("error at line " + (i+1)); 
            }
            
            lines.set(i,line.trim());
            
        }
    }
    
    public static void secondPass() 
    {
        // initialize LC
        int lc = 0;
        int removedLines = 0;
        // loop all lines
        int size = lines.size();
         if(size==0)
        {
             errors.add("No Code inserted");
        }
        
        for (int i = 0; i < size; i++)
        {
            String line = lines.get(i-removedLines);
            
            String[] tokens = line.trim().split("\\s+");
            
            //get the instructuion token
            String instr ;
            String address = null;
            String I = "0";
            int instrShift = 0;
            
            // neglect labels
            if(tokens[0].endsWith(","))
            {
                instrShift = 1;
                line = line.replaceFirst(tokens[0], " ");
            }
            else
                instrShift = 0;
            
           instr = tokens[instrShift]; 
            
           // pseudo instruction
            if( MasterTables.pseudoTable.contains(instr))
            {
               
                if (tokens.length>(instrShift+2))
                {
                    errors.add("error at line " + (i+1) + " -- ivalid comment, please use // to start your comment");  
                }   
                          
                else if(instr.equals("ORG"))
                 {
                     lc = getLC(i-removedLines);
                     
                     if(lc == -1)
                     {
                       errors.add("error at line " + (i+1) + " -- specified ORG address can't be used"); 
                       break;
                     }
                      
                     lines.remove(i-removedLines);
                    removedLines++;
                    continue;
                 }
                else if (instr.equals("END"))
                 {
                     lines.remove(i-removedLines);
                     removedLines++;
                    break;
                 }
                else if (instr.equals("DEC"))
                {
                    try
                    {
                        // negative number 2's complement gives too much ones in the output
                        if(tokens[instrShift+1].startsWith("-") && Integer.toBinaryString(Integer.parseInt(tokens[instrShift+1])).length()>16 && Integer.toBinaryString(Integer.parseInt(tokens[instrShift+1])).substring(0,16).equals("1111111111111111"))
                           line= Integer.toBinaryString(Integer.parseInt(tokens[instrShift+1])).substring(16);
                        
                        else
                           line=String.format("%16s", Integer.toBinaryString(Integer.parseInt(tokens[instrShift+1]))  ).replace(' ', '0') ;
                        
                    }
                    catch(Exception e)
                    {
                        errors.add("error at line " + (i+1) + " -- can't convert specified number");
                        continue;
                    }
                }
                else if (instr.equals("HEX"))
                {
                    try
                    {
                       line=String.format("%16s",  Integer.toBinaryString(Integer.parseInt(tokens[instrShift+1],16))).replace(' ', '0') ; 
                    }
                    catch(Exception e)
                    {
                        errors.add("error at line " + (i+1) + " -- can't convert specified number");
                        continue;
                    }
                }
            }
           // MRI instruction
           else if (MasterTables.mriTable.containsKey(instr))
           {
              // if no address specified
               if(tokens.length<instrShift+2)
               {
                   errors.add("error at line " + (i+1) + " -- MRI instruction should have an address");
                   continue;
               } 
               
             // get the address
             address = tokens[instrShift+1];
             if(!MasterTables.addressTable.containsKey(address))
             {
                 errors.add("error at line " + (i+1) + " -- the specified address is not valid");
                 continue;
             }
             
             // get I if exists
             if (tokens.length>(instrShift+2) && tokens[instrShift+2].equals("I"))
                 I = "1";
             
               line =  I+MasterTables.mriTable.get(instr)+String.format("%12s", MasterTables.addressTable.get(address)).replace(' ', '0');
              
           }
            // non-MRI instruction
            else if (MasterTables.nonMriTable.containsKey(instr))
            {
               // if no address specified
                if(tokens.length>instrShift+1)
                {
                    errors.add("error at line " + (i+1) + " -- non-MRI instruction can not have an address");
                    continue;
                } 
                
              
                line =  I+MasterTables.nonMriTable.get(instr); 
            }
            else
            {
                errors.add("error at line " + (i+1) + " -- invalid instruction");
                continue;
            }
                
            // finally replace old line with translated one
                lines.set(i-removedLines,Integer.toBinaryString(lc) + " >> " + line.trim());
            lc++;
            
        }
    }
    
    private static int getLC(int i)
    {
      try
      {
        String line = lines.get(i);
        String[] tokens = line.trim().split("\\s+");
        if(tokens[0].equals("ORG"))
        { 
            return Integer.parseInt(tokens[1], 16);
        }
        }
        catch(Exception e)
        {
            e.printStackTrace(); 
        }
        return -1;
    }
    
    private static boolean validLineSyntax(String line, int i)
    {
        // initialize searhc Reg Exp
        String labelReg = "^(\\D)(.*)(.*),";
        Pattern labelPattern = Pattern.compile(labelReg);
        char errFound = 'N';
        
        String[] tokens = line.split("\\s+");
        // collect all problems of first pass
        if (tokens.length > 4)
        {
            errors.add("error at line " + (i+1) + " -- line has too many instructions");
            errFound = 'Y';
        }
        //right Label 
        if(tokens[0].endsWith(",") && !labelPattern.matcher(tokens[0]).matches())
        {
            errors.add("error at line " + (i+1) + " --  label must start with character"); 
            errFound = 'Y';
        }
        // Label with END or ORG
         if(tokens[0].endsWith(",") && (tokens[1].equals("ORG")||tokens[1].equals("END")))
        {
            errors.add("error at line " + (i+1) + " -- can't use label with END or ORG"); 
            errFound = 'Y';
        }
        if(tokens[0].equals("END") && tokens.length>1 )
        { 
            errors.add("error at line " + (i+1) + " -- bad END line");
            errFound = 'Y';
        } 
        if(tokens[0].equals("ORG") && tokens.length>2 )
        { 
            errors.add("error at line " + (i+1) + " -- bad ORG line");
            errFound = 'Y';
        } 
        
        // return 
        if(errFound=='Y')
            return false;
        else
            return true;
    }
    public static void runAssembler(String code)
    {
        MasterTables.initTables();
        lines.clear();
        errors.clear();
        firstPass(code);
        if(errors.size()==0)
            // start second pass
            secondPass();
    }
    public static void main (String[] args)
    {
        String code = "ORG 100 //Iam here to comment \n" + "LDA SUB \n"  + "CMA \n"+ "INC // this is my app \n"+ "ADD MIN // adde \n"+ "STA DIF \n" + "HLT \n" + "MIN, DEC 83 \n" + "SUB, DEC -23 \n"  + "DIF, HEX 0 \n"  + "END";
        runAssembler(code);
        
        if(errors.size()>0 )
            System.out.println("errors are " + errors);
        else
        {
        System.out.println("*** program is ****");
        System.out.println(lines);
        }
    }
    
}
