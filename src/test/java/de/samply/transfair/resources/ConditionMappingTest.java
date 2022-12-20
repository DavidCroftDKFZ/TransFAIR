package de.samply.transfair.resources;

import java.util.List;
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
import static de.samply.transfair.util.JsonUtils.*;

@TestInstance(Lifecycle.PER_CLASS)
public class ConditionMappingTest {

  // BBMRI.DE resources
  Condition conditionBbmriForComparingICD10GM, 
    conditionBbmriForConvertingICD10, 
    conditionBbmriForConvertingICD10GM, 
    conditionBbmriForConvertingICD9, 
    conditionMiiSnomed, 
    conditionBbmriForComparingICD10;

  // MII resource
  Condition conditionMiiICD10GM;

  ConditionMapping conditionMapping;
  
  @BeforeEach
  void setup() {
    
    conditionMapping = new ConditionMapping();

    Patient patient = new Patient();
    patient.setId("patientId");
    DateTimeType onset = new DateTimeType();
    onset.setValueAsString("1997-10-22T00:00:00+02:00");

    conditionBbmriForConvertingICD10 = new Condition();
    conditionBbmriForConvertingICD10.setId("conditionId");
    conditionBbmriForConvertingICD10.setOnset(onset);
    conditionBbmriForConvertingICD10.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    conditionBbmriForConvertingICD10.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmriForConverting = new CodeableConcept();
    codeableConceptBbmriForConverting.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C61");
    conditionBbmriForConvertingICD10.setCode(codeableConceptBbmriForConverting);

    conditionBbmriForConvertingICD10GM = new Condition();
    conditionBbmriForConvertingICD10GM.setId("conditionId");
    conditionBbmriForConvertingICD10GM.setOnset(onset);
    conditionBbmriForConvertingICD10GM.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    conditionBbmriForConvertingICD10GM.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmriForConvertingGM = new CodeableConcept();
    codeableConceptBbmriForConvertingGM.getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode("C61");
    conditionBbmriForConvertingICD10GM.setCode(codeableConceptBbmriForConvertingGM);

    conditionBbmriForConvertingICD9 = new Condition();
    conditionBbmriForConvertingICD9.setId("conditionId");
    conditionBbmriForConvertingICD9.setOnset(onset);
    conditionBbmriForConvertingICD9.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    conditionBbmriForConvertingICD9.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmriForConverting9 = new CodeableConcept();
    codeableConceptBbmriForConverting9.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-9").setCode("C61");
    conditionBbmriForConvertingICD9.setCode(codeableConceptBbmriForConverting9);

    conditionBbmriForComparingICD10GM = new Condition();
    conditionBbmriForComparingICD10GM.setId("conditionId");
    conditionBbmriForComparingICD10GM.setOnset(onset);
    conditionBbmriForComparingICD10GM.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    // TODO uncomment when implemented
    // conditionBbmriForComparingICD10GM.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmriForComparingICD10GM = new CodeableConcept();
    codeableConceptBbmriForComparingICD10GM.getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode("C61");
    conditionBbmriForComparingICD10GM.setCode(codeableConceptBbmriForComparingICD10GM);

    conditionBbmriForComparingICD10 = new Condition();
    conditionBbmriForComparingICD10.setId("conditionId");
    conditionBbmriForComparingICD10.setOnset(onset);
    conditionBbmriForComparingICD10.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    // TODO uncomment when implemented
    // conditionBbmriForComparingICD10.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmriForComparingICD10 = new CodeableConcept();
    codeableConceptBbmriForComparingICD10.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C61");
    conditionBbmriForComparingICD10.setCode(codeableConceptBbmriForComparingICD10);

    conditionMiiICD10GM = new Condition();
    conditionMiiICD10GM.setId("conditionId");
    conditionMiiICD10GM.setOnset(onset);
    conditionMiiICD10GM.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose")));
    conditionMiiICD10GM.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptMiiICD10GM = new CodeableConcept();
    codeableConceptMiiICD10GM.getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode("C61");
    conditionMiiICD10GM.setCode(codeableConceptMiiICD10GM);

    conditionMiiSnomed = new Condition();
    conditionMiiSnomed.setId("conditionId");
    conditionMiiSnomed.setOnset(onset);
    conditionMiiSnomed.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose")));
    conditionMiiSnomed.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptMiiSnomed = new CodeableConcept();
    codeableConceptMiiSnomed.getCodingFirstRep().setSystem("http://snomed.info/sct").setCode("399068003");
    conditionMiiSnomed.setCode(codeableConceptMiiSnomed);
  }

  @Test
  void fromBbmriICD10ToMiiExpectOK() {

    Condition condition2Mii = new Condition();

    conditionMapping.fromBbmri(conditionBbmriForConvertingICD10);
    condition2Mii = conditionMapping.toMii();

    compareFhirObjects(condition2Mii, conditionMiiICD10GM);
  }

  @Test
  void fromBbmriICD10GMToMiiExpectOK() {

    Condition condition2Mii = new Condition();

    conditionMapping.fromBbmri(conditionBbmriForConvertingICD10GM);
    condition2Mii = conditionMapping.toMii();

    compareFhirObjects(condition2Mii, conditionMiiICD10GM);
  }

  @Test
  void fromBbmriICD9ToMiiExpectOK() {

    Condition condition2Mii = new Condition();

    conditionMapping.fromBbmri(conditionBbmriForConvertingICD9);
    condition2Mii = conditionMapping.toMii();


    compareFhirObjects(condition2Mii, conditionMiiICD10GM);
  }

  @Test
  void fromMiiICD10GMToBbmriExpectOK() {

    Condition condition2Bbmri = new Condition();

    conditionMapping.fromMii(conditionMiiICD10GM);
    condition2Bbmri = conditionMapping.toBbmri();


    compareFhirObjects(condition2Bbmri, conditionBbmriForComparingICD10GM);
  }

  @Test
  void fromMiiSnomedToBbmriExpectOK() {

    Condition condition2Bbmri = new Condition();

    conditionMapping.fromMii(conditionMiiSnomed);
    condition2Bbmri = conditionMapping.toBbmri();


    compareFhirObjects(condition2Bbmri, conditionBbmriForComparingICD10);
  }







}


