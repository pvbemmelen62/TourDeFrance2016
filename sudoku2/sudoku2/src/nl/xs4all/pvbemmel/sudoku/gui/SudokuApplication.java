package nl.xs4all.pvbemmel.sudoku.gui;

import static java.awt.event.KeyEvent.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;

import nl.xs4all.pvbemmel.sudoku.*;

@SuppressWarnings("serial")
public class SudokuApplication {
  private JFrame frame;
  private String[] args;
  private Sudoku sudoku;
  private SudokuPanel sudokuPanel;
  private JButton startStopButton;
  private boolean isRunning;
  private JLabel stepCountLabel;
  private boolean refreshCells;
  private boolean refreshCount;

  private static Logger getLogger() {
    String cname = SudokuApplication.class.getName();
    //System.out.println(cname);
    return Logger.getLogger(cname);
  }
  
  public static void main(String[] args) {
    SudokuApplication app = new SudokuApplication(args);
    SwingUtilities.invokeLater(app.new GUIRunnable());
  }
  private SudokuApplication(String[] args) {
    this.args = args;
  }
  private class GUIRunnable implements Runnable {
    public void run() {
      refreshCells = true;
      refreshCount = true;
      if(args.length==0) {
        sudoku = new Sudoku(9);
      }
      else {
        sudoku = new Sudoku(9);
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
          sudoku.read(br);
        }
        catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }
      isRunning = false;
      frame = new JFrame("SudokuApplication");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setJMenuBar(new MyMenuBar());

      URL imgUrl = getClass().getResource("/images/logo-25-percent.png");
      ImageIcon imgIcon = new ImageIcon(imgUrl);
      Image image = imgIcon.getImage();
      frame.setIconImage(image);
      
