package nl.xs4all.pvbemmel.sudoku;

public interface SudokuListener {

  void rowColChanged(Sudoku s, RowCol rcBefore, RowCol rcAfter);
  void cellChanged(Sudoku s, RowCol rc, int before, int after);
  void atEnd(Sudoku s);
  void solutionFound(Sudoku s);
}
