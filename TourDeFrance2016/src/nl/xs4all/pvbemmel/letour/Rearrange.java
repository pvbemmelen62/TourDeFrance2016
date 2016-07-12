package nl.xs4all.pvbemmel.letour;


import java.util.*;
import java.io.*;
import java.util.regex.*;

/**
 * Rearrange text from:
 * <pre>
 *   NAVARRO D.    139 COF
 *   COSTA R.    124 MOV
 *   VELITS P.   159 OPQ
 * to
 *   139   NAVARRO D.    COF
 *   124   COSTA R.      MOV
 *   159   VELITS P.     OPQ
</pre>
*/
public class Rearrange {

  public static void main(String[] args) throws Exception {

    String fileName = args[0];

    Map<Integer,List<String>> rowMap = new TreeMap<Integer,List<String>>();

    BufferedReader br = new BufferedReader(new FileReader(fileName));

    Matcher matcher = Pattern.compile(
     "([^\\t]+)\\t+([^\\t]+)\\t+([^\\t]+)\\s*").matcher("");

    while(true) {
      String line = br.readLine();
      if(line==null) {
        break;
      }
      line = line.trim();

      if("".equals(line)) {
        continue;
      }
      matcher.reset(line);
      if(!matcher.matches()) {
        continue;
      }
      String name = matcher.group(1);
      String num = matcher.group(2);
      String team = matcher.group(3);
      
      rowMap.put(Integer.parseInt(num), Arrays.asList(num, name, team));
    }
    for(Integer num : rowMap.keySet()) {
      List<String> row = rowMap.get(num);
      // String num = row.get(0);
      String name = row.get(1);
      String team =row.get(2);
      String s = String.format("%6d%30s%10s", num, name, team);
      System.out.println(s);
    }
  }
}