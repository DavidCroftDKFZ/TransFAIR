package de.samply.transfair.resources;

import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ca.uhn.fhir.context.FhirContext;

@TestInstance(Lifecycle.PER_CLASS)
public class ConditionMappingTest {

  // BBMRI.DE resource
  Condition conditionBbmri;

  // MII resource
  Condition conditionMii;

  ca.uhn.fhir.parser.IParser parser;

  @BeforeEach
  void setup() {
    parser = FhirContext.forR4().newJsonParser();

    Patient patient = new Patient();
    patient.setId("patientId");
    DateType date = new DateType(new Date());

    conditionBbmri = new Condition();
    conditionBbmri.setId("conditionId");
    conditionBbmri.setOnset(date);
    conditionBbmri.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Condition")));
    conditionBbmri.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptBbmri = new CodeableConcept();
    codeableConceptBbmri.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C61");
    conditionBbmri.setCode(codeableConceptBbmri);

    conditionMii = new Condition();
    conditionMii.setId("conditionId");
    conditionMii.setOnset(date);
    conditionMii.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose")));
    conditionBbmri.setSubject(new Reference().setReference(patient.getId()));
    CodeableConcept codeableConceptMii = new CodeableConcept();
    codeableConceptMii.getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode("C61");
    conditionMii.setCode(codeableConceptMii);
  }






}


