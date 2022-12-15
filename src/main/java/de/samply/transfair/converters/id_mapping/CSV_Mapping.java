package de.samply.transfair.converters.id_mapping;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * Reads ID-Mappings from csv file. See read_mappings method for details. Access to the mappings is
 * provided by using the methods from the parent class.
 *
 * @see this.read_mappings
 * @see ID_Mapping
 * @author jdoerenberg
 */
public class CSV_Mapping extends ID_Mapping {

  private String filepath;

  /** Standard constructor */
  public CSV_Mapping() {
    super();
  }

  /**
   * Constructor which stores filepath
   *
   * @param s filepath to csv file where mappings are loaded from
   */
  @SuppressWarnings("unused")
  public CSV_Mapping(@NotNull String s) {
    super();
    this.filepath = s;
  }

  /**
   * Standard setter for field this.filepath
   *
   * @param s filepath to csv file where mappings are loaded from
   */
  public void setFilepath(@NotNull String s) {
    this.filepath = s;
  }

  /**
   * Standard getter for field this.filepath
   *
   * @return filepath to csv file where mappings are loaded from
   */
  @SuppressWarnings("unused")
  public String getFilepath() {
    return this.filepath;
  }

  /**
   * Reads ID mappings from a csv file and stores them in the data structure provided in parental
   * class. The column headlines (first row) contain the names of the domains mapped to each other.
   * After reading these, mappings are processed row by row. If two domains contain a non-empty
   * value, these values are stored in the mapping. This is done for each pair of non-empty values
   * in the row.
   *
   * @throws IOException in case file at this.filepath cannot be read (e.g. opened by different
   *     program, does not exist,...)
   * @throws CsvException in case file at this.filepath is not a proper csv file
   */
  @Override
  public String fetch_mapping(
      @NotNull String id, @NotNull String src_domain, @NotNull String tar_domain)
      throws IOException, CsvException, Exception {
    CSVReader reader = new CSVReader(new FileReader(filepath)); // Potential IOException
    String[] row;

    // Get indicex of rows containing source domain and targe domain
    String[] domains = reader.readNext();
    int src_idx = -1;
    int tar_idx = -1;
    for (int i = 0; i < domains.length; i++) {
      if (domains[i].equals(src_domain)) src_idx = i;
      if (domains[i].equals(tar_domain)) tar_idx = i;
    }
    // If one of the domains is not found, throw an exception
    if (src_idx == -1)
      throw new Exception("Domain " + src_domain + " not found in csv file " + this.filepath);
    if (tar_idx == -1)
      throw new Exception("Domain " + tar_domain + " not found in csv file " + this.filepath);

    // Iterate over whole csv file and search for mapping between the domains where value of
    // src_domain is argumentid
    while ((row = reader.readNext()) != null) {
      if (row[src_idx].equals(id) && !row[tar_idx].equals("")) return row[tar_idx];
    }
    throw new Exception(
        "No mapping from domain "
            + src_domain
            + " to domain "
            + tar_domain
            + " found for id "
            + id);
  }
}
