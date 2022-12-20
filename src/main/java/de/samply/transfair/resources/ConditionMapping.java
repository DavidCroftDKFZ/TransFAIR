package de.samply.transfair.resources;

import static de.samply.transfair.converters.IcdSnomedConverter.fromSnomed2Icd10Who;

import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

/** Organizationmappings for converting between bbmri.de and MII KDS. */
@Slf4j
public class ConditionMapping
    extends ConvertClass<org.hl7.fhir.r4.model.Condition, org.hl7.fhir.r4.model.Condition> {

  String bbmriId = "";
  String bbmriSubject;
  Date onset;
  String diagnosisIcd10Who;
  String diagnosisSnomed;

  String diagnosisIcd10Gm;
  String diagnosisIcd9;

  String miiId = "";
  String miiSubject;

  @Override
  public void fromBbmri(org.hl7.fhir.r4.model.Condition resource) {
    this.bbmriId = resource.getId();
    this.bbmriSubject = resource.getSubject().getReference();
    this.onset = resource.getOnsetDateTimeType().getValue();

    for (Coding coding : resource.getCode().getCoding()) {
      if (Objects.equals(coding.getSystem(), "http://hl7.org/fhir/sid/icd-10")) {
        this.diagnosisIcd10Who = coding.getCode();
      } else if (Objects.equals(coding.getSystem(), "http://fhir.de/CodeSystem/bfarm/icd-10-gm")) {
        this.diagnosisIcd10Gm = coding.getCode();
      } else if (Objects.equals(coding.getSystem(), "http://hl7.org/fhir/sid/icd-9")) {
        this.diagnosisIcd9 = coding.getCode();
      } else {
        log.info("Unsupported Coding");
      }
    }
  }

  @Override
  public void fromMii(org.hl7.fhir.r4.model.Condition resource) {
    this.miiId = resource.getId();

    for (Coding coding : resource.getCode().getCoding()) {
      if (Objects.equals(coding.getSystem(), "http://fhir.de/CodeSystem/bfarm/icd-10-gm")) {
        this.diagnosisIcd10Gm = coding.getCode();
        continue;
      }

      if (Objects.equals(coding.getSystem(), "http://snomed.info/sct")) {
        this.diagnosisSnomed = coding.getCode();
        continue;
      }
      log.debug("Not supported");
    }

    this.onset = resource.getOnsetDateTimeType().getValue();
  }

  @Override
  public org.hl7.fhir.r4.model.Condition toBbmri() {
    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
    condition.setMeta(new Meta().addProfile("https://fhir.bbmri.de/StructureDefinition/Condition"));

    if (bbmriId.isEmpty() && !miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.bbmriId = miiId;
    }

    if (Objects.equals(bbmriSubject, null) && Objects.nonNull(miiSubject)) {
      this.bbmriSubject = this.miiSubject;
    }

    condition.setId(bbmriId);

    condition.setSubject(new Reference(bbmriSubject));

    condition.getOnsetDateTimeType().setValue(this.onset);

    if (Objects.nonNull(this.diagnosisIcd10Gm)) {
      condition
          .getCode()
          .getCodingFirstRep()
          .setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm")
          .setCode(this.diagnosisIcd10Gm);
    } else if (this.diagnosisSnomed != null) {
      condition
          .getCode()
          .getCodingFirstRep()
          .setSystem("http://hl7.org/fhir/sid/icd-10")
          .setCode(fromSnomed2Icd10Who(this.diagnosisSnomed));
    }

    return condition;
  }

  @Override
  public org.hl7.fhir.r4.model.Condition toMii() {
    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
    condition.setMeta(
        new Meta()
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose"));

    if (miiId.isEmpty() && !bbmriId.isEmpty()) {
      this.miiId = bbmriId;
    }
    condition.setId(miiId);

    if (Objects.equals(miiSubject, null) && Objects.nonNull(bbmriSubject)) {
      this.miiSubject = this.bbmriSubject;
    }
    condition.setSubject(new Reference(miiSubject));

    condition.getOnsetDateTimeType().setValue(this.onset);

    if (Objects.equals(this.diagnosisIcd10Gm, null)
        && (Objects.nonNull(this.diagnosisIcd10Who) || Objects.nonNull(this.diagnosisIcd9))) {
      if (Objects.nonNull(this.diagnosisIcd10Who)) {
        // Todo: convert properly
        this.diagnosisIcd10Gm = this.diagnosisIcd10Who;
      } else {
        // Todo: convert properly
        this.diagnosisIcd10Gm = diagnosisIcd9;
      }
    }

    condition
        .getCode()
        .getCodingFirstRep()
        .setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm")
        .setCode(this.diagnosisIcd10Gm);

    return condition;
  }
}
