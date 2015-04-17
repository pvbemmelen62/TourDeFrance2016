package nl.xs4all.pvbemmel.sudoku.gui.test;

import javax.swing.*;

import nl.xs4all.pvbemmel.sudoku.gui.*;

public class TestDelaySlider {

  public static void main(String[] args) {
    Runnable runnable = new Runnable() {
      public void run() {
        int xs[] =    { 0,   15,  30,    300 };
        double ys[] = { 0.0, 0.0, 0.001, 10.0 };
        DelaySlider slider = new DelaySlider();
        for(int i=0; i<4; ++i) {
          slider.setValue(xs[i]);
          double delay = slider.getDelay();
          System.out.println(
            String.format("%d  ->  %g    (expecting %g)", xs[i], delay, ys[i])
          );
        }
      }
    };
    SwingUtilities.invokeLater(runnable);
  }
}
