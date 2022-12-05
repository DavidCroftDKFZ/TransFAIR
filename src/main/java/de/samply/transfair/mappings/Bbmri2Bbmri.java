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
public class Bbmri2Bbmri {

  private static final Logger log = LoggerFactory.getLogger(Bbmri2Bbmri.class);

  @Autowired Configuration configuration;

  @Autowired TransferController transferController;

  ProfileFormats sourceFormat = ProfileFormats.BBMRI;
  ProfileFormats targetFormat;

  private List<String> resources;

  public void bbmri2bbmri() throws Exception {
    log.info("Running TransFAIR in BBMRI2BBMRI mode");
    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }
    log.info("Setup complete");

    this.sourceFormat = ProfileFormats.BBMRI;
    this.targetFormat = ProfileFormats.BBMRI;

    log.info("Start collecting Resources from FHIR server " + configuration.getSourceFhirServer());
    IGenericClient sourceClient =
        transferController.getCtx().newRestfulGenericClient(configuration.getSourceFhirServer());

    // TODO: Collect Organization and Collection

    log.info("Loaded all Patient ID's");

    transferController.buildResources(transferController.fetchOrganizations(sourceClient));
    transferController.buildResources(transferController.fetchOrganizationAffiliation(sourceClient));

    int counter = 1;

    HashSet<String> patientRefs = transferController.getSpecimenPatients(sourceClient);

    for (String pid : patientRefs) {
      List<IBaseResource> patientResources = new ArrayList<>();
      log.debug("Loading data for patient " + pid);

      patientResources.add(transferController.fetchPatientResource(sourceClient, pid));
      patientResources.addAll(transferController.fetchPatientSpecimens(sourceClient, pid));
      patientResources.addAll(transferController.fetchPatientObservation(sourceClient, pid));

      patientResources.addAll(transferController.fetchPatientCondition(sourceClient, pid));

      transferController.buildResources(patientResources);
      log.info("Exported Resources " + counter++ + "/" + patientRefs.size());
    }
  }

  private Boolean setup() {

    if (configuration.getSourceFhirServer().isBlank()) {
      return false;
    }
    if (!configuration.isSaveToFileSystem() || configuration.getTargetFhirServer().isBlank()) {
      return false;
    }
    return true;
  }
}
