package de.samply.transfair.mappings.beacon;

import de.samply.transfair.converters.BbmriBeaconAddressConverter;
import de.samply.transfair.converters.BbmriBeaconSexConverter;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.models.beacon.BeaconGeographicOrigin;
import de.samply.transfair.models.beacon.BeaconIndividual;
import de.samply.transfair.models.beacon.BeaconIndividuals;
import de.samply.transfair.models.beacon.BeaconMeasure;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

/**
 * This mapping transfers patient-related data from one blaze with bbmri to an
 * individuals.json file.
 */
@Slf4j
public class Bbmri2BeaconIndividual {
  private FhirComponent fhirComponent;
  private BeaconIndividuals beaconIndividuals;

  public Bbmri2BeaconIndividual(FhirComponent fhirComponent) {
    this.fhirComponent = fhirComponent;
  }

  /**
   * Transfer data relating to patients from FHIR to Beacon.
   */
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
  public BeaconIndividual transferIndividual(String pid) {
    log.info("Loading data for patient " + pid);

    Patient patient = fhirComponent.transferController.fetchPatientResource(
            fhirComponent.getSourceFhirServer(), pid);

    log.info("Load Observations for patient " + pid);
    List<IBaseResource> observations = fhirComponent.transferController.fetchPatientObservation(
            fhirComponent.getSourceFhirServer(), pid);

    return transferIndividual(patient, observations);
  }

  /**
   * Create an object encapsulating a Beacon individual, based on a patient in the FHIR store.
   *
   * @param patient Patient in FHIR store.
   * @return Beacon individual.
   */
  public BeaconIndividual transferIndividual(Patient patient, List<IBaseResource> observations) {
    String pid = transferId(patient);
    BeaconIndividual beaconIndividual = new BeaconIndividual();
    beaconIndividual.id = pid;
    Enumerations.AdministrativeGender gender = patient.getGender();
    String bbmriGender = patient.getGender().getDisplay();
    beaconIndividual.sex = BbmriBeaconSexConverter.fromBbmriToBeacon(bbmriGender);
    beaconIndividual.geographicOrigin = transferAddress(patient);
    beaconIndividual.measures = transferObservations(observations);

    return beaconIndividual;
  }

  /**
   * Pulls an ID from the BBMRI Patient.
   *
   * @param patient BBMRI Patient.
   * @return ID.
   */
  private String transferId(Patient patient) {
    String id = patient.getIdPart();
    if (id == null) {
      id = patient.getId();
    }

    return id;
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
   * whereas in FHIR, they are contracted out to the Observation resource. In this method,
   * relevant information is extracted from the Observations associated with
   * a patient and transferred to a set of Beacon measures.
   *
   * @param observations List of Observations for the patient.
   * @return A set of Beacon measures for the patient.
   */
  private List<BeaconMeasure> transferObservations(List<IBaseResource> observations) {
    List<BeaconMeasure> measures = new ArrayList<BeaconMeasure>();
    for (IBaseResource o : observations) {
      BeaconMeasure measure = transferObservation((Observation) o);
      if (measure != null) {
        measures.add(measure);
      }
    }

    return measures;
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
    } else if (code.equals("29463-7")) {
      log.info("Observation is Weight");
      double value = observation.getValueQuantity().getValue().doubleValue();
      beaconMeasure = BeaconMeasure.makeWeightMeasure(value, effectiveDateTime);
    } else if (code.equals("8302-2")) {
      log.info("Observation is Height");
      double value = observation.getValueQuantity().getValue().doubleValue();
      beaconMeasure = BeaconMeasure.makeHeightMeasure(value, effectiveDateTime);
    } else {
      log.warn("Unknown code " + code + " in Observation " + id);
    }

    return beaconMeasure;
  }

  /**
   * Loads patient resources from FHIR store.
   *
   * @return Hash, mapping patient IDs onto Patient resource objects.
   */
  private HashSet<String> loadPatients() {
    HashSet<String> patientRefs =
            fhirComponent.transferController.getSpecimenPatients(
                    fhirComponent.getSourceFhirServer());

    log.info("Loaded " + patientRefs.size() + " Patients");

    return patientRefs;
  }

  /**
   * Export all individuals to a JSON file.
   *
   * @param path Path to the directory where the file should be stored.
   *             Null value is allowed.
   */
  public void export(String path) {
    BeaconFileSaver.export(beaconIndividuals.individuals, path, "individuals.json");
  }
}