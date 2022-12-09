package de.samply.transfair.mappings;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.Configuration;
import de.samply.transfair.controller.TransferController;
import de.samply.transfair.fhir.clients.FhirClient;
import de.samply.transfair.fhir.writers.FhirFileSaver;
import de.samply.transfair.fhir.writers.FhirServerSaver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Bbmri2Bbmri extends FhirMappings {

  private static final Logger log = LoggerFactory.getLogger(Bbmri2Bbmri.class);

  @Autowired Configuration configuration;

  @Autowired TransferController transferController;

  private List<String> resources;

  public void transfer() {
    log.info("Running TransFAIR in BBMRI2BBMRI mode");
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

    log.info("Setup complete");

    log.info("Start collecting Resources from FHIR server " + sourceFhirServer);

    FhirClient sourceClient = new FhirClient(transferController.getCtx(), sourceFhirServer);

    if (Objects.nonNull(configuration.getSourceFhirServerUsername())
        && Objects.nonNull(configuration.getSourceFhirServerPassword())) {
      sourceClient.setBasicAuth(
          configuration.getSourceFhirServerUsername(), configuration.getSourceFhirServerPassword());
    }

    if (configuration.isSaveToFileSystem()) {
      this.fhirExportInterface = new FhirFileSaver(transferController.getCtx());
    } else {
      this.fhirExportInterface = new FhirServerSaver(transferController.getCtx(), targetFhirServer);
    }

    // TODO: Collect Organization and Collection

    this.fhirExportInterface.export(
        transferController.buildResources(transferController.fetchOrganizations(sourceClient.getClient())));
    this.fhirExportInterface.export(
        transferController.buildResources(
            transferController.fetchOrganizationAffiliation(sourceClient.getClient())));

    int counter = 1;

    HashSet<String> patientRefs = transferController.getSpecimenPatients(sourceClient.getClient());

    log.info("Loaded " + patientRefs.size() + " Patients");

    for (String pid : patientRefs) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      patientResources.add(transferController.fetchPatientResource(sourceClient.getClient(), pid));
      patientResources.addAll(transferController.fetchPatientSpecimens(sourceClient.getClient(), pid));
      patientResources.addAll(transferController.fetchPatientObservation(sourceClient.getClient(), pid));

      patientResources.addAll(transferController.fetchPatientCondition(sourceClient.getClient(), pid));

      this.fhirExportInterface.export(transferController.buildResources(patientResources));
      log.info("Exported Resources " + counter++ + "/" + patientRefs.size());
    }
  }

  private Boolean setup() {

    if (configuration.getSourceFhirServer().isBlank()) {
      return false;
    }
    return configuration.isSaveToFileSystem()
        || !configuration.getTargetFhirServer().isBlank()
        || !this.overrideSourceFhirServer.isEmpty()
        || !this.overrideTargetFhirServer.isEmpty();
  }
}
