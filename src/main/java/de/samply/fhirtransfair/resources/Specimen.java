package de.samply.fhirtransfair.resources;

import java.util.Date;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;
import org.jetbrains.annotations.NotNull;

public class Specimen {

  // Shared
  Date collectedDate;

  // BBMRI data
  String bbmriId;
  String bbmriSubject;
  // Decoded as https://simplifier.net/bbmri.de/samplematerialtype
  String bbmrisampleType;

  String bbmriBodySite;
  String bbmriFastingStatus;

  String storageTemperature;
  String diagnosisICD10;
  String collectionRef;

  // MII data

  String miiId;
  String miiSubject;
  // Decoded as snomed-ct
  String miiSampleType;

  String miiBodySiteIcd;
  String miiBodySiteSnomedCt;
  String miiFastingStatus;

  String miiStoargeTemperatureHigh;
  String miiStoargeTemperaturelow;

  public void fromBBMRISpecimen(@NotNull org.hl7.fhir.r4.model.Specimen specimen) {
    this.bbmriId = specimen.getId();
    this.bbmriSubject = specimen.getSubject().getReference();
    this.bbmrisampleType = specimen.getType().getCodingFirstRep().getCode();

    this.collectedDate = specimen.getCollection().getCollectedDateTimeType().getValue();
    this.bbmriBodySite = specimen.getCollection().getBodySite().getCodingFirstRep().getCode();
    this.bbmriFastingStatus =
        specimen.getCollection().getFastingStatusCodeableConcept().getCodingFirstRep().getCode();

    try {
      for (Extension e : specimen.getExtension()) {
        if (Objects.equals(
            e.getUrl(), "https://fhir.bbmri.de/StructureDefinition/StorageTemperature")) {
          Type t = e.getValue();
          CodeableConcept codeableConcept = (CodeableConcept) t;
          this.storageTemperature = codeableConcept.getCodingFirstRep().getCode();
        } else if (Objects.equals(
            e.getUrl(), "https://fhir.bbmri.de/StructureDefinition/SampleDiagnosis")) {
          Type t = e.getValue();
          CodeableConcept codeableConcept = (CodeableConcept) t;
          this.diagnosisICD10 = codeableConcept.getCodingFirstRep().getCode();
        } else if (Objects.equals(
            e.getUrl(), "https://fhir.bbmri.de/StructureDefinition/Custodian")) {
          Type t = e.getValue();
          Reference ref = (Reference) t;
          this.collectionRef = ref.getReference();
        } else {
          System.out.println("Unsupported Extension");
        }
      }
    } catch (Exception e) {
      System.out.println("This fails :(");
    }
  }

  public void fromMIISpecimen(@NotNull org.hl7.fhir.r4.model.Specimen specimen) {
    this.miiId = specimen.getId();
    this.miiSubject = specimen.getSubject().getReference();

    this.miiSampleType = specimen.getType().getCodingFirstRep().getCode();
    this.collectedDate = specimen.getCollection().getCollectedDateTimeType().getValue();

    if (specimen
        .getCollection()
        .getBodySite()
        .getCodingFirstRep()
        .getSystem()
        .equals("http://snomed.info/sct")) {
      this.miiBodySiteSnomedCt =
          specimen.getCollection().getBodySite().getCodingFirstRep().getCode();
    } else if (specimen
        .getCollection()
        .getBodySite()
        .getCodingFirstRep()
        .getSystem()
        .equals("http://terminology.hl7.org/CodeSystem/icd-o-3")) {
      this.miiBodySiteIcd = specimen.getCollection().getBodySite().getCodingFirstRep().getCode();
    }

    this.miiFastingStatus =
        specimen.getCollection().getFastingStatusCodeableConcept().getCodingFirstRep().getCode();

    for (Extension extension : specimen.getProcessingFirstRep().getExtension()) {
      if (extension
          .getUrl()
          .equals(
              "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen")) {
        Range r = (Range) extension.getValue();
        this.miiStoargeTemperatureHigh = r.getHigh().getUnit();
        this.miiStoargeTemperaturelow = r.getLow().getUnit();
      }
    }
  }

  public org.hl7.fhir.r4.model.Specimen toBBMRISpecimen() {
    return new org.hl7.fhir.r4.model.Specimen();
  }

  public org.hl7.fhir.r4.model.Specimen toMIISpecimen() {
    return new org.hl7.fhir.r4.model.Specimen();
  }
}
