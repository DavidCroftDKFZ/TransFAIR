package de.samply.transfair.mappings;

import de.samply.transfair.Configuration;
import de.samply.transfair.controller.TransferController;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.fhir.clients.FhirClient;
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

  @Autowired TransferController transferController;

  @Autowired FhirComponent fhirComponent;

  private List<String> resources;

  public void transfer() {
    log.info("Running TransFAIR in BBMRI2BBMRI mode");
    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }

    log.info("Setup complete");

    // TODO: Collect Organization and Collection

    fhirComponent
        .getFhirExportInterface()
        .export(
            transferController.buildResources(
                transferController.fetchOrganizations(fhirComponent.getSourceFhirServer())));
    fhirComponent
        .getFhirExportInterface()
        .export(
            transferController.buildResources(
                transferController.fetchOrganizationAffiliation(
                    fhirComponent.getSourceFhirServer())));

    int counter = 1;

    HashSet<String> patientRefs =
        transferController.getSpecimenPatients(fhirComponent.getSourceFhirServer());

    log.info("Loaded " + patientRefs.size() + " Patients");

    for (String pid : patientRefs) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      patientResources.add(
          transferController.fetchPatientResource(fhirComponent.getSourceFhirServer(), pid));
      patientResources.addAll(
          transferController.fetchPatientSpecimens(fhirComponent.getSourceFhirServer(), pid));
      patientResources.addAll(
          transferController.fetchPatientObservation(fhirComponent.getSourceFhirServer(), pid));

      patientResources.addAll(
          transferController.fetchPatientCondition(fhirComponent.getSourceFhirServer(), pid));

      fhirComponent
          .getFhirExportInterface()
          .export(transferController.buildResources(patientResources));
      log.info("Exported Resources " + counter++ + "/" + patientRefs.size());
    }
  }

  private Boolean setup() {

    if (fhirComponent.configuration.getSourceFhirServer().isBlank()) {
      return false;
    }
    return fhirComponent.configuration.isSaveToFileSystem()
        || !fhirComponent.configuration.getTargetFhirServer().isBlank()
        || !this.overrideSourceFhirServer.isEmpty()
        || !this.overrideTargetFhirServer.isEmpty();
  }
}
