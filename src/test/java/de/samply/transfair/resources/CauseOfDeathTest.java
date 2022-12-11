package de.samply.transfair.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;

public class CauseOfDeathTest {

  // BBMRI.DE resource
  private final String observation_string_source;
  private final String observation_string_target;

  // MII resource
  private final String condition_string_source;
  private final String condition_string_target;

  ca.uhn.fhir.parser.IParser parser;

  public CauseOfDeathTest(){
    FhirContext ctx = FhirContext.forR4();
    this.parser = ctx.newXmlParser();

    //TODO: Both should contain the same information as they will be converted into each other

    // BBMRI.DE resource
    this.observation_string_source = "<Observation><id value=\"bbmri-10002-cause-of-death\"/><meta><versionId value=\"1009\"/><lastUpdated value=\"2022-12-07T08:09:29.234Z\"/><profile value=\"https://fhir.bbmri.de/StructureDefinition/CauseOfDeath\"/></meta><status value=\"final\"/><code><coding><system value=\"http://loinc.org\"/><code value=\"68343-3\"/></coding></code><subject><reference value=\"Patient/bbmri-10002\"/></subject><valueCodeableConcept><coding><system value=\"http://hl7.org/fhir/sid/icd-10\"/><code value=\"D55.2\"/></coding></valueCodeableConcept></Observation>";
    this.observation_string_target = "";

    // MII resource
    this.condition_string_source = "<Condition><id value=\"bbmri-10002-cause-of-death\"/><meta><versionId value=\"1009\"/><lastUpdated value=\"2022-12-07T08:09:29.234Z\"/><profile value=\"https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache\"/></meta><status value=\"final\"/><category><coding><system value=\"http://snomed.info/sct\" /><code value=\"16100001\" /></coding><coding><system value=\"http://loinc.org\" /><code value=\"79378-6\" /></coding></category><code><coding><system value=\"http://hl7.org/fhir/sid/icd-10\"/><code value=\"D55.2\"/></coding></code><subject><reference value=\"Patient/bbmri-10002\"/></subject></Condition>";
    this.condition_string_target = "<Condition xmlns=\"http://hl7.org/fhir\"><meta><profile value=\"https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache\"></profile></meta><category><coding><system value=\"http://loinc.org\"></system><code value=\"79378-6\"></code></coding></category><category><coding><system value=\"http://snomed.info/sct\"></system><code value=\"16100001\"></code></coding></category><code><coding><system value=\"http://fhir.de/CodeSystem/bfarm/icd-10-gm\"></system><code value=\"D55.2\"></code></coding></code></Condition>";
    //https://simplifier.net/MedizininformatikInitiative-ModulDiagnosen/Diagnose/~overview
  }

  @Test
  void convertFromBbmriToMii() {
    Observation original_observation = parser.parseResource(Observation.class, this.observation_string_source);

    CauseOfDeath causeOfDeath = new CauseOfDeath();
    causeOfDeath.fromBbmri(original_observation);
    Condition after_conversion = causeOfDeath.toMii();

    String serialized_after_conversion = parser.encodeResourceToString(after_conversion);
    assertEquals(this.condition_string_target, serialized_after_conversion);
  }

  @Test
  void convertFromMiiToMii() {
    Condition original_condition = parser.parseResource(Condition.class, this.condition_string_source);

    CauseOfDeath causeOfDeath = new CauseOfDeath();
    causeOfDeath.fromMii(original_condition);
    Condition after_conversion = causeOfDeath.toMii();

    String serialized_after_conversion = parser.encodeResourceToString(after_conversion);
    assertEquals(this.condition_string_target, serialized_after_conversion);
  }

  @Test
  void convertFromBbmriToBbmri() {
    Observation original_observation = parser.parseResource(Observation.class, this.observation_string_source);

    CauseOfDeath causeOfDeath = new CauseOfDeath();
    causeOfDeath.fromBbmri(original_observation);
    Observation after_conversion = causeOfDeath.toBbmri();

    String serialized_after_conversion = parser.encodeResourceToString(after_conversion);
    assertEquals(this.observation_string_target, serialized_after_conversion);
  }

  @Test
  void convertFromMiiToBbmri() {
    Condition original_condition = parser.parseResource(Condition.class, this.condition_string_source);

    CauseOfDeath causeOfDeath = new CauseOfDeath();
    causeOfDeath.fromMii(original_condition);
    Observation after_conversion = causeOfDeath.toBbmri();

    String serialized_after_conversion = parser.encodeResourceToString(after_conversion);
    assertEquals(this.observation_string_target, serialized_after_conversion);

  }
}
