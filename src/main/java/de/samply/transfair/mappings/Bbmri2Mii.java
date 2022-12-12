package de.samply.transfair.mappings;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.Configuration;
import de.samply.transfair.controller.TransferController;
import de.samply.transfair.fhir.writers.FhirFileSaver;
import de.samply.transfair.fhir.writers.FhirServerSaver;
import de.samply.transfair.models.ProfileFormats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
  @Autowired Configuration configuration;

  @Autowired TransferController transferController;

  List<String> resources;

  public void transfer() throws Exception {

    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }

    String sourceFhirServer =
        Objects.nonNull(overrideSourceFhirServer)
            ? overrideSourceFhirServer
            : configuration.getSourceFhirServer();
    String targetFhirServer =
        Objects.nonNull(overrideTargetFhirServer)
            ? overrideTargetFhirServer
            : configuration.getTargetFhirServer();

    if (configuration.isSaveToFileSystem()) {
      this.fhirExportInterface = new FhirFileSaver(configuration.getCtx());
    } else {
      this.fhirExportInterface = new FhirServerSaver(configuration.getCtx(), targetFhirServer);
    }

    log.info("Setup complete");

    log.info("Start collecting Resources from FHIR server " + sourceFhirServer);
    IGenericClient sourceClient =
        configuration.getCtx().newRestfulGenericClient(sourceFhirServer);

    HashSet<String> patientIds =
        transferController.fetchPatientIds(sourceClient, configuration.getStartResource());

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

      this.fhirExportInterface.export(transferController.buildResources(patientResources));
      log.info("Exported Resources " + counter++ + "/" + patientIds.size());
    }
  }

  private Boolean setup() {

    if (configuration.getSourceFhirServer().isBlank()) {
      return false;
    }

    if (configuration.getResourcesFilter().isEmpty()) {
      this.resources = List.of("Patient", "Specimen", "Condition", "Observation");
    } else {
      this.resources = Arrays.stream(configuration.getResourcesFilter().split(",")).toList();
    }

    return configuration.isSaveToFileSystem() || !configuration.getTargetFhirServer().isBlank()
        || !this.overrideSourceFhirServer.isEmpty()
        || !this.overrideTargetFhirServer.isEmpty();
  }
}
