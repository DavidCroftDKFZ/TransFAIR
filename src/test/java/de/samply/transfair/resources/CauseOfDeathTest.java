package de.samply.transfair.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;

public class CauseOfDeathTest {

  @Test
  void convertFromBbmriToMii() {
    CauseOfDeath causeOfDeath = new CauseOfDeath();
    Observation observation = new Observation();
    observation.setCode(new CodeableConcept().addCoding(new Coding().setCode("68343-3")));
    observation.setValue(new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/sid/icd-10", "D49.0", "")));

    causeOfDeath.fromBbmri(observation);
    org.hl7.fhir.r4.model.Condition miiCauseOfDeath = causeOfDeath.toMii();

    assertEquals("254604005", miiCauseOfDeath.getCategoryFirstRep().getCodingFirstRep().getCode());
  }

  @Test
  void convertFromMiiToMii() {
    CauseOfDeath causeOfDeath = new CauseOfDeath();
    Condition condition = new Condition();
    condition.getCategoryFirstRep().getCodingFirstRep().setCode("254604005");

    causeOfDeath.fromMii(condition);
    org.hl7.fhir.r4.model.Condition miiCauseOfDeath = causeOfDeath.toMii();
    assertEquals(condition.getCategoryFirstRep().getCodingFirstRep().getCode(), miiCauseOfDeath.getCategoryFirstRep().getCodingFirstRep().getCode());
  }

  @Test
  void convertFromBbmriToBbmri() {
    CauseOfDeath causeOfDeath = new CauseOfDeath();
    Observation observation = new Observation();
    observation.setCode(new CodeableConcept().addCoding(new Coding().setCode("68343-3")));
    observation.setValue(new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/sid/icd-10", "D49.0", "")));

    causeOfDeath.fromBbmri(observation);
    org.hl7.fhir.r4.model.Observation bbmriObservation = causeOfDeath.toBbmri();

    assertEquals(observation.getValueCodeableConcept().getCodingFirstRep().getCode(), bbmriObservation.getValueCodeableConcept().getCodingFirstRep().getCode());
  }

  @Test
  void convertFromMiiToBbmri() {
    CauseOfDeath causeOfDeath = new CauseOfDeath();
    Condition condition = new Condition();
    condition.getCategoryFirstRep().getCodingFirstRep().setCode("254604005");


    causeOfDeath.fromMii(condition);
    org.hl7.fhir.r4.model.Observation observation = causeOfDeath.toBbmri();

    assertEquals("D49.0", observation.getValueCodeableConcept().getCodingFirstRep().getCode());
  }
}