package de.samply.transfair.mappings;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.controller.TransferController;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.fhir.writers.FhirFileSaver;
import de.samply.transfair.fhir.writers.FhirServerSaver;
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
public class Bbmri2Mii extends FhirMappings {

  private static final Logger log = LoggerFactory.getLogger(Bbmri2Mii.class);

  ProfileFormats sourceFormat = ProfileFormats.BBMRI;
  ProfileFormats targetFormat = ProfileFormats.MII;

  @Autowired TransferController transferController;

  @Autowired FhirComponent fhirComponent;

  List<String> resources;

  public void transfer() throws Exception {

    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }

    log.info("Setup complete");

    IGenericClient sourceClient = fhirComponent.getSourceFhirServer();

    HashSet<String> patientIds =
        transferController.fetchPatientIds(
            sourceClient, fhirComponent.configuration.getStartResource());

    log.info("Loaded " + patientIds.size() + " Patients");

    int counter = 1;

    for (String pid : patientIds) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      if (resources.contains("Patient")) {
        patientResources.add(
            transferController.convertPatientResource(
                transferController.fetchPatientResource(sourceClient, pid),
                pid,
                this.sourceFormat,
                this.targetFormat));
      }
      List<IBaseResource> conditions = null;
      if (resources.contains("Condition")) {
        conditions.addAll(
            transferController.convertConditions(
                transferController.fetchPatientCondition(sourceClient, pid),
                this.sourceFormat,
                this.targetFormat));
        patientResources.addAll(conditions);
      }
      if (resources.contains("Specimen")) {
        patientResources.addAll(
            transferController.convertMiiSpecimenResources(
                transferController.fetchPatientSpecimens(sourceClient, pid), conditions));
      }
      if (resources.contains("Observation")) {
        patientResources.addAll(
            transferController.convertObservations(
                transferController.fetchPatientObservation(sourceClient, pid),
                this.sourceFormat,
                this.targetFormat));
      }

      fhirComponent
          .getFhirExportInterface()
          .export(transferController.buildResources(patientResources));
      log.info("Exported Resources " + counter++ + "/" + patientIds.size());
    }
  }

  private Boolean setup() {

    if (fhirComponent.configuration.getSourceFhirServer().isBlank()) {
      return false;
    }

    if (fhirComponent.configuration.getResourcesFilter().isEmpty()) {
      this.resources = List.of("Patient", "Specimen", "Condition", "Observation");
    } else {
      this.resources =
          Arrays.stream(fhirComponent.configuration.getResourcesFilter().split(",")).toList();
    }

    return fhirComponent.configuration.isSaveToFileSystem()
        || !fhirComponent.configuration.getTargetFhirServer().isBlank()
        || !this.overrideSourceFhirServer.isEmpty()
        || !this.overrideTargetFhirServer.isEmpty();
  }
}
