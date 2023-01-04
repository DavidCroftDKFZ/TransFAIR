package de.samply.transfair.mappings;

import de.samply.transfair.Configuration;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.mappings.beacon.Bbmri2BeaconBiosamples;
import de.samply.transfair.mappings.beacon.Bbmri2BeaconIndividual;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This mapping transfers everything from one blaze with bbmri to Beacon BFF files.
 */
@Component
@Slf4j
public class Bbmri2Beacon extends FhirMappings {

  @Autowired FhirComponent fhirComponent;
  @Autowired public Configuration configuration;

  private List<String> resources;

  /** Transferring. */
  public void transfer() {
    log.info("Running TransFAIR in BBMRI2BEACON mode");
    if (!this.setup()) {
      log.info("Variables are not set, transfer not possible");
      return;
    }

    String path = configuration.getBeaconPath();

    Bbmri2BeaconIndividual bbmri2BeaconIndividual = new Bbmri2BeaconIndividual(fhirComponent);
    bbmri2BeaconIndividual.transfer();
    bbmri2BeaconIndividual.export(path);

    Bbmri2BeaconBiosamples bbmri2BeaconBiosamples = new Bbmri2BeaconBiosamples(fhirComponent);
    bbmri2BeaconBiosamples.transfer();
    bbmri2BeaconBiosamples.export(path);
  }

  private Boolean setup() {

    if (fhirComponent.configuration.getSourceFhirServer().isBlank()) {
      return false;
    }

    return true;
  }
}
