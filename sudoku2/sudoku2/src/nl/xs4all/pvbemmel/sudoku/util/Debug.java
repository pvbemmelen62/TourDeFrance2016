package nl.xs4all.pvbemmel.sudoku.util;

import java.text.*;
import java.util.*;
import static java.lang.System.out;

/**
 * Print methods equivalent to those from System.out .
 * <p>
 * Use these methods when during development you want to temporarily add
 * print statements. This allows you to easily find the temporary statements
 * when you want to remove them again.
 */
public class Debug {

  private static SimpleDateFormat simpleDateFormat;
  static {
    simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
  }
  
  public static void print(String s) {
    out.print(s);
    out.flush();
  }
  public static void println(String s) {
    out.println(s);
    out.flush();
  }
  public static void printTime(String s) {
    out.print(time() + " " + s);
    out.flush();
  }
  public static void printlnTime(String s) {
    out.println(time() + " " + s);
    out.flush();
  }
  public static String time() {
    return simpleDateFormat.format(new Date());
  }
  public static void main(String[] args) {
    out.println(time());
    out.flush();
  }
}
