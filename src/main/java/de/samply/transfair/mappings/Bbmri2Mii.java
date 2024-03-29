package de.samply.transfair.mappings;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.FilterService;
import de.samply.transfair.enums.ProfileFormats;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.resources.CauseOfDeathMapping;
import de.samply.transfair.resources.CheckResources;
import de.samply.transfair.resources.ConditionMapping;
import de.samply.transfair.resources.PatientMapping;
import de.samply.transfair.resources.SpecimenMapping;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/** Mapping for bbmri.de data and transformation to MII KDS. */
@Component
@Slf4j
public class Bbmri2Mii extends FhirMappings {

  ProfileFormats sourceFormat = ProfileFormats.BBMRI;
  ProfileFormats targetFormat = ProfileFormats.MII;

  @Autowired FhirComponent fhirComponent;

  @Autowired FilterService filterService;

  List<String> resources;

  /** Transferring. */
  public void transfer() throws Exception {

    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }

    log.info("Setup complete");

    IGenericClient sourceClient = fhirComponent.getSourceFhirServer();

    HashSet<String> patientIds =
        fhirComponent.transferController.fetchPatientIds(
            sourceClient, fhirComponent.configuration.getStartResource());

    if (Objects.nonNull(filterService.blacklist)) {
      patientIds.removeIf(item -> filterService.blacklist.patient.ids.contains(item));
    }

    if (Objects.nonNull(filterService.whitelist)) {
      patientIds.removeIf(item -> !filterService.blacklist.patient.ids.contains(item));
    }

    log.info("Loaded " + patientIds.size() + " Patients");

    int counter = 1;

    for (String pid : patientIds) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      if (resources.contains("Patient")) {
        PatientMapping ap = new PatientMapping();
        log.debug("Analysing patient " + pid + " with format bbmri");
        ap.fromBbmri(fhirComponent.transferController.fetchPatientResource(sourceClient, pid));
        patientResources.add(ap.toMii());
      }
      if (resources.contains("Condition")) {
        for (IBaseResource base :
            fhirComponent.transferController.fetchPatientCondition(sourceClient, pid)) {
          Condition condition = (Condition) base;
          ConditionMapping conditionMapping = new ConditionMapping();

          conditionMapping.fromBbmri(condition);
          patientResources.add(conditionMapping.toMii());
        }
      }
      if (resources.contains("Specimen")) {
        for (Specimen specimen :
            fhirComponent.transferController.fetchPatientSpecimens(sourceClient, pid)) {
          SpecimenMapping transferSpecimenMapping = new SpecimenMapping();
          log.debug("Analysing Specimen " + specimen.getId() + " with format mii kds");
          transferSpecimenMapping.fromBbmri(specimen);

          log.debug("Export Specimen " + specimen.getId() + " with format mii kds");
          patientResources.add(transferSpecimenMapping.toMii());
        }
      }
      if (resources.contains("Observation")) {
        for (IBaseResource base :
            fhirComponent.transferController.fetchPatientObservation(sourceClient, pid)) {
          Observation observation = (Observation) base;

          if (CheckResources.checkBbmriCauseOfDeath(observation)) {
            CauseOfDeathMapping causeOfDeathMapping = new CauseOfDeathMapping();
            causeOfDeathMapping.fromBbmri(observation);
            log.debug("Analysing Cause of Death " + observation.getId() + " with format bbmri");

            if (targetFormat == ProfileFormats.BBMRI) {
              patientResources.add(causeOfDeathMapping.toBbmri());
              log.debug("Analysing Cause of Death " + observation.getId() + " with format bbmri");
            }
          }
        }
      }

      fhirComponent
          .getFhirExportInterface()
          .export(fhirComponent.transferController.buildResources(patientResources));
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
