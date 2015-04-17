package nl.xs4all.pvbemmel.sudoku;

//-------------------------------------------------------------------------
public class Cell {
  /**
   * Integer in [0,size].
   * Value 0 indicates that the cell is empty.
   */
  public int value;
  public boolean isFixed;
  public boolean[] subRect;
  /**
   * row[number] === "row contains number" (1<= number <= size).
   * row[0] is irrelevant, i.e. value is never used.
   */
  public boolean[] row;
  /**
   * col[number] === "column contains number" (1<= number <= size).
   * col[0] is irrelevant, i.e. value is never used.
   */
  public boolean[] col;
  /**
   * Sets value of cell and updates subRect, row, col.
   * Precondition: val <= size && subRect[val]==false
   *   && row[val]==false && col[val]==false 
   * @param val
   */
  public void setValue(int val) {
    subRect[value] = false;
    row[value] = false;
    col[value] = false;
    value = val;
    subRect[value] = true;
    row[value] = true;
    col[value] = true;
  }
  /**
   * Sets value of cell to 0, and updates subRect, row, col.
   * Precondition: !isFixed && val <= size
   */
  public void clear() {
    subRect[value] = false;
    row[value] = false;
    col[value] = false;
    value = 0;
  }
}