package de.samply.transfair.mappings;

import de.samply.transfair.fhir.FhirComponent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Jump Mapping.
 * This mapping transfers everything from one blaze with bbmri to another
 */
@Component
@Slf4j
public class Bbmri2Beacon extends FhirMappings {

  @Autowired FhirComponent fhirComponent;

  private List<String> resources;

  /** Transferring. */
  public void transfer() {
    log.info("Running TransFAIR in BBMRI2BEACON mode");
    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }

    log.info("Setup complete");

    // TODO: Collect Organization and Collection

    fhirComponent
        .getFhirExportInterface()
        .export(
            fhirComponent.transferController.buildResources(
                fhirComponent.transferController.fetchOrganizations(
                    fhirComponent.getSourceFhirServer())));
    fhirComponent
        .getFhirExportInterface()
        .export(
            fhirComponent.transferController.buildResources(
                fhirComponent.transferController.fetchOrganizationAffiliation(
                    fhirComponent.getSourceFhirServer())));

    int counter = 1;

    HashSet<String> patientRefs =
        fhirComponent.transferController.getSpecimenPatients(fhirComponent.getSourceFhirServer());

    log.info("Loaded " + patientRefs.size() + " Patients");

    for (String pid : patientRefs) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      patientResources.add(
          fhirComponent.transferController.fetchPatientResource(
              fhirComponent.getSourceFhirServer(), pid));
      patientResources.addAll(
          fhirComponent.transferController.fetchPatientSpecimens(
              fhirComponent.getSourceFhirServer(), pid));
      patientResources.addAll(
          fhirComponent.transferController.fetchPatientObservation(
              fhirComponent.getSourceFhirServer(), pid));

      patientResources.addAll(
          fhirComponent.transferController.fetchPatientCondition(
              fhirComponent.getSourceFhirServer(), pid));

      fhirComponent
          .getFhirExportInterface()
          .export(fhirComponent.transferController.buildResources(patientResources));
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
