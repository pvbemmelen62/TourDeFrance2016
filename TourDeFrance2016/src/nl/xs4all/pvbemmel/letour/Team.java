package nl.xs4all.pvbemmel.letour;

import java.util.*;

public class Team {
  private String name;
  private String country;
  private List<String> managers;
  private List<Cyclist> cyclists;

  public Team() {
    this(null, null);
  }
  public Team(String name, String country) {
    this.name = name;
    this.country = country;
    this.managers = new ArrayList<String>();
    this.cyclists = new ArrayList<Cyclist>();
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
  public void addManager(String manager) {
    managers.add(manager);
  }
  public void removeManager(String manager) {
    managers.remove(manager);
  }
  public List<String> getManagers() {
    return managers;
  }
  public void setManagers(List<String> managers) {
    this.managers = managers;
  }
  public void addCyclist(Cyclist cyclist) {
    cyclists.add(cyclist);
  }
  public void removeCyclist(Cyclist cyclist) {
    cyclists.remove(cyclist);
  }
  public List<Cyclist> getCyclists() {
    return cyclists;
  }
  public void setCyclists(List<Cyclist> cyclists) {
    this.cyclists = cyclists;
  }
  public String toString() {
    return "[" + name + ", " + "(" + country + ")"
      + ", " + "#managers:" + managers.size()
      
      + ", " + "#cyclists:" + cyclists.size() + "]";
  }
  
}