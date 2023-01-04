package de.samply.transfair.mappings.beacon;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.samply.transfair.models.beacon.BeaconBiosample;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(Lifecycle.PER_CLASS)
public class Bbmri2BeaconBiosampleTest {
  public static final String SPECIMEN_FILENAME_0 = "Specimen.bbmri-0-specimen-0.json";
  public static final String TAX_ID_0 = "9606";

  ClassLoader classLoader;
  Specimen specimen0;
  BeaconBiosample beaconBiosample;

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

    specimen0 = parser.parseResource(Specimen.class, readResourceFile(SPECIMEN_FILENAME_0));
    Bbmri2BeaconBiosamples bbmri2BeaconBiosamples = new Bbmri2BeaconBiosamples(null);
    beaconBiosample = bbmri2BeaconBiosamples.transferBiosample(specimen0);
  }

  @Test
  void bbmri2BeaconBiosampleIdExpectOK() {
    assertEquals(beaconBiosample.id, specimen0.getIdPart());
  }

  @Test
  void bbmri2BeaconBiosampleCollectionDateExpectOK() {
    assertEquals(beaconBiosample.collectionDate, specimen0.getCollection().getCollectedDateTimeType().getValueAsString());
  }

  @Test
  void bbmri2BeaconBiosampleIndividualIdExpectOK() {
    assertNotNull(beaconBiosample.individualId);
  }

  @Test
  void bbmri2BeaconBiosampleInfoExpectOK() {
    assertEquals(beaconBiosample.info.taxId, TAX_ID_0);
  }

  @Test
  void bbmri2BeaconBiosampleSampleOriginTypeExpectOK() {
    assertNotNull(beaconBiosample.sampleOriginType.id);
  }
}
