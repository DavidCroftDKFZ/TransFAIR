package de.samply.transfair.resources;

import de.samply.transfair.converters.Icd10Converter;
import de.samply.transfair.converters.SnomedSamplyTypeConverter;
import de.samply.transfair.converters.TemperatureConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;

/** Specimenmappings for converting between bbmri.de and MII KDS. */
@Slf4j
public class SpecimenMapping
    extends ConvertClass<org.hl7.fhir.r4.model.Specimen, org.hl7.fhir.r4.model.Specimen> {

  // Shared
  Date collectedDate;

  String fastingStatus;

  // BBMRI data
  String bbmriId = "";
  String bbmriSubject = "";
  // Decoded as https://simplifier.net/bbmri.de/samplematerialtype
  String bbmrisampleType;

  String bbmriBodySite;

  String storageTemperature;
  private String diagnosisIcd10Gm;
  String diagnosisIcd10Who;

  String collectionRef;

  // MII data

  String miiId = "";
  String miiSubject = "";
  // Decoded as snomed-ct
  String miiSampleType;

  String miiBodySiteIcd;
  String miiBodySiteSnomedCt;

  private String miiConditionRef;

  Long miiStoargeTemperatureHigh;
  Long miiStoargeTemperaturelow;

  boolean hasParent;

  @Override
  public void fromBbmri(org.hl7.fhir.r4.model.Specimen resource) {
    this.bbmriId = resource.getId();
    this.bbmriSubject = resource.getSubject().getReference();
    this.bbmrisampleType = resource.getType().getCodingFirstRep().getCode();

    this.collectedDate = resource.getCollection().getCollectedDateTimeType().getValue();
    this.bbmriBodySite = resource.getCollection().getBodySite().getCodingFirstRep().getCode();
    this.fastingStatus =
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
          for (Coding codeableConcept1 : codeableConcept.getCoding()) {
            switch (codeableConcept1.getSystem()) {
              case "http://hl7.org/fhir/sid/icd-10":
                this.diagnosisIcd10Who = codeableConcept.getCodingFirstRep().getCode();
                break;
              case "http://fhir.de/CodeSystem/dimdi/icd-10-gm":
                this.setDiagnosisIcd10Gm(codeableConcept.getCodingFirstRep().getCode());
                break;
              default:
            }
          }
        } else if (Objects.equals(
            e.getUrl(), "https://fhir.bbmri.de/StructureDefinition/Custodian")) {
          Type t = e.getValue();
          Reference ref = (Reference) t;
          this.collectionRef = ref.getReference();
        } else {
          log.info("Unsupported Extension");
        }
      }
    } catch (Exception e) {
      log.info("This fails :(");
      log.error(e.getMessage());
    }
  }

  @Override
  public void fromMii(org.hl7.fhir.r4.model.Specimen resource) {

    this.hasParent = resource.hasParent();

    this.miiId = resource.getId();
    this.miiSubject = resource.getSubject().getReference();

    this.miiSampleType = resource.getType().getCodingFirstRep().getCode();
    this.collectedDate = resource.getCollection().getCollectedDateTimeType().getValue();

    if (Objects.equals(
        resource.getCollection().getBodySite().getCodingFirstRep().getSystem(),
        "http://snomed.info/sct")) {
      this.miiBodySiteSnomedCt =
          resource.getCollection().getBodySite().getCodingFirstRep().getCode();
    } else if (Objects.equals(
        resource.getCollection().getBodySite().getCodingFirstRep().getSystem(),
        "http://terminology.hl7.org/CodeSystem/icd-o-3")) {
      this.miiBodySiteIcd = resource.getCollection().getBodySite().getCodingFirstRep().getCode();
    }

    this.fastingStatus =
        resource.getCollection().getFastingStatusCodeableConcept().getCodingFirstRep().getCode();

    for (Extension extension : resource.getProcessingFirstRep().getExtension()) {
      if (Objects.equals(
          extension.getUrl(),
          "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen")) {
        Range r = (Range) extension.getValue();
        this.miiStoargeTemperatureHigh = r.getHigh().getValue().longValue();
        this.miiStoargeTemperaturelow = r.getLow().getValue().longValue();
      } else if (Objects.equals(
          extension.getUrl(),
          "https://simplifier.net/medizininformatikinitiative-modulbiobank/files/fsh-generated/resources/structuredefinition-diagnose.json")) {
        this.setMiiConditionRef(extension.getValue().toString());
      }
    }
  }

  @Override
  public org.hl7.fhir.r4.model.Specimen toBbmri() {

    if (this.hasParent) {
      return null;
    }

    org.hl7.fhir.r4.model.Specimen specimen = new org.hl7.fhir.r4.model.Specimen();
    specimen.setMeta(new Meta().addProfile("https://fhir.bbmri.de/StructureDefinition/Specimen"));

    if (bbmriId.isEmpty() && !miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.bbmriId = miiId;
    }

    specimen.setId(bbmriId);

    if (bbmriSubject.isEmpty() && !miiSubject.isEmpty()) {
      this.bbmriSubject = miiSubject;
    }

    specimen.getSubject().setReference(bbmriSubject);

    if (Objects.equals(bbmrisampleType, null) && !Objects.equals(miiSampleType, null)) {
      this.bbmrisampleType = SnomedSamplyTypeConverter.fromMiiToBbmri(miiSampleType);
    }

    CodeableConcept coding = new CodeableConcept();
    coding.getCodingFirstRep().setCode(bbmrisampleType);
    specimen.setType(coding);

    specimen.getCollection().getCollectedDateTimeType().setValue(this.collectedDate);

    if (Objects.nonNull(miiBodySiteIcd)) {
      this.bbmriBodySite = miiBodySiteIcd;
    } else if (Objects.nonNull(miiBodySiteSnomedCt)) {
      // Todo: Cast from Snomed CT to ICD-0-3
      this.bbmriBodySite = miiBodySiteSnomedCt;
    }

    CodeableConcept bodySiteCode = new CodeableConcept();
    bodySiteCode.getCodingFirstRep().setCode(this.bbmriBodySite);
    bodySiteCode.getCodingFirstRep().setSystem("urn:oid:1.3.6.1.4.1.19376.1.3.11.36");
    specimen.getCollection().setBodySite(bodySiteCode);

    specimen
        .getCollection()
        .getFastingStatusCodeableConcept()
        .getCodingFirstRep()
        .setCode(this.fastingStatus);

    if (Objects.nonNull(storageTemperature)) {
      Extension extension = new Extension();
      extension.setUrl("https://fhir.bbmri.de/StructureDefinition/StorageTemperature");
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode(storageTemperature));
    }

    if (Objects.nonNull(this.miiStoargeTemperatureHigh)
        && Objects.nonNull(this.miiStoargeTemperaturelow)) {
      specimen.addExtension(
          TemperatureConverter.fromMiiToBbmri(
              this.miiStoargeTemperatureHigh, this.miiStoargeTemperaturelow));
    }

    if (Objects.nonNull(this.getDiagnosisIcd10Gm()) || Objects.nonNull(this.diagnosisIcd10Who)) {
      Extension extension = new Extension();
      extension.setUrl("https://fhir.bbmri.de/StructureDefinition/SampleDiagnosis");
      List<Coding> diagnosis = new ArrayList<>();
      if (Objects.nonNull(this.getDiagnosisIcd10Gm())) {
        this.diagnosisIcd10Who = Icd10Converter.gm2who(this.getDiagnosisIcd10Gm());
      }
      diagnosis.add(
          new Coding()
              .setSystem("http://fhir.de/CodeSystem/dimdi/icd-10-gm")
              .setCode(this.getDiagnosisIcd10Gm()));
      diagnosis.add(
          new Coding()
              .setSystem("http://hl7.org/fhir/sid/icd-10")
              .setCode(this.getDiagnosisIcd10Gm()));
      CodeableConcept codeableConcept = new CodeableConcept();
      extension.setValue(codeableConcept.setCoding(diagnosis));
    }

    return specimen;
  }

  @Override
  public org.hl7.fhir.r4.model.Specimen toMii() {
    org.hl7.fhir.r4.model.Specimen specimen = new org.hl7.fhir.r4.model.Specimen();
    specimen.setMeta(
        new Meta()
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Specimen"));

    if (!bbmriId.isEmpty() && miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.miiId = bbmriId;
    }

    specimen.setId(miiId);

    if (!bbmriSubject.isEmpty() && miiSubject.isEmpty()) {
      this.miiSubject = bbmriSubject;
    }

    specimen.getSubject().setReference(miiSubject);

    if (Objects.equals(miiSampleType, null)) {
      this.miiSampleType = SnomedSamplyTypeConverter.fromBbmriToMii(bbmrisampleType);
    }

    CodeableConcept coding = new CodeableConcept();
    coding.getCodingFirstRep().setCode(miiSampleType).setSystem("http://snomed.info/sct");
    specimen.setType(coding);

    specimen.getCollection().getCollectedDateTimeType().setValue(this.collectedDate);

    specimen
        .getCollection()
        .getFastingStatusCodeableConcept()
        .getCodingFirstRep()
        .setCode(this.fastingStatus);

    if (!Objects.equals(this.storageTemperature, null)) {
      specimen
          .getCollection()
          .setExtension(List.of(TemperatureConverter.fromBbrmiToMii(this.storageTemperature)));
    }
    return specimen;
  }

  public void setMiiConditionRef(String miiConditionRef) {
    this.miiConditionRef = miiConditionRef;
  }

  public String getMiiConditionRef() {
    return miiConditionRef;
  }

  public String getDiagnosisIcd10Gm() {
    return diagnosisIcd10Gm;
  }

  public void setDiagnosisIcd10Gm(String diagnosisIcd10Gm) {
    this.diagnosisIcd10Gm = this.diagnosisIcd10Gm;
  }
}
