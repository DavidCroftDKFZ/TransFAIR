package de.samply.transfair.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import de.samply.transfair.resources.CauseOfDeath;
import java.util.List;
import java.util.Objects;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class CauseOfDeathTest {

  // BBMRI.DE resource
  Observation causeOfDeathBbmri;

  // MII resource
  Condition causeOfDeathMII;

  ca.uhn.fhir.parser.IParser parser;

  @BeforeAll
  void setup() {
    parser = FhirContext.forR4().newJsonParser();

    Patient patient = new Patient();
    patient.setId("causeOfDeath");

    causeOfDeathBbmri = new Observation();
    causeOfDeathBbmri.setId("death");
    causeOfDeathBbmri.getMeta().setProfile(List.of(new CanonicalType(
        "https://fhir.bbmri.de/StructureDefinition/CauseOfDeath")));
    causeOfDeathBbmri.getCode().getCodingFirstRep().setSystem("http://loinc.org").setCode("68343-3");
    causeOfDeathBbmri.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C25.0");
    causeOfDeathBbmri.setValue(codeableConcept);

    causeOfDeathMII = new Condition();
    causeOfDeathMII.setId("death");
    causeOfDeathMII.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache")));
    CodeableConcept loinc = new CodeableConcept();
    loinc.getCodingFirstRep().setSystem("http://loinc.org").setCode("79378-6");
    CodeableConcept snomed = new CodeableConcept();
    snomed.getCodingFirstRep().setSystem("http://snomed.info/sct").setCode("16100001");
    causeOfDeathMII.setSubject(new Reference().setReference(patient.getId()));
    causeOfDeathMII.setCategory(List.of(loinc,snomed));
    causeOfDeathMII.getCode().getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C25.0");

  }

  @Test
  void convertFromBbmriToMii() {
    CauseOfDeath causeOfDeath = new CauseOfDeath();

    causeOfDeath.fromBbmri(causeOfDeathBbmri);

    compareFhirObjects(causeOfDeathMII, causeOfDeath.toMii());
  }

  void compareFhirObjects(IBaseResource a, IBaseResource b) {
    String actualAsJson = parser.encodeResourceToString(a);
    String expectedAsJson = parser.encodeResourceToString(b);
    assert(Objects.equals(expectedAsJson,actualAsJson));
  }
  @Test
  void convertFromBbmriToBbmri() {
    CauseOfDeath causeOfDeath = new CauseOfDeath();

    causeOfDeath.fromBbmri(causeOfDeathBbmri);

    compareFhirObjects(causeOfDeathBbmri,causeOfDeath.toBbmri());
  }

  @Test
  void convertFromMiiToBbmri() {
    CauseOfDeath causeOfDeath = new CauseOfDeath();

    causeOfDeath.fromMii(causeOfDeathMII);

    compareFhirObjects(causeOfDeathBbmri,causeOfDeath.toBbmri());
  }
}


