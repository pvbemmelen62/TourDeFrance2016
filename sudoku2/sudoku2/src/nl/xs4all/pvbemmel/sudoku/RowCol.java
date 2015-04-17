package nl.xs4all.pvbemmel.sudoku;

public class RowCol {
  public int row;
  public int col;

  public RowCol(int row, int col) {
    this.row = row;
    this.col = col;
  }
  public String toString() {
    return "{" + row + ", " + col + "}";
  }

}
