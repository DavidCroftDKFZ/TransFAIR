package de.samply.fhirtransfair.resources;

import java.util.Date;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class Condition extends ConvertClass<org.hl7.fhir.r4.model.Condition,org.hl7.fhir.r4.model.Condition> {

  String bbmriId = "";
  String subject;
  Date onset;
  Date abatement;
  String diagnosisICD10WHO;
  String diagnosisICD10GM;
  String diagnosisICD9;

  String miiId = "";

  @Override
  public void fromBbmri(org.hl7.fhir.r4.model.Condition resource) {
    this.bbmriId = resource.getId();
    this.subject = resource.getSubject().getReference();
    this.onset = resource.getOnsetDateTimeType().getValue();
    this.abatement = resource.getAbatementDateTimeType().getValue();

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

    return condition;
  }

  @Override
  public org.hl7.fhir.r4.model.Condition toMii() {
    return null;
  }

}
