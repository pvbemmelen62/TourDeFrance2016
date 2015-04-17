package nl.xs4all.pvbemmel.sudoku.gui.test;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import nl.xs4all.pvbemmel.sudoku.gui.*;


public class TestAboutDialog {
  public static void main(String[] args) {
    TestAboutDialog test = new TestAboutDialog();
    SwingUtilities.invokeLater(test.new GUIRunnable());
  }
  static Logger getLogger() {
    return Logger.getLogger(TestAboutDialog.class.getName());
  }
  class GUIRunnable implements Runnable {
    public void run() {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e) {
      }
      URL imgUrl = getClass().getResource("/images/logo-50-percent.png");
      getLogger().fine("imgUrl: " + imgUrl);
      String imgSrc = imgUrl.toString();
      Font font = UIManager.getFont("Label.font");
      getLogger().fine("font: " + font);
      String fontFamily = font.getFamily();
      String fontSize = ""+font.getSize()+"pt";
      Map<String,String> map = new HashMap<String,String>();
      map.put("fontFamilyPlaceHolder", fontFamily);
      map.put("fontSizePlaceHolder", fontSize);
      map.put("imgSrcPlaceHolder", imgSrc);
      map.put("versionPlaceHolder", "1.0.0");
      map.put("emailPlaceHolder", "pvbemmelen62@gmail.com");
      AboutDialog dialog = new AboutDialog(map);
      dialog.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
      dialog.setSize(500, 600);
      dialog.setLocationRelativeTo(null);
      dialog.setVisible(true);
      System.exit(0);
    }
  }
}