package de.samply.transfair.mappings.beacon;

import de.samply.transfair.converters.BbmriBeaconTypeConverter;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.models.beacon.BeaconBiosample;
import de.samply.transfair.models.beacon.BeaconBiosamples;
import de.samply.transfair.models.beacon.BeaconSampleInfo;
import de.samply.transfair.models.beacon.BeaconSampleOriginType;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;

/**
 * This mapping transfers sample-related data from one blaze with bbmri to a biosamples.json file.
 */
@Slf4j
public class Bbmri2BeaconBiosamples {
  private FhirComponent fhirComponent;
  private BeaconBiosamples beaconBiosamples;

  public Bbmri2BeaconBiosamples(FhirComponent fhirComponent) {
    this.fhirComponent = fhirComponent;
  }

  /**
   * Transfer data relating to samples from FHIR to Beacon.
   */
  public void transfer() {
    HashSet<String> specimenRefs = loadSpecimens();
    beaconBiosamples = new BeaconBiosamples();

    for (String sid : specimenRefs) {
      beaconBiosamples.add(transferBiosample(sid));
    }
  }

  /**
   * Create an object encapsulating a Beacon biosample, based on a Specimen in the FHIR store.
   *
   * @param sid ID of a specimen in FHIR store.
   * @return Beacon biosample.
   */
  public BeaconBiosample transferBiosample(String sid) {
    log.info("Loading data for sample " + sid);

    Specimen specimen = fhirComponent.transferController.fetchSpecimenResource(
            fhirComponent.getSourceFhirServer(), sid);

    return transferBiosample(specimen);
  }

  /**
   * Create an object encapsulating a Beacon biosample, based on a Specimen in the FHIR store.
   *
   * @param specimen ID of a specimen in FHIR store.
   * @return Beacon biosample.
   */
  public BeaconBiosample transferBiosample(Specimen specimen) {
    BeaconBiosample beaconBiosample = new BeaconBiosample();
    beaconBiosample.id = transferId(specimen);
    beaconBiosample.individualId = transferPatientId(specimen);
    beaconBiosample.collectionDate = transferCollectionDate(specimen);
    beaconBiosample.info = transferInfo(specimen);
    beaconBiosample.sampleOriginType = transferType(specimen);

    return beaconBiosample;
  }

  /**
   * Pulls an ID from the BBMRI Specimen.
   *
   * @param specimen BBMRI Specimen.
   * @return ID.
   */
  private String transferId(Specimen specimen) {
    String id = specimen.getIdPart();
    if (id == null) {
      id = specimen.getId();
    }

    return id;
  }

  /**
   * Returns the ID of the patient from whom the specimen was taken.
   *
   * @param specimen BBMRI Specimen.
   * @return Patient ID.
   */
  private String transferPatientId(Specimen specimen) {
    Reference subject = specimen.getSubject();
    String patientId = subject.getReference().toString();

    return patientId.substring(8);
  }

  /**
   * Returns the date at which the specimen was collected.
   *
   * @param specimen BBMRI Specimen.
   * @return Collectin date.
   */
  private String transferCollectionDate(Specimen specimen) {
    String date = specimen.getCollection().getCollectedDateTimeType().getValueAsString();

    return date;
  }

  /**
   * Returns the "info" object required by Beacon. This contains mainly information
   * about the species of the subject. I.e. H. sapiens.
   *
   * @param specimen BBMRI Specimen.
   * @return Beacin info object.
   */
  private BeaconSampleInfo transferInfo(Specimen specimen) {
    BeaconSampleInfo beaconSampleInfo = BeaconSampleInfo.createHumanBeaconSampleInfo();

    return beaconSampleInfo;
  }

  /**
   * Returns the type of the sample, e.g. blood.
   *
   * @param specimen BBMRI Specimen.
   * @return Sample type.
   */
  private BeaconSampleOriginType transferType(Specimen specimen) {
    CodeableConcept type = specimen.getType();
    if (type == null) {
      log.warn("No sample type available for: " + specimen.getId());
      return null;
    }
    List<Coding> codings = type.getCoding();
    if (codings == null || codings.size() < 1) {
      log.warn("No sample codings available for: " + specimen.getId());
      return null;
    }
    String code = codings.get(0).getCode();
    BeaconSampleOriginType beaconSampleOriginType =
            BbmriBeaconTypeConverter.fromBbmriToBeacon(code);

    return beaconSampleOriginType;
  }

  /**
   * Loads patient resources from FHIR store.
   *
   * @return Hash, mapping patient IDs onto Patient resource objects.
   */
  private HashSet<String> loadSpecimens() {
    HashSet<String> specimenRefs =
            fhirComponent.transferController.getSpecimenIds(fhirComponent.getSourceFhirServer());

    log.info("Loaded " + specimenRefs.size() + " Specimens");

    return specimenRefs;
  }

  /**
   * Export all specimens to a JSON file.
   *
   * @param path Path to the directory where the file should be stored.
   *             Null value is allowed.
   */
  public void export(String path) {
    BeaconFileSaver.export(beaconBiosamples.biosamples, path, "biosamples.json");
  }
}