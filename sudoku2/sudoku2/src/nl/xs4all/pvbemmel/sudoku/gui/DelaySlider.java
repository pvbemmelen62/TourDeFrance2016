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
  public double getDelay() {
    int value = getValue();
    if(value<30) {
      return 0.0;
    }
    double logValue = valueToLog.getValue(value);
    double delay = Math.pow(10.0, logValue);
    return delay;
  }
}