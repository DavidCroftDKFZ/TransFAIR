package de.samply.transfair.mappings;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.Configuration;
import de.samply.transfair.controller.TransferController;
import de.samply.transfair.models.ProfileFormats;
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
public class Mii2Bbmri {

  private static final Logger log = LoggerFactory.getLogger(Mii2Bbmri.class);


  @Autowired
  TransferController transferController;

  @Autowired
  Configuration configuration;

  List<String> resources;

  ProfileFormats sourceFormat = ProfileFormats.MII;
  ProfileFormats targetFormat = ProfileFormats.BBMRI;

  public void mii2bbmri() throws Exception {
    this.setup();

    log.info("Start collecting Resources from FHIR server " + configuration.getSourceFhirServer());
    IGenericClient sourceClient = transferController.getCtx().newRestfulGenericClient(configuration.getSourceFhirServer());

    HashSet<String> patientIds = transferController.fetchPatientIds(sourceClient);

    log.info("Loaded all " + patientIds.size() + " Patients");

    int counter = 1;

    for (String pid : patientIds) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      if (resources.contains("Patient")) {
        patientResources.add(
            transferController.convertPatientResource(transferController.fetchPatientResource(sourceClient, pid), pid));
      }
      if (resources.contains("Specimen")) {
        patientResources.addAll(
            transferController.convertBbmriSpecimenResources(transferController.fetchPatientSpecimens(sourceClient, pid)));
      }
      if (resources.contains("Observation")) {
        patientResources.addAll(transferController.convertObservations(transferController.fetchPatientObservation(sourceClient, pid)));
      }
      if (resources.contains("Condition")) {
        patientResources.addAll(transferController.convertConditions(transferController.fetchPatientCondition(sourceClient, pid)));
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
