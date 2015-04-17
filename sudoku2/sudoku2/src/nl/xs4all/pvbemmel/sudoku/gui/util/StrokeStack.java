package nl.xs4all.pvbemmel.sudoku.gui.util;

import java.awt.*;
import java.util.*;

public class StrokeStack {
  private Stack<Stroke> strokes;
  Graphics2D g;
  
  public StrokeStack(Graphics2D g) {
    this.g = g;
    strokes = new Stack<Stroke>();
  }
  /** Stores stroke on stack and makes it current. */
  public void pushStroke(Stroke s) {
    strokes.push(s);
    g.setStroke(s);
  }
  /** Takes top stroke from stack and makes it current, and removes it from
   *  stack. */
  public void popStroke() {
    g.setStroke(strokes.peek());
    strokes.pop();
  }

}
