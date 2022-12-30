package de.samply.transfair.mappings;

import de.samply.transfair.converters.BbmriBeaconAddressConverter;
import de.samply.transfair.converters.BbmriBeaconSexConverter;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.models.beacon.BeaconIndividual;
import de.samply.transfair.models.beacon.BeaconIndividuals;
import de.samply.transfair.models.beacon.BeaconMeasure;
import de.samply.transfair.models.beacon.BeaconMeasures;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
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

    // This is just here to help with debugging.
    log.info("Wait before starting");
    try {
      Thread.sleep(20000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    fhirComponent.getFhirExportInterface();

    log.info("Setup complete");

    HashSet<String> patientRefs =
        fhirComponent.transferController.getSpecimenPatients(fhirComponent.getSourceFhirServer());

    log.info("Loaded " + patientRefs.size() + " Patients");

    BeaconIndividuals beaconIndividuals = new BeaconIndividuals();

    for (String pid : patientRefs) {
      log.info("Loading data for patient " + pid);

      // Collect data about individuals.
      Patient patient = fhirComponent.transferController.fetchPatientResource(
              fhirComponent.getSourceFhirServer(), pid);
      BeaconIndividual beaconIndividual = new BeaconIndividual();
      beaconIndividual.id = pid;
      Enumerations.AdministrativeGender gender = patient.getGender();
      String bbmriGender = patient.getGender().getDisplay();
      beaconIndividual.sex = BbmriBeaconSexConverter.fromBbmriToBeacon(bbmriGender);
      List<Address> addresses = patient.getAddress();
      if (addresses != null && addresses.size() > 0) {
        Address address = addresses.get(0);
        String country = address.getCountry();
        if (country == null) {
          List<StringType> lines = address.getLine();
          if (lines != null && lines.size() > 0) {
            country = lines.get(lines.size() - 1).asStringValue();
          }
        }
        log.info("country=" + country);
        beaconIndividual.geographicOrigin = BbmriBeaconAddressConverter.fromBbmriToBeacon(country);
      }
      List<IBaseResource> observations = fhirComponent.transferController.fetchPatientObservation(
              fhirComponent.getSourceFhirServer(), pid);
      BeaconMeasures beaconMeasures = new BeaconMeasures();
      for (IBaseResource o : observations) {
        log.info("Got an Observation");
        Observation observation = (Observation) o;
        String id = observation.getId();
        String effectiveDateTime = null;
        if (observation.getEffective() == null) {
          log.warn("getEffective returns null for observation " + id);
        } else {
          effectiveDateTime = observation.getEffective().toString().substring(13, 23);
        }
        List<Coding> codings = observation.getCode().getCoding();
        if (codings.size() < 1) {
          log.warn("Observation has no codings " + id);
          continue;
        }
        String code = codings.get(0).getCode();
        if (code == null) {
          log.warn("Observation has null coding " + id);
          continue;
        }
        if (code.equals("39156-5")) {
          log.info("Observation is BMI");
          double value = observation.getValueQuantity().getValue().doubleValue();
          beaconMeasures.add(BeaconMeasure.makeBmiMeasure(value, effectiveDateTime));
        }
        if (code.equals("29463-7")) {
          log.info("Observation is Weight");
          double value = observation.getValueQuantity().getValue().doubleValue();
          beaconMeasures.add(BeaconMeasure.makeWeightMeasure(value, effectiveDateTime));
        }
        if (code.equals("8302-2")) {
          log.info("Observation is Height");
          double value = observation.getValueQuantity().getValue().doubleValue();
          beaconMeasures.add(BeaconMeasure.makeHeightMeasure(value, effectiveDateTime));
        }
        log.info("Finished an Observation");
      }
      beaconIndividual.measures = beaconMeasures;
      beaconIndividuals.add(beaconIndividual);
    }

    // Export all collected data.
    beaconIndividuals.export();
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
