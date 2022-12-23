package de.samply.transfair.resources;

import static de.samply.transfair.JsonUtils.compareFhirObjects;
import java.util.List;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class SpecimenMappingTest {

  // BBMRI.DE resources
  Specimen specimenBbmriForConverting;
  Specimen specimenBbmriForComparing;

  // MII resource
  Specimen specimenMiiForConverting;
  Specimen specimenMiiForComparing;

  SpecimenMapping specimenMapping;

  @BeforeEach
  void setup() {

    specimenMapping = new SpecimenMapping();

    Patient patient = new Patient();
    patient.setId("patientId");

    specimenBbmriForConverting = new Specimen();
    specimenBbmriForConverting.setId("specimenId");
    specimenBbmriForConverting.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Specimen")));
    specimenBbmriForConverting.setSubject(new Reference().setReference(patient.getId()));

    Extension e = new Extension();
    e.setUrl("https://fhir.bbmri.de/StructureDefinition/SampleDiagnosis");
    CodeableConcept codeableConceptBbmriForConverting = new CodeableConcept();
    codeableConceptBbmriForConverting.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C61");
    e.setValue(codeableConceptBbmriForConverting);
    specimenBbmriForConverting.setExtension(List.of(e));

    specimenMiiForConverting = new Specimen();
    specimenMiiForConverting.setId("specimenId");
    specimenMiiForConverting.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Specimen")));
    specimenMiiForConverting.setSubject(new Reference().setReference(patient.getId()));
    
    CodeableConcept bodySiteCode = new CodeableConcept();
    bodySiteCode.getCodingFirstRep().setCode("8148/2");
    bodySiteCode.getCodingFirstRep().setSystem("urn:oid:1.3.6.1.4.1.19376.1.3.11.36");
    specimenMiiForConverting.getCollection().setBodySite(bodySiteCode);

  }

  @Test
  void fromBbmriToMiiExpectOK() {

    Specimen specimen2Mii = new Specimen();

    specimenMapping.fromBbmri(specimenBbmriForConverting);
    specimen2Mii = specimenMapping.toMii();

    compareFhirObjects(specimen2Mii, specimenMiiForConverting);
  }

  @Test
  void fromMiiToBbmriExpectOK() {

    Specimen specimen2Bbmri = new Specimen();

    specimenMapping.fromMii( specimenMiiForConverting);
    specimen2Bbmri = specimenMapping.toBbmri();


    compareFhirObjects(specimen2Bbmri, specimenBbmriForConverting);
  }


}


