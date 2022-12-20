package de.samply.transfair.converters;

import de.samply.transfair.converters.idMapping.CsvMapping;
import de.samply.transfair.converters.idMapping.IdMapping;
import de.samply.transfair.converters.idMapping.IdentityMapping;
import de.samply.transfair.enums.ResourceType;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class holds the different ID's and can map to the opposite project.
 * It uses the Singleton pattern in order to be accessible from the whole project.
 */
@Component("IdMapper")
@Slf4j
public class IdMapper {

  private IdMapping idMapping;

  private final String mapperSetting;

  private String csvMappingsPath;

  /** New Idmapper. */
  public IdMapper() {
    // TODO: injections do not work in test...
    this.mapperSetting = "csvmapping";
    this.csvMappingsPath = "./test_mapping.csv";
  }

  @PostConstruct
   void setup() {
    // injection. @PostConstruct causes NullpointerException in .to... methods
    switch (this.mapperSetting) {
      case "csvmapping" -> {
        // log.info("Using csvmapping " + this.csv_mappings_path); //TODO: Readd
        this.idMapping = new CsvMapping(this.csvMappingsPath);
      }
      default -> { // If none of the above settings is matched
        // log.info("No ID-Mappings defined"); //TODO: Readd
        this.idMapping = new IdentityMapping();
      }
    }
  }

  /**
   * Standard getter for this.mapper_setting
   *
   * @return setting, which type of mapper should be used (e.g. csvmapper)
   */
  public String getMapperSetting() {
    return this.mapperSetting;
  }

  /**
   * Standard getter for this.csv_mappings_path
   *
   * @return setting, where csv file with mappings is located
   */
  public String getCsvMappingsPath() {
    return this.csvMappingsPath;
  }

  private final String prefixBbmri = "BBMRI."; // Prefix for IDs within BBMRI FHIR-Store
  private final String prefixMii = "MII."; // Prefix for IDs within MII FHIR-Store

  /**
   * Maps ids from MII domains to BBMRI domains depending on the FHIR resource type.
   *
   * @param id the id to be mapped to BBMRI domain
   * @param resourceType define which FHIR resource type the id belongs to - e.g. Patient, Specimen
   * @return id from the respective BBMRI domain
   * @throws IllegalArgumentException escalates exceptions from {@link IdMapping}.map_id method
   */
  public String toBbmri(String id, ResourceType resourceType) throws Exception {
    return switch (resourceType) {
      case PATIENT -> this.idMapping.mapId(id, prefixMii + "Patient", prefixBbmri + "Patient");
      case SPECIMEN -> this.idMapping.mapId(
          id, prefixMii + "Specimen", prefixBbmri + "Specimen");
    };
  }

  /**
   * Maps ids from BBMRI domains to MII domains depending on the FHIR resource type.
   *
   * @param id the id to be mapped to MII domain
   * @param resourceType define which FHIR resource type the id belongs to - e.g. Patient, Specimen
   * @return id from the respective MII domain
   * @throws IllegalArgumentException escalates exceptions from {@link IdMapping}.map_id method
   */
  public String toMii(String id, ResourceType resourceType) throws Exception {
    return switch (resourceType) {
      case PATIENT -> idMapping.mapId(id, prefixBbmri + "Patient", prefixMii + "Patient");
      case SPECIMEN -> idMapping.mapId(id, prefixBbmri + "Specimen", prefixMii + "Specimen");
    };
  }
}
