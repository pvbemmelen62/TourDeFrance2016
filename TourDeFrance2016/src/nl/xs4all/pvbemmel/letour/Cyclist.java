package nl.xs4all.pvbemmel.letour;

class Cyclist {
  private String name;
  private String country;
  private int number;

  
  public Cyclist() {
  }
  public Cyclist(int number, String name, String country) {
    this.name = name;
    this.country = country;
    this.number = number;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getCountry() {
    return country;
  }
  public void setCountry(String country) {
    this.country = country;
  }
  public int getNumber() {
    return number;
  }
  public void setNumber(int number) {
    this.number = number;
  }

  public String toString() {
    String rv = String.format("[%4d, %s, (%s)]", number, name, country);
    return rv;
  }
  
  public static class Test {
    public static void main(String[] args) {
      Cyclist c = new Cyclist(1, "Paul van Bemmelen", "Ned");
      System.out.println(c);
      System.out.flush();
    }
  }
}