      Container cp = frame.getContentPane();
      sudokuPanel = new SudokuPanel();
      sudokuPanel.setSudoku(sudoku);
      sudoku.addSudokuListener(new MySudokuListener());
      cp.add(sudokuPanel, BorderLayout.CENTER);
      JPanel panel = new JPanel();
      BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
      panel.setLayout(bl);
      StatePanel statePanel = new StatePanel();
      ControlPanel controlPanel = new ControlPanel();
      panel.add(statePanel);
      panel.add(controlPanel);
      cp.add(panel, BorderLayout.SOUTH);
      frame.addComponentListener(new ComponentAdapter() {
        public void componentShown(ComponentEvent e) {
          if(sudoku.getStepCount()==0) {
            sudokuPanel.setEditable(true);
            sudokuPanel.repaint();
            sudokuPanel.requestFocusInWindow();
          }
        }
      });
      frame.pack();
      frame.setSize(800, 600);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    }
  }
  private class MyMenuBar extends JMenuBar {
    MyMenuBar() {
      add(new FileMenu("File")).setMnemonic(VK_F);
      add(new HelpMenu("Help")).setMnemonic(VK_H);
    }
  }
  private class FileMenu extends JMenu {
    FileMenu(String label) {
      super(label);
//    JMenuItem item = add(new ClearAllAction("Clear all"));
      JMenuItem item = add(new NewAction("New..."));
      item.setMnemonic(VK_N);
      item.setToolTipText("Create new sudoku");
      add(new LoadAction("Open...")).setMnemonic(VK_O);
//      add(new SaveAction("Save")).setMnemonic(VK_S);
      add(new SaveAsAction("Save As...")).setMnemonic(VK_A);
      add(new ExitAction("Exit")).setMnemonic(VK_X);
    }
  }
  private class HelpMenu extends JMenu {
    HelpMenu(String label) {
      super(label);
      add(new UsageAction("Usage...")).setMnemonic(VK_U);
      add(new AboutAction("About...")).setMnemonic(VK_A);
    }
  }
  private class UsageAction extends AbstractAction {
    UsageAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      Font font = UIManager.getFont("Label.font");
      getLogger().fine("font: " + font);
      String fontFamily = font.getFamily();
      String fontSize = ""+font.getSize()+"pt";
      Map<String,String> map = new HashMap<String,String>();
      map.put("fontFamilyPlaceHolder", fontFamily);
      map.put("fontSizePlaceHolder", fontSize);
      map.put("versionPlaceHolder", "1.0.0");
      UsageDialog dialog = new UsageDialog(map);
      dialog.setSize(600,800);
      dialog.setLocationRelativeTo(null);
      dialog.setVisible(true);
    }
  }
  
  private class LoadAction extends AbstractAction {
    LoadAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      String dfltDir = System.getProperty("user.dir", "C:\\");
      String dirName = Preferences.userRoot().get("ioDir", dfltDir);
      JFileChooser chooser = new JFileChooser(dirName);
      int rv = chooser.showOpenDialog(frame);
      switch(rv) {
      case JFileChooser.APPROVE_OPTION: break;
      case JFileChooser.CANCEL_OPTION: return;
      case JFileChooser.ERROR_OPTION: return;
      }
      File file = chooser.getSelectedFile();
      File dir = file.getParentFile();
      if(dir!=null) {
        Preferences.userRoot().put("ioDir", dir.getAbsolutePath());
      }
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        sudoku.read(br);
      }
      catch (IOException e1) {
        String msg = "" + e1;
        String title = "IO Error";
        JOptionPane.showMessageDialog(frame, msg, title,
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      sudokuPanel.setResized(true);
      sudokuPanel.setEditable(sudoku.getStepCount()==0);
      sudokuPanel.repaint();
      if(sudoku.getStepCount()==0) {
        sudokuPanel.requestFocusInWindow();
      }
      stepCountLabel.setText(""+sudoku.getStepCount());
    }
  }
  private class AboutAction extends AbstractAction {
    AboutAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
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
      map.put("emailPlaceHolder", "pvbemmelen62@gmail.com");
      map.put("versionPlaceHolder", "1.0.1");
      AboutDialog dialog = new AboutDialog(map);
      dialog.setSize(400,500);
      dialog.setLocationRelativeTo(null);
      dialog.setVisible(true);
    }
  }
  private class SaveAsAction extends AbstractAction {
    SaveAsAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      String dfltDir = System.getProperty("user.dir", "C:\\");
      String dir = Preferences.userRoot().get("ioDir", dfltDir);
      JFileChooser chooser = new JFileChooser(dir);
      int rv = chooser.showSaveDialog(frame);
      switch(rv) {
      case JFileChooser.APPROVE_OPTION: break;
      case JFileChooser.CANCEL_OPTION: return;
      case JFileChooser.ERROR_OPTION: return;
      }
      File file = chooser.getSelectedFile();
      Preferences.userRoot().put("ioDir", file.getParentFile().
        getAbsolutePath());
      try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
        //boolean drawDashes = false;
        //boolean drawZeros = false;
        //sudoku.printValues(w, drawDashes, drawZeros);
        //sudoku.printCellValues(w);
        sudoku.write(w);
      }
      catch (IOException e1) {
        String msg = ""+e1;
        String title = "IO Error";
        JOptionPane.showMessageDialog(frame, msg, title,
          JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
  }
  private class NewAction extends AbstractAction {
    NewAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      SudokuSizeDialog ssd = new SudokuSizeDialog(frame);
      ssd.setLocationRelativeTo(null);
      ssd.setVisible(true);
      if(ssd.isCancelled()) {
        return;
      }
      int size;
      size = ssd.getSudokuSize();
      sudoku.resetValues(size);
      sudokuPanel.setResized(true);
      sudokuPanel.setEditable(true);
      sudokuPanel.repaint();
      stepCountLabel.setText("0");
      sudokuPanel.requestFocusInWindow();
    }
  }
  private class ExitAction extends AbstractAction {
    ExitAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      System.exit(0);
    }
  }
  private class MySudokuListener implements SudokuListener {
    public void rowColChanged(Sudoku s, RowCol rcBefore, RowCol rcAfter) {
      if(refreshCount) {
        stepCountLabel.setText(""+s.getStepCount());
      }
      if(refreshCells) {
        sudokuPanel.repaintCell(rcBefore);
        sudokuPanel.repaintCell(rcAfter);
      }
    }
    public void cellChanged(Sudoku s, RowCol rc, int before, int after) {
      if(refreshCount) {
        stepCountLabel.setText(""+s.getStepCount());
      }
      if(refreshCells) {
        sudokuPanel.repaintCell(rc);
      }
    }
    public void atEnd(Sudoku s) {
      setIsRunning(false);
    }
    public void solutionFound(Sudoku s) {
      setIsRunning(false);
    }
  }
  private class StatePanel extends JPanel {
    StatePanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      JPanel panel;
      panel = new JPanel();
      panel.setLayout(new FlowLayout());
      panel.add(new JLabel("Step count:"));
      stepCountLabel = new JLabel("0");
      panel.add(stepCountLabel);
      add(panel);
    }
  }
  private class ControlPanel extends JPanel {
    ControlPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      JPanel panel;
      JCheckBox cb;
      panel = new JPanel();
      panel.setLayout(new FlowLayout());
      panel.add(new JLabel("Refresh count:"));
      cb = new JCheckBox(new ToggleRefreshCountAction());
      cb.setSelected(true);
      panel.add(cb);
      add(panel);
      panel = new JPanel();
      panel.setLayout(new FlowLayout());
      panel.add(new JLabel("Refresh cells:"));
      cb = new JCheckBox(new ToggleRefreshCellAction());
      cb.setSelected(true);
      panel.add(cb);
      add(panel);
      add(Box.createVerticalStrut(10));
      panel = new JPanel();
      panel.setLayout(new FlowLayout());
      panel.add(Box.createHorizontalGlue());
      panel.add(new JLabel("Delay (sec)"));
      panel.add(Box.createHorizontalGlue());
      add(panel);
      DelaySlider slider = new DelaySlider();
      slider.addChangeListener(new DelayChangeListener());
      add(slider);
      panel = new JPanel();
      startStopButton = new JButton(new StartStopAction("Start")); 
      panel.add(startStopButton);
      panel.add(new JButton(new StepAction("Step")));
      add(panel);
    }
  }
  private class DelayChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      DelaySlider slider = (DelaySlider)e.getSource();
      double delayS = slider.getDelay(); // seconds
      int delayMs;
      if(delayS<0.0001) {
        delayMs = 0;
      }
      else {
        delayMs = (int)(1000*delayS); // ms;
      }
      sudoku.setDelay(delayMs);
    }
  }
  private class ToggleRefreshCountAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      refreshCount = ((JCheckBox)e.getSource()).isSelected();
      if(refreshCount) {
        stepCountLabel.setText(""+sudoku.getStepCount());
      }
    }
  }
  private class ToggleRefreshCellAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      refreshCells = ((JCheckBox)e.getSource()).isSelected();
      if(refreshCells) {
        // Was false, so repaints of cells have been missed; therefore:
        sudokuPanel.repaint();
      }
    }
  }
  private class StartStopAction extends AbstractAction {
    StartStopAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      sudokuPanel.setEditable(false);
      if(!isRunning) {
        if(sudoku.isAtEnd()) {
          JOptionPane.showMessageDialog(frame, "At end.");
          return;
        }
      }
      toggleIsRunning();
      sudoku.toggleIsRunning();
    }
  }
  void setIsRunning(boolean isRunning) {
    if(this.isRunning == isRunning) {
      return;
    }
    toggleIsRunning();
  }
  private void toggleIsRunning() {
    isRunning = !isRunning;
    startStopButton.setText(isRunning ? "Stop" : "Start");
  }
  private class StepAction extends AbstractAction {
    StepAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      sudokuPanel.setEditable(false);
      if(sudoku.isAtEnd()) {
        JOptionPane.showMessageDialog(frame, "At end.");
        return;
      }
      if(isRunning) {
        toggleIsRunning();
        sudoku.toggleIsRunning();
      }
      else {
        sudoku.step();
      }
    }
  }

}
