package de.samply.transfair.resources;

import static de.samply.transfair.JsonUtils.compareFhirObjects;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
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

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1997);
    cal.set(Calendar.MONTH, Calendar.OCTOBER);
    cal.set(Calendar.DAY_OF_MONTH, 22);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.ZONE_OFFSET, 2);
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
    // TODO to be converted to MII, Condition searched and referenced, if there is none, a new one created
    codeableConceptBbmriForConvertingDiagnosis.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode("C61");
    diagnosisExtensionBbmri.setValue(codeableConceptBbmriForConvertingDiagnosis);

    Extension storageTemperatureExtensionBbmri = new Extension();
    storageTemperatureExtensionBbmri.setUrl("https://fhir.bbmri.de/StructureDefinition/StorageTemperature");
    CodeableConcept codeableConceptBbmriForConvertingStorageTemperature = new CodeableConcept();
    codeableConceptBbmriForConvertingStorageTemperature.getCodingFirstRep().setSystem("https://fhir.bbmri.de/CodeSystem/StorageTemperature").setCode("temperatureGN");
    storageTemperatureExtensionBbmri.setValue(codeableConceptBbmriForConvertingStorageTemperature);
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
    CodeableConcept codingSampleTypeMiiForComparing = new CodeableConcept();
    codingSampleTypeMiiForComparing.getCodingFirstRep().setCode("119361006").setSystem("http://snomed.info/sct");
    specimenMiiForComparing.setType(codingSampleTypeMiiForComparing);
    specimenMiiForComparing.getCollection().getCollectedDateTimeType().setValue(collectedDate);



    specimenMiiForConverting = new Specimen();
    specimenMiiForConverting.setId("specimenId");
    specimenMiiForConverting.getMeta().setProfile(List.of(new CanonicalType("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Specimen")));
    specimenMiiForConverting.setSubject(new Reference().setReference(patient.getId()));

    Extension storageTemperatureExtensionMiiForConverting = new Extension();
    storageTemperatureExtensionMiiForConverting.setUrl(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen");
    storageTemperatureExtensionMiiForConverting.setValue(new Range().setHigh(new Quantity(-195)).setLow(new Quantity(-160)));
    specimenMiiForConverting.getProcessingFirstRep().setExtension(List.of(storageTemperatureExtensionMiiForConverting));

    CodeableConcept bodySiteCodeForConverting = new CodeableConcept();
    bodySiteCodeForConverting.getCodingFirstRep().setCode("8148/2").setSystem("http://snomed.info/sct");
    specimenMiiForConverting.getCollection().setBodySite(bodySiteCodeForConverting);
    CodeableConcept codingSampleTypeMiiForConverting = new CodeableConcept();
    codingSampleTypeMiiForConverting.getCodingFirstRep().setCode("119361006").setSystem("http://snomed.info/sct");
    specimenMiiForConverting.setType(codingSampleTypeMiiForConverting);

    specimenMiiForConverting.getCollection().getCollectedDateTimeType().setValue(collectedDate);


    specimenBbmriForComparing = new Specimen();
    specimenBbmriForComparing.setId("specimenId");
    specimenBbmriForComparing.getMeta().setProfile(List.of(new CanonicalType("https://fhir.bbmri.de/StructureDefinition/Specimen")));
    specimenBbmriForComparing.setSubject(new Reference().setReference(patient.getId()));

    Extension storageTemperatureExtensionBbmriForComparing = new Extension();
    storageTemperatureExtensionBbmriForComparing.setUrl("https://fhir.bbmri.de/StructureDefinition/StorageTemperature");
    CodeableConcept codeableConceptBbmriForComparingStorageTemperature = new CodeableConcept();
    codeableConceptBbmriForComparingStorageTemperature.getCodingFirstRep().setSystem("https://fhir.bbmri.de/CodeSystem/StorageTemperature").setCode("temperatureGN");
    storageTemperatureExtensionBbmriForComparing.setValue(codeableConceptBbmriForComparingStorageTemperature);
    specimenBbmriForComparing.addExtension(storageTemperatureExtensionBbmriForComparing);

    CodeableConcept bodySiteCodeForComparing = new CodeableConcept();
    bodySiteCodeForComparing.getCodingFirstRep().setCode("8148/2").setSystem("urn:oid:1.3.6.1.4.1.19376.1.3.11.36");
    specimenBbmriForComparing.getCollection().setBodySite(bodySiteCodeForComparing);

    CodeableConcept codeableConceptBbmriForComparingSampleType = new CodeableConcept();
    codeableConceptBbmriForComparingSampleType.getCodingFirstRep().setSystem("https://fhir.bbmri.de/CodeSystem/SampleMaterialType").setCode("blood-plasma");
    specimenBbmriForComparing.setType(codeableConceptBbmriForComparingSampleType);

    specimenBbmriForComparing.getCollection().getCollectedDateTimeType().setValue(collectedDate);

  }

  @Test
  void fromBbmriToMiiExpectOK() {

    Specimen specimen2Mii = new Specimen();

    specimenMapping.fromBbmri(specimenBbmriForConverting);
    specimen2Mii = specimenMapping.toMii();

    compareFhirObjects(specimen2Mii, specimenMiiForComparing);
  }

  @Test
  void fromMiiToBbmriExpectOK() {

    Specimen specimen2Bbmri = new Specimen();

    specimenMapping.fromMii(specimenMiiForConverting);
    specimen2Bbmri = specimenMapping.toBbmri();


    compareFhirObjects(specimen2Bbmri, specimenBbmriForComparing);
  }


}


