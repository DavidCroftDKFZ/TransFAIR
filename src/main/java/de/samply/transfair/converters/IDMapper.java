package de.samply.transfair.converters;

import de.samply.transfair.converters.id_mapping.CSV_Mapping;
import de.samply.transfair.converters.id_mapping.ID_Mapping;
import de.samply.transfair.converters.id_mapping.Identity_Mapping;
import de.samply.transfair.enums.Resource_Type;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class holds the different ID's and can map to the opposite project. It uses the Singleton
 * pattern in order to be accessible from the whole project
 */
@Component("IDMapper")
@Slf4j
public class IDMapper {

  private ID_Mapping id_mapping;

  private final String mapper_setting;

  private String csv_mappings_path;

  // TODO: Autowired does not work from test...
  // @Autowired

  public IDMapper() {
    // TODO: injections do not work in test...
    this.mapper_setting = "csvmapping";
    this.csv_mappings_path = "./test_mapping.csv";
  }

  @PostConstruct
  public void
      setup() { // TODO: Should be called automatically after object was created i.e. after value
    // injection. @PostConstruct causes NullpointerException in .to... methods
    switch (this.mapper_setting) {
      case "csvmapping" -> {
        // log.info("Using csvmapping " + this.csv_mappings_path); //TODO: Readd
        this.id_mapping = new CSV_Mapping(this.csv_mappings_path);
      }
      default -> { // If none of the above settings is matched
        // log.info("No ID-Mappings defined"); //TODO: Readd
        this.id_mapping = new Identity_Mapping();
      }
    }
  }

  /**
   * Standard getter for this.mapper_setting
   *
   * @return setting, which type of mapper should be used (e.g. csvmapper)
   */
  public String getMapper_setting() {
    return this.mapper_setting;
  }

  /**
   * Standard getter for this.csv_mappings_path
   *
   * @return setting, where csv file with mappings is located
   */
  public String getCsv_mappings_path() {
    return this.csv_mappings_path;
  }

  private final String prefix_bbmri = "BBMRI."; // Prefix for IDs within BBMRI FHIR-Store
  private final String prefix_mii = "MII."; // Prefix for IDs within MII FHIR-Store

  /**
   * Maps ids from MII domains to BBMRI domains depending on the FHIR resource type
   *
   * @param id the id to be mapped to BBMRI domain
   * @param resource_type define which FHIR resource type the id belongs to - e.g. Patient, Specimen
   * @return id from the respective BBMRI domain
   * @throws IllegalArgumentException escalates exceptions from {@link ID_Mapping}.map_id method
   */
  public String toBbmri(String id, Resource_Type resource_type) throws Exception {
    return switch (resource_type) {
      case PATIENT -> this.id_mapping.map_id(id, prefix_mii + "Patient", prefix_bbmri + "Patient");
      case SPECIMEN -> this.id_mapping.map_id(
          id, prefix_mii + "Specimen", prefix_bbmri + "Specimen");
    };
  }

  /**
   * Maps ids from BBMRI domains to MII domains depending on the FHIR resource type
   *
   * @param id the id to be mapped to MII domain
   * @param resource_type define which FHIR resource type the id belongs to - e.g. Patient, Specimen
   * @return id from the respective MII domain
   * @throws IllegalArgumentException escalates exceptions from {@link ID_Mapping}.map_id method
   */
  public String toMii(String id, Resource_Type resource_type) throws Exception {
    return switch (resource_type) {
      case PATIENT -> id_mapping.map_id(id, prefix_bbmri + "Patient", prefix_mii + "Patient");
      case SPECIMEN -> id_mapping.map_id(id, prefix_bbmri + "Specimen", prefix_mii + "Specimen");
    };
  }
}
