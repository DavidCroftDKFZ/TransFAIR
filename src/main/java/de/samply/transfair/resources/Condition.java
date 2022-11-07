package de.samply.transfair.resources;

import java.util.Date;
import java.util.Objects;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;

public class Condition extends ConvertClass<org.hl7.fhir.r4.model.Condition,org.hl7.fhir.r4.model.Condition> {

  String bbmriId = "";
  String bbmriSubject;
  Date onset;
  String diagnosisICD10WHO;
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
          coding.getSystem(), "http://fhir.de/StructureDefinition/CodingICD10GM")) {
        this.diagnosisICD10GM = coding.getCode();
      } else if (Objects.equals(coding.getSystem(), "http://hl7.org/fhir/sid/icd-9")) {
        this.diagnosisICD9 = coding.getCode();
      } else {
        System.out.println("Unsupported Coding");
      }
    }
  }

  @Override
  public void fromMii(org.hl7.fhir.r4.model.Condition resource) {

    this.miiId = resource.getId();


  }

  @Override
  public org.hl7.fhir.r4.model.Condition toBbmri() {
    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();

    if(bbmriId.isEmpty() && !miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.bbmriId = miiId;
    }

    if(Objects.equals(bbmriSubject,null) && Objects.nonNull(miiSubject)) {
      this.bbmriSubject = this.miiSubject;
    }
    condition.setSubject(new Reference(bbmriSubject));

    condition.getOnsetDateTimeType().setValue(this.onset);

    condition.getCode().getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode(this.diagnosisICD10GM);

    return condition;
  }

  @Override
  public org.hl7.fhir.r4.model.Condition toMii() {
    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();

    if(miiId.isEmpty() && !bbmriId.isEmpty()) {
      this.miiId = bbmriId;
    }
    condition.setId(miiId);

    if(Objects.equals(miiSubject,null) && Objects.nonNull(bbmriSubject)) {
      this.miiSubject = this.bbmriSubject;
    }
    condition.setSubject(new Reference(miiSubject));

    condition.getOnsetDateTimeType().setValue(this.onset);

    if(Objects.equals(this.diagnosisICD10GM,null) && (Objects.nonNull(this.diagnosisICD10WHO) || Objects.nonNull(this.diagnosisICD9))) {
      if(Objects.nonNull(this.diagnosisICD10WHO)) {
        //Todo: convert properly
        this.diagnosisICD10GM = this.diagnosisICD10WHO;
      } else {
        //Todo: convert properly
        this.diagnosisICD10GM = diagnosisICD9;
      }
    }

    condition.getCode().getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm").setCode(this.diagnosisICD10GM);

    return condition;
  }

}
