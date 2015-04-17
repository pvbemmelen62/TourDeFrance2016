package nl.xs4all.pvbemmel.sudoku.gui.util;

import java.awt.event.*;

import javax.swing.*;

public class MySwingUtilities {
  private static ActionListener listener;
  private static Timer timer;

  static {
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      }
    };
    timer = new Timer(1, listener);
    timer.setRepeats(false);
  }
  public static synchronized void invokeLater(int delay, Runnable runnable) {
    timer.stop();
    if(delay==0) {
      SwingUtilities.invokeLater(runnable);
      return;
    }
    final Runnable _runnable = runnable;
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _runnable.run();
      }
    };
    timer = new Timer(delay, listener);
    timer.setRepeats(false);
    timer.start();
  }
  public static synchronized void cancel() {
    timer.stop();
  }
}
