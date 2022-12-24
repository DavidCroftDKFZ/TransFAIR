package de.samply.transfair.resources;

import static de.samply.transfair.JsonUtils.compareFhirObjects;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1997);
    cal.set(Calendar.MONTH, Calendar.OCTOBER);
    cal.set(Calendar.DAY_OF_MONTH, 22);
    Date collectedDate = cal.getTime();
    
//    DateTimeType collected = new DateTimeType();
//    collected.setValueAsString("1997-10-22T00:00:00+02:00");

    specimenBbmriForConverting = new Specimen();
    specimenBbmriForConverting.setId("specimenId");
    specimenBbmriForConverting.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Specimen")));
    specimenBbmriForConverting.setSubject(new Reference().setReference(patient.getId()));

    Extension diagnosisExtensionBbmri = new Extension();
    diagnosisExtensionBbmri.setUrl("https://fhir.bbmri.de/StructureDefinition/SampleDiagnosis");
    CodeableConcept codeableConceptBbmriForConvertingDiagnosis = new CodeableConcept();
    codeableConceptBbmriForConvertingDiagnosis.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C61");
    diagnosisExtensionBbmri.setValue(codeableConceptBbmriForConvertingDiagnosis);

    Extension storageTemperatureExtensionBbmri = new Extension();
    storageTemperatureExtensionBbmri.setUrl("https://fhir.bbmri.de/StructureDefinition/StorageTemperature");
    CodeableConcept codeableConceptBbmriForConvertingStorageTemperature = new CodeableConcept();
    codeableConceptBbmriForConvertingStorageTemperature.getCodingFirstRep().setSystem("https://fhir.bbmri.de/CodeSystem/StorageTemperature").setCode("temperatureGN");
    storageTemperatureExtensionBbmri.setValue(codeableConceptBbmriForConvertingStorageTemperature);
    specimenBbmriForConverting.setExtension(List.of(diagnosisExtensionBbmri, storageTemperatureExtensionBbmri));

    specimenBbmriForConverting.setExtension(List.of(diagnosisExtensionBbmri, storageTemperatureExtensionBbmri));

    CodeableConcept codeableConceptBbmriForConvertingSampleType = new CodeableConcept();
    codeableConceptBbmriForConvertingSampleType.getCodingFirstRep().setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("blood-plasma");

    specimenBbmriForConverting.setType(codeableConceptBbmriForConvertingSampleType);
    
    specimenBbmriForConverting.getCollection().getCollectedDateTimeType().setValue(collectedDate);


    specimenMiiForComparing = new Specimen();
    specimenMiiForComparing.setId("specimenId");
    specimenMiiForComparing.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Specimen")));
    specimenMiiForComparing.setSubject(new Reference().setReference(patient.getId()));
    Extension storageTemperatureExtensionMiiForComparing = new Extension();
    storageTemperatureExtensionMiiForComparing.setUrl(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen");
    storageTemperatureExtensionMiiForComparing.setValue(new Range().setHigh(new Quantity(-195)).setLow(new Quantity(-160)));

    specimenMiiForComparing.getCollection().setExtension(List.of(storageTemperatureExtensionMiiForComparing));
    
    CodeableConcept coding = new CodeableConcept();
    coding.getCodingFirstRep().setCode("119361006").setSystem("http://snomed.info/sct");
    specimenMiiForComparing.setType(coding);

    specimenMiiForComparing.getCollection().getCollectedDateTimeType().setValue(collectedDate);






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

    compareFhirObjects(specimen2Mii, specimenMiiForComparing);
  }

  @Test
  @Disabled
  void fromMiiToBbmriExpectOK() {

    Specimen specimen2Bbmri = new Specimen();

    specimenMapping.fromMii( specimenMiiForConverting);
    specimen2Bbmri = specimenMapping.toBbmri();


    compareFhirObjects(specimen2Bbmri, specimenBbmriForConverting);
  }


}


