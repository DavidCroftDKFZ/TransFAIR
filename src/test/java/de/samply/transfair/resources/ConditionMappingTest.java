package de.samply.transfair.resources;

import java.util.List;
import java.util.Objects;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ca.uhn.fhir.context.FhirContext;

@TestInstance(Lifecycle.PER_CLASS)
public class ConditionMappingTest {

  // BBMRI.DE resources
  Condition conditionBbmriForComparing, conditionBbmriForConverting;

  // MII resource
  Condition conditionMii;

  ConditionMapping conditionMapping = new ConditionMapping();

  ca.uhn.fhir.parser.IParser parser;

  @BeforeEach
  void setup() {
    parser = FhirContext.forR4().newJsonParser();

    Patient patient = new Patient();
    patient.setId("patientId");
    DateTimeType onset = new DateTimeType();
    onset.setValueAsString("1997-10-22T00:00:00+02:00");

    conditionBbmriForConverting = new Condition();
    conditionBbmriForConverting.setId("conditionId");
    conditionBbmriForConverting.setOnset(onset);
    conditionBbmriForConverting.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    conditionBbmriForConverting.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmriForConverting = new CodeableConcept();
    codeableConceptBbmriForConverting.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C61");
    conditionBbmriForConverting.setCode(codeableConceptBbmriForConverting);
    
    conditionBbmriForComparing = new Condition();
    conditionBbmriForComparing.setId("conditionId");
    conditionBbmriForComparing.setOnset(onset);
    conditionBbmriForComparing.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    // TODO uncomment when implemented
    // conditionBbmriForComparing.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmriForComparing = new CodeableConcept();
    codeableConceptBbmriForComparing.getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode("C61");
    conditionBbmriForComparing.setCode(codeableConceptBbmriForComparing);

    conditionMii = new Condition();
    conditionMii.setId("conditionId");
    conditionMii.setOnset(onset);
    conditionMii.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose")));
    conditionMii.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptMii = new CodeableConcept();
    codeableConceptMii.getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode("C61");
    conditionMii.setCode(codeableConceptMii);
  }

  @Test
  void fromBbmriToMiiExpectOK() {

    Condition condition2Mii = new Condition();

    conditionMapping.fromBbmri(conditionBbmriForConverting);
    condition2Mii = conditionMapping.toMii();


    compareFhirObjects(condition2Mii, conditionMii);
  }

  @Test
  void fromMiiToBbmriExpectOK() {

    Condition condition2Bbmri = new Condition();

    conditionMapping.fromMii(conditionMii);
    condition2Bbmri = conditionMapping.toBbmri();


    compareFhirObjects(condition2Bbmri, conditionBbmriForComparing);
  }


  void compareFhirObjects(IBaseResource a, IBaseResource b) {
    String actualAsJson = parser.encodeResourceToString(a);
    String expectedAsJson = parser.encodeResourceToString(b);
    assert(Objects.equals(expectedAsJson, actualAsJson));
  }




}


