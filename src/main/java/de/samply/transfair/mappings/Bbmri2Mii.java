package de.samply.transfair.mappings;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.Configuration;
import de.samply.transfair.controller.TransferController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Bbmri2Mii {

  private static final Logger log = LoggerFactory.getLogger(Bbmri2Mii.class);


  @Autowired
  Configuration configuration;

  @Autowired
  TransferController transferController;

  List<String> resources;

  public void bbmri2mii() throws Exception {

    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }
    log.info("Setup complete");

    log.info("Start collecting Resources from FHIR server " + configuration.getSourceFhirServer());
    IGenericClient sourceClient = transferController.getCtx().newRestfulGenericClient(configuration.getSourceFhirServer());

    HashSet<String> patientIds = transferController.fetchPatientIds(sourceClient);

    log.info("Loaded all " + patientIds.size() + " Patients");

    int counter = 1;

    for (String pid : patientIds) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      if (resources.contains("Patient")) {
        patientResources.add(transferController.convertPatientResource(transferController.fetchPatientResource(sourceClient, pid), pid));
      }
      List<IBaseResource> conditions = null;
      if (resources.contains("Condition")) {
        conditions.addAll(transferController.convertConditions(transferController.fetchPatientCondition(sourceClient, pid)));
        patientResources.addAll(conditions);
      }
      if (resources.contains("Specimen")) {
        patientResources.addAll(
            transferController.convertMiiSpecimenResources(transferController.fetchPatientSpecimens(sourceClient, pid), conditions));
      }
      if (resources.contains("Observation")) {
        patientResources.addAll(transferController.convertObservations(transferController.fetchPatientObservation(sourceClient, pid)));
      }

      transferController.buildResources(patientResources);
      log.info("Exported Resources " + counter++ + "/" + patientIds.size());
    }
  }

  private Boolean setup() {

    if (configuration.getSourceFhirServer().isBlank()) {
      return false;
    }
    if (!configuration.isSaveToFileSystem() || configuration.getTargetFhirServer().isBlank()) {
      return false;
    }

    if (configuration.getResourcesFilter().isEmpty()) {
      this.resources = List.of("Patient", "Specimen", "Condition", "Observation");
     } else {
      this.resources = Arrays.stream(configuration.getResourcesFilter().split(",")).toList();
      }


    return true;
  }

}
