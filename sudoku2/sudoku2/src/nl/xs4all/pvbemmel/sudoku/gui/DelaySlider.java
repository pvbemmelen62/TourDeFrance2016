package nl.xs4all.pvbemmel.sudoku.gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import nl.xs4all.pvbemmel.sudoku.util.*;

@SuppressWarnings("serial")
public class DelaySlider extends JSlider {
  private LinearFunction valueToLog;
  private LinearFunction logToValue;
  
  public DelaySlider() {
    super(0, 300, 0);
    setMaximumSize(new Dimension(400, 1000));
    valueToLog = new LinearFunction(1, 1);
    valueToLog.setConstraints(new Double[]{ 30.0,  300.0 },
        new Double[] { -3.0, 1.0 });
    logToValue = valueToLog.getInverse();
    Hashtable<Integer,JComponent> labelTable =
      new Hashtable<Integer,JComponent>();
    labelTable.put(new Integer(0), new JLabel("0"));
    int[] logs = new int[] { -3, -2, -1, 0, 1 };
    String[] labels = new String[] { "0.001", "0.01", "0.1", "1", "10" };
    for(int i=0; i<logs.length; ++i) {
      double v = logToValue.getValue(logs[i]);
      labelTable.put((int)v, new JLabel(labels[i]));
    }
    setLabelTable(labelTable);
    setPaintLabels(true);
  }
  /**
   * Get delay in seconds.
   */
  public double getDelay() {
    double delay = valueToDelay(getValue());
    return delay;
  }
  /**
   * Set delay in seconds.
   */
  public void setDelay(double delay) {
    int value = delayToValue(delay);
    setValue(value);
  }
  private double valueToDelay(int value) {
    if(value<30) {
      return 0.0;
    }
    double logValue = valueToLog.getValue(value);
    double delay = Math.pow(10.0, logValue);
    return delay;
  }
  public int delayToValue(double delay) {
    if(delay<=0.0) {
      return 0;
    }
    double logValue = Math.log10(delay);
    double d = logToValue.getValue(logValue);
    int value = (int)d;
    return value;
  }
  static class Test {
    public static void main(String[] args) {
      DelaySlider slider = new DelaySlider();
      for(int i=0; i<300; ++i) {
        double delay = slider.valueToDelay(i);
        int value = slider.delayToValue(delay);
        String s = String.format("%10d  -->  %10g  -->  %10d", i, delay, value);
        System.out.println(s);
      }
    }
  }
}