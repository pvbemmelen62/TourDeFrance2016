package nl.xs4all.pvbemmel.sudoku.util;

public interface Function1D<T> {
  T getValue(T value);
  Function1D<T> getInverse();
  /**
   * Constrain function so that it maps x[i] to y[i].
   * Must also constrain inverse to map y[i] to x[i].
   */
  void setConstraints(T[] x, T[] y);
}
