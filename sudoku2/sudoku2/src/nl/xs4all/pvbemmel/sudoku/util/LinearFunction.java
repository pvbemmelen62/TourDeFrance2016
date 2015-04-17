package nl.xs4all.pvbemmel.sudoku.util;

/**
 * Maps value to value*s + t.
 * @author Paul van Bemmelen
 */
public class LinearFunction implements Function1D<Double> {
  private double s = 10;
  private double t = 5;
  private LinearFunction inverse;
  /**
   * Maps value to value*s + t.
   * @author Paul van Bemmelen
   */
  public LinearFunction(double s, double t) {
    this.s = s;
    this.t = t;
    inverse = new LinearFunction(1.0/s, -t/s, this);
  }
  private LinearFunction(double s, double t, LinearFunction inverse) {
    this.s = s;
    this.t = t;
    this.inverse = inverse;
  }
  public Double getValue(Double value) {
    return value*s + t;
  }
  public Double getValue(Integer value) {
    return value*s + t;
  }
  public LinearFunction getInverse() {
    return inverse;
  }
  /**
   * Set constraints: force function to map x[i] to y[i] , i=0,1 .
   */
  public void setConstraints(Double[] x, Double[] y) {
    if( !( x.length == y.length && y.length == 2) ) {
      throw new IllegalArgumentException("Wrong number of values.");
    }
    //|y0|      |x0|      |1|
    //|  | =  s |  | +  t | |
    //|y1|      |x1|      |1|
    double det = x[0]-x[1];
    if(det==0.0) {
      throw new IllegalArgumentException("Constraints are dependent.");
    }
    s = (y[0] - y[1])/det;
    t = (x[0]*y[1] - x[1]*y[0])/det;
    inverse.s = 1.0/s;
    inverse.t = -t/s;
  }
  public double getScale() {
    return s;
  }
  public double getTranslate() {
    return t;
  }
  public static void main(String[] args) {
    LinearFunction f = new LinearFunction(10,5);
    for(int i=0; i<10; ++i) {
      double fv = f.getValue((double)i);
      System.out.println(
        i +
        "-- f.getValue() --> " + fv +
        "-- f.getInverse.getValue() --> " + f.getInverse().getValue(fv) );
    }
  }
}
