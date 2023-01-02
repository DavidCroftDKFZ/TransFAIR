package de.samply.transfair.mappings.beacon;

import com.google.gson.GsonBuilder;
import de.samply.transfair.TempParams;
import de.samply.transfair.converters.BbmriBeaconTypeConverter;
import de.samply.transfair.fhir.FhirComponent;
import de.samply.transfair.models.beacon.*;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

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
  private BeaconBiosample transferBiosample(String sid) {
    log.info("Loading data for sample " + sid);

    Specimen specimen = fhirComponent.transferController.fetchSpecimenResource(
            fhirComponent.getSourceFhirServer(), sid);
    BeaconBiosample beaconBiosample = new BeaconBiosample();
    beaconBiosample.id = transferId(specimen);
    beaconBiosample.individualId = transferPatientId(specimen);
    beaconBiosample.collectionDate = transferCollectionDate(specimen);
    beaconBiosample.info = transferInfo(specimen);
    beaconBiosample.sampleOriginType = transferType(specimen);

    return beaconBiosample;
  }

  private String transferId(Specimen specimen) {
    String id = specimen.getIdPart();
    if (id == null)
      id = specimen.getId();

    return id;
  }

  private String transferPatientId(Specimen specimen) {
    Reference subject = specimen.getSubject();
    String patientId = subject.getReference().toString();

    return patientId.substring(8);
  }

  private String transferCollectionDate(Specimen specimen) {
    String date = specimen.getCollection().getCollectedDateTimeType().getValueAsString();

    return date;
  }

  private BeaconSampleInfo transferInfo(Specimen specimen) {
    BeaconSampleInfo beaconSampleInfo = BeaconSampleInfo.createHumanBeaconSampleInfo();

    return beaconSampleInfo;
  }

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
    BeaconSampleOriginType beaconSampleOriginType = BbmriBeaconTypeConverter.fromBbmriToBeacon(code);

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
   */
  public void export() {
    String filename = "biosamples.json";
    String path = TempParams.getSaveToFilePath();
    String filepath = path + "/" + filename;
    log.info("export: filepath=" + filepath);
    try {
      FileWriter myWriter = new FileWriter(filepath);
      String output = new GsonBuilder().setPrettyPrinting().create().toJson(beaconBiosamples.biosamples);
      myWriter.write(output);
      myWriter.close();
    } catch (IOException e) {
      log.error("An error occurred while writing output to file " + filepath);
      e.printStackTrace();
    }
  }
}