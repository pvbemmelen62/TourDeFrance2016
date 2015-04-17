package nl.xs4all.pvbemmel.sudoku.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class SudokuSizeDialog extends JDialog {

  boolean isCancelled = false;
  int sudokuSize;
  
  public SudokuSizeDialog(Window owner) {
    super(owner, "Choose sudoku size", ModalityType.DOCUMENT_MODAL);
    
    sudokuSize = 9;
    Container cp = getContentPane();
    //
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new GridBagLayout());
    //
    JPanel panel;
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JRadioButton _9button = new JRadioButton(new SizeAction(9));
    _9button.setMnemonic('9');
    JRadioButton _16button = new JRadioButton(new SizeAction(16));
    _16button.setMnemonic('1');
    panel.add(_9button);
    panel.add(_16button);
    ButtonGroup bg = new ButtonGroup();
    bg.add(_9button);
    bg.add(_16button);
    _9button.setSelected(true);
    
    centerPanel.add(panel);
    cp.add(centerPanel, BorderLayout.CENTER);
    //
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    JButton okButton = new JButton(new OKAction("OK"));
    okButton.setMnemonic('o');
    JButton cancelButton = new JButton(new CancelAction("Cancel"));
    cancelButton.setMnemonic('c');
    panel.add(Box.createHorizontalGlue());
    panel.add(okButton);
    panel.add(cancelButton);
    panel.add(Box.createHorizontalGlue());
    cp.add(panel, BorderLayout.SOUTH);

    getRootPane().setDefaultButton(okButton);
    
    pack();
    setSize(300, 200);
  }
  public boolean isCancelled() {
    return isCancelled;
  }
  public int getSudokuSize() {
    return sudokuSize;
  }
  class SizeAction extends AbstractAction {
    int size;
    public SizeAction(int size) {
      super(""+size);
      this.size = size;
    }
    public void actionPerformed(ActionEvent e) {
      sudokuSize = size;
    }
  }
  class OKAction extends AbstractAction {
    OKAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      isCancelled = false;
      setVisible(false);
    }
  }
  class CancelAction extends AbstractAction {
    CancelAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      isCancelled = true;
      setVisible(false);
    }
  }
}