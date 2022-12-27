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
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Mapping for MII KDS data and transformation to bbmri.de. */
@Component
@Slf4j
public class Mii2Bbmri extends FhirMappings {

  @Autowired FhirComponent fhirComponent;

  List<String> resources;

  @Autowired FilterService filterService;

  ProfileFormats sourceFormat = ProfileFormats.MII;
  ProfileFormats targetFormat = ProfileFormats.BBMRI;

  /** Transferring. */
  public void transfer() {
    this.setup();

    IGenericClient sourceClient = fhirComponent.getSourceFhirServer();

    HashSet<String> patientIds =
        fhirComponent.transferController.fetchPatientIds(
            sourceClient, fhirComponent.configuration.getStartResource());

    if (Objects.nonNull(filterService.blacklist)) {
      patientIds.removeIf(item -> filterService.blacklist.patient.ids.contains(item));
    }

    if (Objects.nonNull(filterService.whitelist)) {
      patientIds.removeIf(item -> !filterService.whitelist.patient.ids.contains(item));
    }

    log.info("Loaded " + patientIds.size() + " Patients");

    int counter = 1;

    for (String pid : patientIds) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      if (resources.contains("Patient")) {
        PatientMapping ap = new PatientMapping();
        log.debug("Analysing patient " + pid + " with format MII KDS");
        ap.fromMii(fhirComponent.transferController.fetchPatientResource(sourceClient, pid));
        patientResources.add(ap.toBbmri());
      }
      if (resources.contains("Specimen")) {
        for (Specimen specimen :
            fhirComponent.transferController.fetchPatientSpecimens(sourceClient, pid)) {
          SpecimenMapping transferSpecimenMapping = new SpecimenMapping();
          log.debug("Analysing Specimen " + specimen.getId() + " with format bbmri.de");
          transferSpecimenMapping.fromBbmri(specimen);

          log.debug("Analysing Specimen " + specimen.getId() + " with format bbmri.de");
          patientResources.add(transferSpecimenMapping.toBbmri());
        }
      }
      if (resources.contains("Condition")) {
        for (IBaseResource base :
            fhirComponent.transferController.fetchPatientCondition(sourceClient, pid)) {
          Condition condition = (Condition) base;

          if (CheckResources.checkMiiCauseOfDeath(condition)) {
            CauseOfDeathMapping causeOfDeathMapping = new CauseOfDeathMapping();
            causeOfDeathMapping.fromMii(condition);
            log.debug("Analysing Cause of Death " + condition.getId() + " with format mii");

            patientResources.add(causeOfDeathMapping.toBbmri());
            log.debug("Exporting Cause of Death " + condition.getId() + " with format bbmri");
          } else {
            ConditionMapping conditionMapping = new ConditionMapping();
            conditionMapping.fromMii(condition);
            patientResources.add(conditionMapping.toBbmri());
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
