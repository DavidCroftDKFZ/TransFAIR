package de.samply.transfair.resources;

import static de.samply.transfair.JsonUtils.compareFhirObjects;
import java.util.List;
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
  void failConvertionBBMRI() {
    CauseOfDeathMapping causeOfDeathMapping = new CauseOfDeathMapping();

    causeOfDeathMapping.fromBbmri(new Observation());

    compareFhirObjects(new Observation(), causeOfDeathMapping.toBbmri());
  }

  @Test
  void failConvertionMII() {
    CauseOfDeathMapping causeOfDeathMapping = new CauseOfDeathMapping();

    causeOfDeathMapping.fromMii(new Condition());

    compareFhirObjects(new Condition(), causeOfDeathMapping.toMii());
  }

  @Test
  void convertFromBbmriToMii() {
    CauseOfDeathMapping causeOfDeathMapping = new CauseOfDeathMapping();

    causeOfDeathMapping.fromBbmri(causeOfDeathBbmri);

    compareFhirObjects(causeOfDeathMII, causeOfDeathMapping.toMii());
  }

  @Test
  void convertFromBbmriToBbmri() {
    CauseOfDeathMapping causeOfDeathMapping = new CauseOfDeathMapping();

    causeOfDeathMapping.fromBbmri(causeOfDeathBbmri);

    compareFhirObjects(causeOfDeathBbmri, causeOfDeathMapping.toBbmri());
  }

  @Test
  void convertFromMiiToBbmri() {
    CauseOfDeathMapping causeOfDeathMapping = new CauseOfDeathMapping();

    causeOfDeathMapping.fromMii(causeOfDeathMII);

    compareFhirObjects(causeOfDeathBbmri, causeOfDeathMapping.toBbmri());
  }
}


