package de.samply.transfair.mappings.beacon;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.samply.transfair.models.beacon.BeaconIndividual;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(Lifecycle.PER_CLASS)
public class Bbmri2BeaconIndividualTest {
  public static final String PATIENT_FILENAME_0 = "Patient.bbmri-0.json";
  public static final String OBSERVATION_FILENAME_0 = "Observation.0-bmi.json";
  public static final String OBSERVATION_FILENAME_1 = "Observation.bbmri-0-body-height.json";
  public static final String OBSERVATION_FILENAME_2 = "Observation.bbmri-0-body-weight.json";
  public static final int MEASURE_COUNT_0 = 3;
  public static final String COUNTRY_0 = "Spain";
  public static final String SEX_0 = "male";

  ClassLoader classLoader;
  Patient patient0;
  List<IBaseResource> observations;
  BeaconIndividual beaconIndividual;

  /**
   * Read the given file from resources and return the contents as a string.
   *
   * @param filename Name of resource file.
   * @return String, containing file body.
   */
  private String readResourceFile(String filename) {
    String content = "";
    try {
      InputStream inputStream = classLoader.getResourceAsStream(filename);
      InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader reader = new BufferedReader(isr);
      String line;
      while ((line = reader.readLine()) != null) {
        content += line;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return content;
  }

  @BeforeAll
  void setup() {
    classLoader = getClass().getClassLoader();
    FhirContext ctx = FhirContext.forR4();
    IParser parser = ctx.newJsonParser();

    patient0 = parser.parseResource(Patient.class, readResourceFile(PATIENT_FILENAME_0));
    observations = new ArrayList<IBaseResource>();
    observations.add(parser.parseResource(Observation.class, readResourceFile(OBSERVATION_FILENAME_0)));
    observations.add(parser.parseResource(Observation.class, readResourceFile(OBSERVATION_FILENAME_1)));
    observations.add(parser.parseResource(Observation.class, readResourceFile(OBSERVATION_FILENAME_2)));
    Bbmri2BeaconIndividual bbmri2BeaconIndividual = new Bbmri2BeaconIndividual(null);
    beaconIndividual = bbmri2BeaconIndividual.transferIndividual(patient0, observations);
  }

  @Test
  void bbmri2BeaconIndividualIdExpectOK() {
    assertEquals(beaconIndividual.id, patient0.getIdPart());
  }

  @Test
  void bbmri2BeaconIndividualMeasuresExpectOK() {
    assertEquals(beaconIndividual.measures.size(), MEASURE_COUNT_0);
  }

  @Test
  void bbmri2BeaconIndividualGeographicOriginExpectOK() {
    assertEquals(beaconIndividual.geographicOrigin.label, COUNTRY_0);
  }

  @Test
  void bbmri2BeaconIndividualSexExpectOK() {
    assertEquals(beaconIndividual.sex.label, SEX_0);
  }
}
