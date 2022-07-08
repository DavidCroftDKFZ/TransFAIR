package de.samply.fhirtransfair.resources;

import java.util.List;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;

public class CauseOfDeath {

  // Currently the MII only has Cause of death

  String miiCauseOfDeath;
  String bbmriCauseOfDeath;

  public void fromBbmri(org.hl7.fhir.r4.model.Observation observation) {
    // Type, Cause of Death, BMI, Body hight, Body wight, tabacco use
    this.bbmriCauseOfDeath = observation.getValue().primitiveValue();
  }

  public void fromMii(org.hl7.fhir.r4.model.Condition observation) {
    this.miiCauseOfDeath = observation.getCategoryFirstRep().getCodingFirstRep().getCode();
  }
  // https://simplifier.net/medizininformatikinitiative-modulperson/sdmiipersontodesursache

  public org.hl7.fhir.r4.model.Observation toBbmri() {
    org.hl7.fhir.r4.model.Observation observation = new org.hl7.fhir.r4.model.Observation();

    return observation;
  }

  public org.hl7.fhir.r4.model.Condition toMii() {
    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
    CodeableConcept codeableConcept = new CodeableConcept();
    Coding c = new Coding();
    c.setCode(miiCauseOfDeath);
    codeableConcept.setCoding((List<Coding>) c);

    condition.setCategory((List<CodeableConcept>) codeableConcept);

    return condition;
  }
}
