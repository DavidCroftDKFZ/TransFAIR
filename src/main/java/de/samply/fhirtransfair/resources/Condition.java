package de.samply.fhirtransfair.resources;

import java.util.Date;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class Condition {

  String id;
  String subject;
  Date onset;
  Date abatement;
  String diagnosisICD10WHO;
  String diagnosisICD10GM;
  String diagnosisICD9;

  public void fromBBMRICondition(org.hl7.fhir.r4.model.Condition condition) {
    this.id = condition.getId();
    this.subject = condition.getSubject().getReference();
    this.onset = condition.getOnsetDateTimeType().getValue();
    this.abatement = condition.getAbatementDateTimeType().getValue();

    for (Coding coding : condition.getCode().getCoding()) {
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

  public void fromMiiCondition(org.hl7.fhir.r4.model.Condition condition) {
    // https://simplifier.net/medizininformatikinitiative-moduldiagnosen/diagnose
  }

  public org.hl7.fhir.r4.model.Condition toBbmriCondition() {
    return new org.hl7.fhir.r4.model.Condition();
  }

  public org.hl7.fhir.r4.model.Condition toMiiCondition() {
    return new org.hl7.fhir.r4.model.Condition();
  }
}
