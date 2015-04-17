package nl.xs4all.pvbemmel.sudoku;

import java.io.*;
import java.util.*;

import javax.swing.*;

import nl.xs4all.pvbemmel.sudoku.gui.util.*;

public class Sudoku {
  //-------------------------------------------------------------------------
  int size;
  int subSize;
  boolean[][] cols;       // size x (size+1)
  boolean[][] rows;       // size x (size+1)
  boolean[][][] subRects; // subSize x subSize x (size+1)
  Cell[][] cells;
  List<SudokuListener> listeners; // not thread safe: access from EDT only.
  boolean isRunning;
  int delay;
  
  int nextRow;
  int nextCol;
  private boolean backingUp; // required for dealing with fixed cells.
  private Long stepCount;
  
  public Sudoku(int size) {
    init(size);
    listeners = new ArrayList<SudokuListener>();
    delay = 0;
  }
  public void init(int size) {
    boolean valid = size==9 || size==16;
    if(!valid) {
      throw new IllegalArgumentException("Size must be 9 or 16" );
    }
    this.size = size;
    subSize = (int) Math.round(Math.sqrt(size));
    //------------------------------------
    subRects = new boolean[subSize][][];
    for(int r=0; r<subSize; ++r) {
      subRects[r] = new boolean[subSize][];
      for(int c=0; c<subSize; ++c) {
        subRects[r][c] = new boolean[size+1];
      }
    }
    //------------------------------------
    rows = new boolean[size][];
    for(int c=0; c<size; ++c) {
      rows[c] = new boolean[size+1];
    }
    //------------------------------------
    cols = new boolean[size][];
    for(int c=0; c<size; ++c) {
      cols[c] = new boolean[size+1];
    }
    //------------------------------------
    cells = new Cell[size][];
    for(int r=0; r<size; ++r) {
      cells[r] = new Cell[size];
    }
    for(int r=0; r<size; ++r) {
      for(int c=0; c<size; ++c) {
        Cell cell = new Cell();
        cells[r][c] = cell;
        cell.subRect = subRects[r/subSize][c/subSize];
        cell.row = rows[r];
        cell.col = cols[c];
      }
    }
    nextRow = 0;
    nextCol = 0;
    backingUp = false;
    stepCount = 0L;
    isRunning = false;
  }
  public void addSudokuListener(SudokuListener listener) {
    listeners.add(listener);
  }
  public int getSize() {
    return size;
  }
  public int getNextRow() {
    return nextRow;
  }
  public int getNextCol() {
    return nextCol;
  }
  public long getStepCount() {
    return stepCount;
  }
  public int getDelay() {
    return delay;
  }
  public void setDelay(int delay) {
    this.delay = delay;
  }
  /**
   * Sets cell.value and cell.isFixed values. Does not notify listeners.
   * @param cells
   */
  public void setCellValues(Cell[][] cells) {
    init(cells.length);
    for(int r=0; r<size; ++r) {
      for(int c=0; c<size; ++c) {
        Cell c0 = this.cells[r][c];
        Cell c1 = cells[r][c];
        c0.value = c1.value;
        c0.isFixed = c1.isFixed;
        c0.row[c0.value] = true;
        c0.col[c0.value] = true;
        c0.subRect[c0.value] = true;
      }
    }
  }
  /**
   * Sets values. Does not notify listeners.
   * @param values
   */
  public void setValues(int[][] values) {
    init(values.length);
    for(int r=0; r<size; ++r) {
      for(int c=0; c<size; ++c) {
        Cell cell = cells[r][c];
        cell.value = values[r][c];
        cell.isFixed = cell.value>0;
        cell.row[cell.value] = true;
        cell.col[cell.value] = true;
        cell.subRect[cell.value] = true;
      }
    }
  }
  public void resetValues(int size) {
    init(size);
  }
  /**
   * Sets value but only if valid. Does notify listeners.
   * This routine is used by code for manually entering fixed cell values.
   * Sets isFixed to value>0 .
   * @param row
   * @param col
   * @param value
   * @return if value is valid; if it ain't, then value isn't changed.
   */
  public boolean setValue(int row, int col, int value) {
    Cell cell = cells[row][col];
    boolean valid =
        value==0 ||
        ( 0<value && value<=size &&
          !(cell.row[value] || cell.col[value] || cell.subRect[value])
        );
    if(!valid) {
      return false;
    }
    int before = cell.value;
    cell.setValue(value);
    cell.isFixed = value>0;
    fireCellChange(row, col, before, value);
    return true;
  }
  public Cell getCell(int row, int col) {
    return cells[row][col];
  }
  public int getValue(int row, int col) {
    return cells[row][col].value;
  }
  public int[][] getValues() {
    int[][] rv = new int[size][];
    for(int r=0; r<size; ++r) {
      rv[r] = new int[size];
      for(int c=0; c<size; ++c) {
        rv[r][c] = cells[r][c].value;
      }
    }
    return rv;
  }
  public void toggleIsRunning() {
    isRunning = !isRunning;
    if(isRunning) {
      SwingUtilities.invokeLater(new StepRunnable());
    }
    else {
      MySwingUtilities.cancel();
    }
  }
  private class StepRunnable implements Runnable {
    public void run() {
      if(isAtEnd()) {
        throw new IllegalStateException("Already at end.");
      }
      step();
      if(isRunning) {
        if(isAtEnd() || isSolution()) {
          isRunning = false;
        }
        else {
//        SwingUtilities.invokeLater(new StepRunnable());
          MySwingUtilities.invokeLater(getDelay(), new StepRunnable());
        }
      }
    }
  }
  /**
   * Step forward until solution is found, or until all combinations have been
   * tried.
   * Precondition: not isAtEnd().
   * @return true iff a solution is found
   */
  public boolean solve() {
    if(isAtEnd()) {
      throw new IllegalStateException("Already at end.");
    }
    do {
      step();
    }
    while(!isAtEnd() && !isSolution());
    
    if(isSolution()) {
      return true;
    } else if(isAtEnd()) {
      return false;
    }
    else {
      throw new IllegalStateException("Internal error.");
    }
  }
  /**
   * Take a step towards next solution.
   */
  public void step() {
    ++stepCount;
    if(nextRow==size) {
      // find next solution
      gotoPreviousCell();
      backingUp = true;
      return;
    }
    if(nextRow==-1) {
      throw new IllegalStateException("Already at end.");
    }
    {
      Cell cell = cells[nextRow][nextCol];
      if(cell.isFixed) {
        if(backingUp) {
          gotoPreviousCell();
          return;
        }
        else {
          gotoNextCell();
          return;
        }
      }
      else {
        int nextValue = cell.value;
        int valueBefore = nextValue;
        boolean isValid;
        do {
          ++nextValue;
          isValid = 
              nextValue <= size
           && cell.subRect[nextValue]==false
           && cell.row[nextValue]==false
           && cell.col[nextValue]==false;
          if(isValid) {
            break;
          }
        }
        while(nextValue<=size);
        if(isValid) {
          backingUp = false;
          int valueAfter = nextValue;
          setCellValue(cell, nextRow, nextCol, valueBefore, valueAfter);
          gotoNextCell();
          return;
        }
        else {
          backingUp = true;
          cell.clear();
          gotoPreviousCell();
          return;
        }
      }
    }
  }
  private void gotoNextCell() {
    RowCol rcBefore = new RowCol(nextRow,nextCol);
    ++nextCol;
    if(nextCol==size) {
      nextCol = 0;
      ++nextRow;
    }
    RowCol rcAfter = new RowCol(nextRow,nextCol);
    fireRowColChange(rcBefore, rcAfter);
    if(isSolution()) {
      fireSolutionFound();
    }
  }
  private void gotoPreviousCell() {
    RowCol rcBefore = new RowCol(nextRow,nextCol);
    --nextCol;
    if(nextCol<0) {
      nextCol = size-1;
      --nextRow;
    }
    RowCol rcAfter = new RowCol(nextRow,nextCol);
    fireRowColChange(rcBefore, rcAfter);
    if(isAtEnd()) {
      fireAtEnd();
    }
  }
  private void fireRowColChange(RowCol rcBefore, RowCol rcAfter) {
    for(SudokuListener listener : listeners) {
      listener.rowColChanged(this, rcBefore, rcAfter);
    }
  }
  private void fireAtEnd() {
    for(SudokuListener listener : listeners) {
      listener.atEnd(this);
    }
  }
  private void fireSolutionFound() {
    for(SudokuListener listener : listeners) {
      listener.solutionFound(this);
    }
  }
  private void setCellValue(Cell cell, int row, int col, int before,
      int after) {
    cell.setValue(after);
    fireCellChange(row, col, before, after);
  }
  private void fireCellChange(int row, int col, int before, int after) {
    for(SudokuListener listener : listeners) {
      listener.cellChanged(this, new RowCol(row,col), before, after);
    }
  }
  public boolean isSolution() {
    boolean rv = nextRow==size;
    return rv;
  }
  public boolean isAtEnd() {
    boolean rv = nextRow==-1;
    return rv;
  }
  /**
   * Parse cells, both cell.value and cell.isFixed.
   * @param reader
   * @return each Cell returned has null row, col, subRect.
   * @throws IOException 
   */
  public static Cell[][] parseSpaceSeparatedCells(BufferedReader reader)
      throws IOException {
    String line;
    line = reader.readLine();
    int n = Integer.parseInt(line.trim());
    int fieldWidth = n<10 ? 3 : 4;
    int lineLength = n * fieldWidth;
    Cell[][] rows = new Cell[n][];
    for(int r=0; r<n; ++r) {
      Cell[] row = new Cell[n];
      rows[r] = row;
      line = reader.readLine();
      if(lineLength<line.length()) {
        String spaces =
          String.format("%1$" + (lineLength-line.length()) + "c", ' ');
        line = line + spaces;
      }
      for(int c=0; c<n; ++c) {
        String field = line.substring(c*fieldWidth, (c+1)*fieldWidth).trim();
        Cell cell = new Cell();
        row[c] = cell;
        cell.value = field.length()==0 ? 0 : Integer.parseInt(field);
        cell.isFixed = field.contains("+");
      }
    }
    return rows;
  }
  public static int[][] parseSpaceSeparatedValues(BufferedReader reader)
      throws IOException {
    String line;
    line = reader.readLine();
    int n = Integer.parseInt(line.trim());
    int fieldWidth = n<10 ? 3 : 4;
    int lineLength = n * fieldWidth;
    int[][] rows = new int[n][];
    for(int r=0; r<n; ++r) {
      int[] row = new int[n];
      rows[r] = row;
      line = reader.readLine();
      if(lineLength<line.length()) {
        String spaces =
          String.format("%1$" + (lineLength-line.length()) + "c", ' ');
        line = line + spaces;
      }
      for(int c=0; c<n; ++c) {
        String field = line.substring(c*fieldWidth, (c+1)*fieldWidth).trim();
        row[c] = field.length()==0 ? 0 : Integer.parseInt(field);
      }
    }
    return rows;
  }
  private static final String ioVersion = "1.0";
  public void read(BufferedReader br) throws IOException {
    String nl = System.getProperty("line.separator");
    String line;
    int lineNum = 0;
    TreeMap<String,Long> numbers = new TreeMap<String, Long>();
    TreeMap<String,Boolean> booleans = new TreeMap<String, Boolean>();
    TreeMap<String,String> strings = new TreeMap<String, String>();
    for(String name : Arrays.asList("sudokuVersion", "size","nextRow",
        "nextCol", "stepCount", "backingUp")) {
      line = br.readLine();
      ++lineNum;
      String prefix = name+": ";
      if(line.startsWith(prefix)) {
        String s = line.substring(prefix.length(),line.length()).trim();
        if(name.equals("sudokuVersion")) {
          if(!s.equals(ioVersion)) {
            throw new IOException("Input file must be version " + ioVersion);
          }
          strings.put(name, s);
        }
        else if(name.equals("backingUp")) {
          booleans.put(name, Boolean.parseBoolean(s));
        }
        else {
          numbers.put(name, Long.parseLong(s));
        }
      }
      else {
        throw new IOException("Error at line " + lineNum + ":" + nl + line);
      }
    }
    int n = (int)(long)numbers.get("size");
    int fieldWidth = n<10 ? 3 : 4;
    int lineLength = n * fieldWidth;
    Cell[][] rows = new Cell[n][];
    for(int r=0; r<n; ++r) {
      Cell[] row = new Cell[n];
      rows[r] = row;
      line = br.readLine();
      if(lineLength<line.length()) {
        String spaces =
          String.format("%1$" + (lineLength-line.length()) + "c", ' ');
        line = line + spaces;
      }
      for(int c=0; c<n; ++c) {
        String field = null;
        try {
          field = line.substring(c*fieldWidth, (c+1)*fieldWidth).trim();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        Cell cell = new Cell();
        row[c] = cell;
        cell.value = field.length()==0 ? 0 : Integer.parseInt(field);
        cell.isFixed = field.contains("+");
      }
    }
    // Parsing okay: now apply the values:
    init(size);
    setCellValues(rows);
    nextRow = (int)(long)numbers.get("nextRow");
    nextCol = (int)(long)numbers.get("nextCol");
    stepCount = numbers.get("stepCount");
    backingUp = booleans.get("backingUp");
  }
  public void write(BufferedWriter w) throws IOException {
    String nl = System.getProperty("line.separator");
    w.write("sudokuVersion: "+ioVersion+nl);
    w.write("size: "+size+nl);
    w.write("nextRow: "+nextRow+nl);
    w.write("nextCol: "+nextCol+nl);
    w.write("stepCount: "+stepCount+nl);
    w.write("backingUp: "+backingUp+nl);
    int fieldWidth = size<10 ? 3 : 4;
    String fmt = "%"+fieldWidth+"d";
    String fmtFixed = "%+"+fieldWidth+"d";
    String spaces = String.format("%1$" + fieldWidth + "c", ' ');
    for(int r=0; r<size; ++r) {
      for(int c=0; c<size; ++c) {
        Cell cell = cells[r][c];
        int v = cell.value;
        String s;
        if(v==0) {
          s = spaces;
        }
        else {
          s = String.format(cell.isFixed ? fmtFixed : fmt, v);
        }
        w.write(s);
      }
      w.write(nl);
    }
    w.flush();
  }
//  public void printCellValues(BufferedWriter w) throws IOException {
//    String nl = System.getProperty("line.separator");
//    w.write(""+size);
//    w.write(nl);
//    int fieldWidth = size<10 ? 3 : 4;
//    String fmt = "%"+fieldWidth+"d";
//    String fmtFixed = "%+"+fieldWidth+"d";
//    String spaces = String.format("%1$" + fieldWidth + "c", ' ');
//    for(int r=0; r<size; ++r) {
//      for(int c=0; c<size; ++c) {
//        Cell cell = cells[r][c];
//        int v = cell.value;
//        String s;
//        if(v==0) {
//          s = spaces;
//        }
//        else {
//          s = String.format(cell.isFixed ? fmtFixed : fmt, v);
//        }
//        w.write(s);
//      }
//      w.write(nl);
//    }
//    w.flush();
//  }
//  public void printValues(Writer w, boolean drawDashes, boolean drawZeros)
//      throws IOException {
//    String nl = System.getProperty("line.separator");
//    w.write(""+size);
//    w.write(nl);
//    int fieldWidth = size<10 ? 3 : 4;
//    String fmt = "%"+fieldWidth+"d";
//    String spaces = String.format("%1$" + fieldWidth + "c", ' ');
//    String dashes = spaces.replaceAll("\\s", "-");
//    for(int r=0; r<size; ++r) {
//      if (drawDashes) {
//        if (r > 0 && r < size - 1 && r % subSize == 0) {
//          for (int c = 0; c < size; ++c) {
//            if (c > 0 && c % subSize == 0) {
//              w.write("+");
//            }
//            w.write(dashes);
//          }
//          w.write(nl);
//        }
//      }
//      for(int c=0; c<size; ++c) {
//        if(drawDashes) {
//          if(c>0 && c%subSize==0) {
//            w.write("|");
//          }
//        }
//        int value = cells[r][c].value;
//        String s;
//        if(value==0 && !drawZeros) {
//          s = spaces;
//        }
//        else {
//          s = String.format(fmt, cells[r][c].value);
//        }
//        w.write(s);
//      }
//      w.write(nl);
//    }
//    w.flush();
//  }
}
