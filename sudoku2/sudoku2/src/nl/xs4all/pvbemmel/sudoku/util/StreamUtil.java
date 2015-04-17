package nl.xs4all.pvbemmel.sudoku.util;

import java.io.*;
import java.nio.charset.*;

public class StreamUtil {

  public static String getAsString(java.io.InputStream is) {
    
// From
//   http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
// by Pavel Repin:
//    I learned this trick from "Stupid Scanner tricks" article. The reason it
//    works is because Scanner iterates over tokens in the stream, and in this
//    case we separate tokens using "beginning of the input boundary" (\A) thus
//    giving us only one token for the entire contents of the stream.
//
//    Note, if you need to be specific about the input stream's encoding, you
//    can provide the second argument to Scanner constructor that indicates
//    what charset to use (e.g. "UTF-8").
    
    try (
      java.util.Scanner s = new java.util.Scanner(is)
    ) {
      s.useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
    }
  }
  
  public static InputStream getAsInputStream(String s) {
    InputStream stream = new ByteArrayInputStream(s.getBytes(
        StandardCharsets.UTF_8));  
    return stream;
  }
}
