package nl.xs4all.pvbemmel.sudoku.gui.util;

import java.awt.*;

public class TextUtil {

  /**
   * Center one or two lines of text in a rectangle.
   * @param s1 first line of text
   * @param s2 second line of text; may be null.
   * @param g  Graphics object.
   * @param metrics FontMetrics object.
   * @param x x coordinate of upperleft of rectangle.
   * @param y y coordinate of upperleft of rectangle.
   * @param w width of rectangle.
   * @param h height of rectangle.
   * @see /JavaExamples3/src/je3/graphics/GraphicsSampler.java
   */
  public static void centerText(String s1, String s2, Graphics g,
      FontMetrics metrics, int x, int y, int w, int h) {
    int height = metrics.getHeight();
    int ascent = metrics.getAscent();
    int width1 = 0, width2 = 0, x0 = 0, x1 = 0, y0 = 0, y1 = 0;
    width1 = metrics.stringWidth(s1);
    if (s2 != null)
      width2 = metrics.stringWidth(s2);
    x0 = x + (w - width1) / 2;
    x1 = x + (w - width2) / 2;
    if (s2 == null) {
      y0 = y + (h - height) / 2 + ascent;
    }
    else {
      y0 = y + (h - (int) (height * 2.2)) / 2 + ascent;
      y1 = y0 + (int) (height * 1.2);
    }
    g.drawString(s1, x0, y0);
    if (s2 != null)
      g.drawString(s2, x1, y1);
  }  
}
