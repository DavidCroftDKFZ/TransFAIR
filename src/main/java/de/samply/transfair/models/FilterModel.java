package de.samply.transfair.models;

import java.util.ArrayList;

/** Json class for filtering attr. */
public class FilterModel {
  /** Patient related attrs. */
  public Patient patient;

  /** Specimen related attrs. */
  public Specimen specimen;

  /** Class fppr specimen related attrs. */
  public static class Specimen {
    public ArrayList<String> ids;
    public ArrayList<String> profile;
    public ArrayList<String> orgaFilter;
  }

  /** Class fppr specimen related attrs. */
  public static class Patient {
    public ArrayList<String> ids;
    public ArrayList<String> profile;
    public ArrayList<String> orgaFilter;
  }
}
