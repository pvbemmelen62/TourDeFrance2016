package nl.xs4all.pvbemmel.sudoku.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import static java.awt.event.KeyEvent.*;

import javax.swing.*;

import nl.xs4all.pvbemmel.sudoku.*;
import nl.xs4all.pvbemmel.sudoku.gui.util.*;

@SuppressWarnings("serial")
public class SudokuPanel extends JPanel {
  private Sudoku sudoku;
  private Font fontPlain;
  private Font fontBold;
  private FontMetrics metricsPlain;
  private FontMetrics metricsBold;
  /** Transforms cell (c,r) to pixel (x,y) . */
  private AffineTransform c2p;
  /** Transforms pixel (x,y) to cell (c,r). */
  private AffineTransform p2c;
  private BasicStroke strokeSudoku;
  private BasicStroke strokeSubrect;
  private BasicStroke strokeCell;
  private BasicStroke strokeNext;
  private BasicStroke strokeEdit;
  private boolean isResized;
  private int editRow;
  private int editCol;
  private boolean isEditable;
  
  public SudokuPanel() {
    isResized = true;
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        isResized = true;
      }
    });
    addFocusListener(new FocusAdapter() {
      private void repaintCursorIfNeeded(FocusEvent e) {
        if(isEditable) {
          int r = editRow;
          int c = editCol;
          boolean valid = 0<=r && r<sudoku.getSize()
              && 0<=c && c<sudoku.getSize();
          if(!valid) {
            return;
          }
          repaintCell(r,c);
        }
      }
      public void focusGained(FocusEvent e) {
        repaintCursorIfNeeded(e);
      }
      public void focusLost(FocusEvent e) {
        repaintCursorIfNeeded(e);
      }
    });
    editRow = 0;
    editCol = 0;
    isEditable = false;
    ActionMap acMap = getActionMap();
    acMap.put("MoveLeft", new MoveLeftAction());
    acMap.put("MoveRight", new MoveRightAction());
    acMap.put("MoveUp", new MoveUpAction());
    acMap.put("MoveDown", new MoveDownAction());
    acMap.put("Backspace", new BackspaceAction());
    for(int i=0; i<10; ++i) {
      acMap.put("digit"+i, new DigitAction(i));
    }
    InputMap inMap = getInputMap();
    inMap.put(KeyStroke.getKeyStroke(VK_LEFT, 0), "MoveLeft");
    inMap.put(KeyStroke.getKeyStroke(VK_RIGHT, 0), "MoveRight");
    inMap.put(KeyStroke.getKeyStroke(VK_UP, 0), "MoveUp");
    inMap.put(KeyStroke.getKeyStroke(VK_DOWN, 0), "MoveDown");
    inMap.put(KeyStroke.getKeyStroke(VK_BACK_SPACE, 0), "Backspace");
    for(int i=0; i<10; ++i) {
      inMap.put(KeyStroke.getKeyStroke("typed "+i), "digit"+i);
    }
    addMouseListener(new MyMouseListener());
  }
  class MyMouseListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      if(!isEditable) {
        return;
      }
      updateIfResized();
      Point p = e.getPoint();
      Point2D pc = p2c.transform(p, null);
      double r = pc.getY();
      double c = pc.getX();
      boolean valid = 0<=r && r<sudoku.getSize();
      valid = valid && 0<=c && c<sudoku.getSize();
      if(valid) {
        int rOld = editRow;
        int cOld = editCol;
        editRow = (int)r;
        editCol = (int)c;
        repaintCell(rOld, cOld);
        repaintCell(editRow, editCol);
      }
      requestFocusInWindow();
    }
    
  }
  public void setSudoku(Sudoku sudoku) {
    this.sudoku = sudoku;
  }
  public boolean isEditable() {
    return isEditable;
  }
  public void setEditable(boolean isEditable) {
    if(this.isEditable==isEditable) {
      return;
    }
    this.isEditable = isEditable;
    repaintCell(editRow, editCol);
  }
  
  public void setResized(boolean isResized) {
    this.isResized = isResized;
  }
  public int getEditRow() {
    return editRow;
  }
  public int getEditCol() {
    return editCol;
  }
  protected void updateIfResized() {
    if(!isResized) {
      return;
    }
    isResized = false;
    int h = getHeight();
    int w = getWidth();
    int sudSize = sudoku.getSize();
    int numPixels = Math.min(w, h);
    double scale = numPixels/533.0; // : determined experimentally.
    int margin = (int)(10 * scale);
    double cellSize = (numPixels-margin)/sudSize;
    double xOffset = (w - cellSize*sudSize)/2;
    double yOffset = (h - cellSize*sudSize)/2;
    
//  [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
//  [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
//  [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
    
//  [ x']   [  cS    0   xOfs ] [ x ]   [ cS*x        + xOfs]
//  [ y'] = [   0   cS   yOfs ] [ y ] = [        cs*y + yOfs]
//  [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]

    //  { m00 m10 m01 m11 [m02 m12]}.
    //  { cS   0   0  cS  xOfs yOfs }
    c2p = new AffineTransform(new double[] {
        cellSize, 0, 0, cellSize, xOffset, yOffset });
    try {
      p2c = c2p.createInverse();
    }
    catch (NoninvertibleTransformException e) {
      e.printStackTrace();
    }
    //
    double dpi = 96;
    double fontSizeInch = cellSize/dpi;
    double pointsPerInch = 72;
    // Font sizes factors and stroke widths were determined by trial and error.
    int fontSize = (int)(pointsPerInch * fontSizeInch * 0.8);
    fontPlain = new Font("SansSerif", Font.PLAIN, fontSize);
    fontBold = new Font("SansSerif", Font.BOLD, (int)(fontSize*1.2));
    metricsPlain = this.getFontMetrics(fontPlain);
    metricsBold = this.getFontMetrics(fontBold);
    strokeSudoku = new BasicStroke((float)(4.0*scale));
    strokeSubrect = new BasicStroke((float)(4.0*scale));
    strokeCell = new BasicStroke((float)(2.0*scale));
    strokeNext = new BasicStroke((float)(4.0*scale));
    strokeEdit = new BasicStroke((float)(4.0*scale));
  }
  protected void paintComponent(Graphics g) {
    int h = getHeight();
    int w = getWidth();
    int sudSize = sudoku.getSize();
    updateIfResized();
    //Graphics2D g2 = (Graphics2D)getGraphics().create();
    Graphics2D g2 = (Graphics2D)g;
    g2.setColor(Color.WHITE);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2.fillRect(0,0,w,h);
    g2.setColor(Color.BLACK);
    g2.setFont(fontPlain);

    // Using g2.getClipBounds() to calculate sharper bounds for r and c
    // only speeds up drawing by approximately factor 2.
    // Don't bother.
    int subRectSize = (int) Math.round(Math.sqrt(sudSize));
    for(int r=0; r<=sudSize; ++r) {
      Point2D p0 = c2p.transform(new Point(0,r), null);
      Point2D p1 = c2p.transform(new Point(sudSize,r), null);
      if(r==0 || r==sudSize) {
        g2.setStroke(strokeSudoku);
      }
      else if(r%subRectSize == 0) {
        g2.setStroke(strokeSubrect);
      }
      else {
        g2.setStroke(strokeCell);
      }
      g2.drawLine((int)p0.getX(), (int)p0.getY(), (int)p1.getX(),
          (int)p1.getY());
    }
    for(int c=0; c<=sudSize; ++c) {
      Point2D p0 = c2p.transform(new Point(c,0), null);
      Point2D p1 = c2p.transform(new Point(c,sudSize), null);
      if(c==0 || c==sudSize) {
        g2.setStroke(strokeSudoku);
      }
      else if(c%subRectSize == 0) {
        g2.setStroke(strokeSubrect);
      }
      else {
        g2.setStroke(strokeCell);
      }
      g2.drawLine((int)p0.getX(), (int)p0.getY(), (int)p1.getX(),
          (int)p1.getY());
    }
    for(int r=0; r<sudSize; ++r) {
      for(int c=0; c<sudSize; ++c) {
        Cell cell=null;
        cell = sudoku.getCell(r,c);
        if(cell.value!=0) {
          Point2D p0 = c2p.transform(new Point(c,r), null);
          Point2D d = c2p.deltaTransform(new Point(1,1), null);
          if(cell.isFixed) {
            g2.setFont(fontBold);
            TextUtil.centerText(""+cell.value, null, g2, metricsBold,
              (int)p0.getX(), (int)p0.getY(),
              (int)d.getX(), (int)d.getY());
          }
          else {
            g2.setFont(fontPlain);
            TextUtil.centerText(""+cell.value, null, g2, metricsPlain,
                (int)p0.getX(), (int)p0.getY(),
                (int)d.getX(), (int)d.getY());
          }
        }
        StrokeStack ss = new StrokeStack(g2);
        if(r==sudoku.getNextRow() && c==sudoku.getNextCol()) {
          Point2D p0 = c2p.transform(new Point(c,r), null);
          Point2D d = c2p.deltaTransform(new Point(1,1), null);
          g2.setColor(Color.RED);
          float lw = strokeSubrect.getLineWidth();
          double x0 = p0.getX();
          double y0 = p0.getY();
          double dx = d.getX();
          double dy = d.getY();
          ss.pushStroke(strokeNext);
          int[] xPoints = new int[] {
              (int)(x0+lw), (int)(x0+dx-lw), (int)(x0+dx-lw), (int)(x0+lw),
              (int)(x0+lw)
          };
          int[] yPoints = new int[] {
              (int)(y0+lw), (int)(y0+lw), (int)(y0+dy-lw), (int)(y0+dy-lw),
              (int)(y0+lw)
          };
          g2.drawPolyline(xPoints, yPoints, 5);
          ss.popStroke();
          g2.setColor(Color.BLACK);
        }
        if(isEditable && r==editRow && c==editCol && isFocusOwner()) {
          Point2D p0 = c2p.transform(new Point(c,r), null);
          Point2D d = c2p.deltaTransform(new Point(1,1), null);
          ss.pushStroke(strokeEdit);
          double x0 = p0.getX();
          double y0 = p0.getY();
          double dx = d.getX();
          double dy = d.getY();
          float lw = strokeEdit.getLineWidth();
          float sp = lw*2;
          g2.drawLine((int)(x0+dx-sp), (int)(y0+sp),
            (int)(x0+dx-sp), (int)(y0+dy-sp));
          ss.popStroke();
        }
      }
    }
  }
  public void repaintCell(RowCol rc) {
    repaintCell(rc.row, rc.col);
  }
  public void repaintCell(int row, int col) {
    updateIfResized();
    Point2D p = c2p.transform(new Point(col, row), null);
    Point2D d = c2p.deltaTransform(new Point(1,1), null);
    repaint((int)p.getX(), (int)p.getY(), (int)d.getX(), (int)d.getY());
  }
  class MoveLeftAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int r0 = editRow;
      int c0 = editCol;
      --editCol;
      if(editCol<0) {
        editCol = sudoku.getSize()-1;
        --editRow;
        if(editRow<0) {
          editRow = sudoku.getSize()-1;
        }
      }
      repaintCell(r0, c0);
      repaintCell(editRow, editCol);
    }
  }
  class MoveRightAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int r0 = editRow;
      int c0 = editCol;
      ++editCol;
      if(editCol==sudoku.getSize()) {
        editCol = 0;
        ++editRow;
        if(editRow==sudoku.getSize()) {
          editRow = 0;
        }
      }
      repaintCell(r0, c0);
      repaintCell(editRow, editCol);
    }
  }
  class MoveUpAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int r0 = editRow;
      int c0 = editCol;
      --editRow;
      if(editRow<0) {
        editRow = sudoku.getSize()-1;
        --editCol;
        if(editCol<0) {
          editCol = sudoku.getSize()-1;
        }
      }
      repaintCell(r0, c0);
      repaintCell(editRow, editCol);
    }
  }
  class MoveDownAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int r0 = editRow;
      int c0 = editCol;
      ++editRow;
      if(editRow==sudoku.getSize()) {
        editRow = 0;
        ++editCol;
        if(editCol==sudoku.getSize()) {
          editCol = 0;
        }
      }
      repaintCell(r0, c0);
      repaintCell(editRow, editCol);
    }
  }
  class BackspaceAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int before = sudoku.getValue(editRow, editCol);
      String s = Integer.toString(before);
      int after;
      if(s.length()>1) {
        after = Integer.parseInt(s.substring(0, s.length()-1));
      }
      else {
        after = 0;
      }
      boolean success = sudoku.setValue(editRow, editCol, after);
      if(!success) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }
  class DigitAction extends AbstractAction {
    int digit;
    DigitAction(int digit) {
      this.digit = digit;
    }
    public void actionPerformed(ActionEvent e) {
      int before = sudoku.getValue(editRow, editCol);
      String s = before==0 ? "" : ""+before;
      s = s + digit;
      int after = Integer.parseInt(s);
      if(after>sudoku.getSize()) {
        Toolkit.getDefaultToolkit().beep();
        return;
      }
      sudoku.setValue(editRow, editCol, after);
    }
    
  }
}
