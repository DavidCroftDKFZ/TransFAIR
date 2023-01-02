package de.samply.transfair.mappings.beacon;

import com.google.gson.GsonBuilder;
import de.samply.transfair.TempParams;
import de.samply.transfair.converters.BbmriBeaconAddressConverter;
import de.samply.transfair.converters.BbmriBeaconSexConverter;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.models.beacon.*;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * This mapping transfers patient-related data from one blaze with bbmri to an individuals.json file.
 */
@Slf4j
public class Bbmri2BeaconIndividual {
  private FhirComponent fhirComponent;
  private BeaconIndividuals beaconIndividuals;

  public Bbmri2BeaconIndividual(FhirComponent fhirComponent) {
    this.fhirComponent = fhirComponent;
  }

  public void transfer() {
    HashSet<String> patientRefs = loadPatients();
    beaconIndividuals = new BeaconIndividuals();

    for (String pid : patientRefs) {
      beaconIndividuals.add(transferIndividual(pid));
    }
  }

  /**
   * Create an object encapsulating a Beacon individual, based on a patient in the FHIR store.
   *
   * @param pid ID of a patient in FHIR store.
   * @return Beacon individual.
   */
  private BeaconIndividual transferIndividual(String pid) {
    log.info("Loading data for patient " + pid);

    Patient patient = fhirComponent.transferController.fetchPatientResource(
            fhirComponent.getSourceFhirServer(), pid);
    BeaconIndividual beaconIndividual = new BeaconIndividual();
    beaconIndividual.id = pid;
    Enumerations.AdministrativeGender gender = patient.getGender();
    String bbmriGender = patient.getGender().getDisplay();
    beaconIndividual.sex = BbmriBeaconSexConverter.fromBbmriToBeacon(bbmriGender);
    beaconIndividual.geographicOrigin = transferAddress(patient);
    beaconIndividual.measures = transferObservations(pid);

    return beaconIndividual;
  }

  /**
   * Takes the country name from a Patient resource and creates a geographic origin
   * object suitable for use in Beacon.
   *
   * @param patient FHIR Patient resource.
   * @return Beacon geographic origin.
   */
  private BeaconGeographicOrigin transferAddress(Patient patient) {
    List<Address> addresses = patient.getAddress();
    if (addresses != null && addresses.size() > 0) {
      Address address = addresses.get(0);
      String country = address.getCountry(); // ideally, this should return a non-null value
      if (country == null) {
        List<StringType> lines = address.getLine();
        if (lines != null && lines.size() > 0) {
          // Assume that the last line of the address is a country.
          country = lines.get(lines.size() - 1).asStringValue();
        }
      }
      log.info("country=" + country);

      return BbmriBeaconAddressConverter.fromBbmriToBeacon(country);
    }

    return null;
  }

  /**
   * In Beacon, "measures" (such as body weight) are considered to be part of an individual,
   * whereas in FHIR, they are contracted out to the Observation resource.
   *
   * In this method, relevant information is extracted from the Observations associated with
   * a patient and transferred to a set of Beacon measures.
   *
   * @param pid The ID of the patient.
   * @return A set of Beacon measures for the patient.
   */
  private BeaconMeasures transferObservations(String pid) {
    log.info("Load Observations for patient " + pid);
    List<IBaseResource> observations = fhirComponent.transferController.fetchPatientObservation(
            fhirComponent.getSourceFhirServer(), pid);
    BeaconMeasures beaconMeasures = new BeaconMeasures();
    for (IBaseResource o : observations) {
      beaconMeasures.add(transferObservation((Observation) o));
    }

    return beaconMeasures;
  }

  /**
   * Transfer the information in the given FHIR Observation to a Beacon measure.
   *
   * @param observation FHIR Observation resource.
   * @return Beacon measure.
   */
  private BeaconMeasure transferObservation(Observation observation) {
    String id = observation.getId();
    String effectiveDateTime = null;
    if (observation.getEffective() == null) {
      log.warn("getEffective returns null for observation " + id);
    } else {
      effectiveDateTime = observation.getEffective().toString().substring(13, 23);
    }
    List<Coding> codings = observation.getCode().getCoding();
    if (codings == null || codings.size() < 1) {
      log.warn("No codings in Observation " + id);
      return null;
    }
    String code = codings.get(0).getCode();
    if (code == null) {
      log.warn("Null 0th coding in Observation " + id);
      return null;
    }
    BeaconMeasure beaconMeasure = null;
    if (code.equals("39156-5")) {
      log.info("Observation is BMI");
      double value = observation.getValueQuantity().getValue().doubleValue();
      beaconMeasure = BeaconMeasure.makeBmiMeasure(value, effectiveDateTime);
    }
    else if (code.equals("29463-7")) {
      log.info("Observation is Weight");
      double value = observation.getValueQuantity().getValue().doubleValue();
      beaconMeasure = BeaconMeasure.makeWeightMeasure(value, effectiveDateTime);
    }
    else if (code.equals("8302-2")) {
      log.info("Observation is Height");
      double value = observation.getValueQuantity().getValue().doubleValue();
      beaconMeasure = BeaconMeasure.makeHeightMeasure(value, effectiveDateTime);
    } else
      log.warn("Unknown code " + code + " in Observation " + id);

    return beaconMeasure;
  }

  /**
   * Loads patient resources from FHIR store.
   *
   * @return Hash, mapping patient IDs onto Patient resource objects.
   */
  private HashSet<String> loadPatients() {
    HashSet<String> patientRefs =
            fhirComponent.transferController.getSpecimenPatients(fhirComponent.getSourceFhirServer());

    log.info("Loaded " + patientRefs.size() + " Patients");

    return patientRefs;
  }

  /**
   * Export all individuals to a JSON file.
   */
  public void export() {
    String filename = "individuals.json";
    String path = TempParams.getSaveToFilePath();
    String filepath = path + "/" + filename;
    log.info("export: filepath=" + filepath);
    try {
      FileWriter myWriter = new FileWriter(filepath);
      String output = new GsonBuilder().setPrettyPrinting().create().toJson(beaconIndividuals.individuals);
      myWriter.write(output);
      myWriter.close();
    } catch (IOException e) {
      log.error("An error occurred while writing output to file " + filepath);
      e.printStackTrace();
    }
  }
}