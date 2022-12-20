package de.samply.transfair.resources;

import java.util.Date;
import java.util.Objects;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import lombok.extern.slf4j.Slf4j;
import static de.samply.transfair.converters.IcdSnomedConverter.*;

/** Organizationmappings for converting between bbmri.de and MII KDS. */
@Slf4j
public class ConditionMapping
extends ConvertClass<org.hl7.fhir.r4.model.Condition, org.hl7.fhir.r4.model.Condition> {

  String bbmriId = "";
  String bbmriSubject;
  Date onset;
  String diagnosisICD10WHO;
  String diagnosisSnomed;

  String diagnosisICD10GM;
  String diagnosisICD9;

  String miiId = "";
  String miiSubject;

  @Override
  public void fromBbmri(org.hl7.fhir.r4.model.Condition resource) {
    this.bbmriId = resource.getId();
    this.bbmriSubject = resource.getSubject().getReference();
    this.onset = resource.getOnsetDateTimeType().getValue();

    for (Coding coding : resource.getCode().getCoding()) {
      if (Objects.equals(coding.getSystem(), "http://hl7.org/fhir/sid/icd-10")) {
        this.diagnosisICD10WHO = coding.getCode();
      } else if (Objects.equals(
          coding.getSystem(), "http://fhir.de/CodeSystem/bfarm/icd-10-gm")) {
        this.diagnosisICD10GM = coding.getCode();
      } else if (Objects.equals(coding.getSystem(), "http://hl7.org/fhir/sid/icd-9")) {
        this.diagnosisICD9 = coding.getCode();
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
        this.diagnosisICD10GM = coding.getCode();
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

    if (this.diagnosisICD10GM != null) {
      condition
      .getCode()
      .getCodingFirstRep()
      .setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm")
      .setCode(this.diagnosisICD10GM);
    } else if (this.diagnosisSnomed != null) {
      condition.getCode().getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10").setCode(fromIcd10Who2Snomed(this.diagnosisSnomed));
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

    if (Objects.equals(this.diagnosisICD10GM, null)
        && (Objects.nonNull(this.diagnosisICD10WHO) || Objects.nonNull(this.diagnosisICD9))) {
      if (Objects.nonNull(this.diagnosisICD10WHO)) {
        // Todo: convert properly
        this.diagnosisICD10GM = this.diagnosisICD10WHO;
      } else {
        // Todo: convert properly
        this.diagnosisICD10GM = diagnosisICD9;
      }
    }

    condition
    .getCode()
    .getCodingFirstRep()
    .setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm")
    .setCode(this.diagnosisICD10GM);

    return condition;
  }
}
