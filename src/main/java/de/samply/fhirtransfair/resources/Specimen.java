package de.samply.fhirtransfair.resources;

import de.samply.fhirtransfair.converters.SnomedSamplyTypeConverter;
import java.util.Date;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;

public class Specimen extends ConvertClass<org.hl7.fhir.r4.model.Specimen, org.hl7.fhir.r4.model.Specimen> {

  // Shared
  Date collectedDate;

  // BBMRI data
  String bbmriId = "";
  String bbmriSubject;
  // Decoded as https://simplifier.net/bbmri.de/samplematerialtype
  String bbmrisampleType;

  String bbmriBodySite;
  String bbmriFastingStatus;

  String storageTemperature;
  String diagnosisICD10;
  String collectionRef;

  // MII data

  String miiId = "";
  String miiSubject;
  // Decoded as snomed-ct
  String miiSampleType;

  String miiBodySiteIcd;
  String miiBodySiteSnomedCt;
  String miiFastingStatus;

  String miiStoargeTemperatureHigh;
  String miiStoargeTemperaturelow;

  @Override
  public void fromBbmri(org.hl7.fhir.r4.model.Specimen resource) {
    this.bbmriId = resource.getId();
    this.bbmriSubject = resource.getSubject().getReference();
    this.bbmrisampleType = resource.getType().getCodingFirstRep().getCode();

    this.collectedDate = resource.getCollection().getCollectedDateTimeType().getValue();
    this.bbmriBodySite = resource.getCollection().getBodySite().getCodingFirstRep().getCode();
    this.bbmriFastingStatus =
        resource.getCollection().getFastingStatusCodeableConcept().getCodingFirstRep().getCode();

    try {
      for (Extension e : resource.getExtension()) {
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

  @Override
  public void fromMii(org.hl7.fhir.r4.model.Specimen resource) {
    this.miiId = resource.getId();
    this.miiSubject = resource.getSubject().getReference();

    this.miiSampleType = resource.getType().getCodingFirstRep().getCode();
    this.collectedDate = resource.getCollection().getCollectedDateTimeType().getValue();

    if (Objects.equals(resource
        .getCollection()
        .getBodySite()
        .getCodingFirstRep()
        .getSystem(), "http://snomed.info/sct")) {
      this.miiBodySiteSnomedCt =
          resource.getCollection().getBodySite().getCodingFirstRep().getCode();
    } else if (Objects.equals(resource
        .getCollection()
        .getBodySite()
        .getCodingFirstRep()
        .getSystem(), "http://terminology.hl7.org/CodeSystem/icd-o-3")) {
      this.miiBodySiteIcd = resource.getCollection().getBodySite().getCodingFirstRep().getCode();
    }

    this.miiFastingStatus =
        resource.getCollection().getFastingStatusCodeableConcept().getCodingFirstRep().getCode();

    for (Extension extension : resource.getProcessingFirstRep().getExtension()) {
      if (Objects.equals(extension
              .getUrl(),
          "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen")) {
        Range r = (Range) extension.getValue();
        this.miiStoargeTemperatureHigh = r.getHigh().getUnit();
        this.miiStoargeTemperaturelow = r.getLow().getUnit();
      }
    }
  }

  @Override
  public org.hl7.fhir.r4.model.Specimen toBbmri() {
    org.hl7.fhir.r4.model.Specimen specimen = new org.hl7.fhir.r4.model.Specimen();

    if(bbmriId.isEmpty() && !miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.bbmriId = miiId;
    }

    specimen.setId(bbmriId);

    if(bbmriSubject.isEmpty() && !miiSubject.isEmpty()) {
      this.bbmriSubject = miiSubject;
    }

    specimen.getSubject().setReference(bbmriSubject);

    if(Objects.equals(bbmrisampleType,null)) {
      this.bbmrisampleType = SnomedSamplyTypeConverter.fromBbmriToMii(miiSampleType);
    }

    CodeableConcept coding = new CodeableConcept();
    coding.getCodingFirstRep().setCode(bbmrisampleType);
    specimen.setType(coding);


    return specimen;
  }

  @Override
  public org.hl7.fhir.r4.model.Specimen toMii() {
    org.hl7.fhir.r4.model.Specimen specimen = new org.hl7.fhir.r4.model.Specimen();

    if(!bbmriId.isEmpty() && miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.miiId = bbmriId;
    }

    specimen.setId(miiId);

    if(!bbmriSubject.isEmpty() && miiSubject.isEmpty()) {
      this.miiSubject = bbmriSubject;
    }

    specimen.getSubject().setReference(miiSubject);

    if(Objects.equals(miiSampleType,null)) {
      this.miiSampleType = SnomedSamplyTypeConverter.fromBbmriToMii(bbmrisampleType);
    }

    CodeableConcept coding = new CodeableConcept();
    coding.getCodingFirstRep().setCode(miiSampleType);
    specimen.setType(coding);

    return specimen;
  }
}
