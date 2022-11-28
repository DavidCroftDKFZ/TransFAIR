package de.samply.transfair.resources;

import java.util.List;
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;

public class CauseOfDeath extends ConvertClass<Observation, Condition> {

  // Currently the MII only has Cause of death

  // SnomedCT
  String miiCauseOfDeath = "";
  String miiID = "";
  String miiPatientID = "";

  // ICD-10
  String bbmriCauseOfDeath = "";
  String bbmriID = "";
  String bbmriPatientID = "";

  // https://simplifier.net/medizininformatikinitiative-modulperson/sdmiipersontodesursache

  @Override
  public void fromBbmri(Observation resource) {
    this.bbmriID = resource.getId();
    this.bbmriCauseOfDeath = resource.getValue().primitiveValue();
    this.bbmriPatientID = resource.getSubject().getId();
  }

  @Override
  public void fromMii(Condition resource) {
    this.miiID = resource.getId();
    this.miiCauseOfDeath = resource.getCode().getCodingFirstRep().getCode();
    this.miiPatientID = resource.getSubject().getReference();
  }

  public org.hl7.fhir.r4.model.Observation toBbmri() {
    org.hl7.fhir.r4.model.Observation observation = new org.hl7.fhir.r4.model.Observation();
    observation.setMeta(new Meta().addProfile("https://fhir.bbmri.de/StructureDefinition/CauseOfDeath"));

    Coding codingFirstRep = observation.getCode().getCodingFirstRep();
    codingFirstRep.setCode("68343-3");
    codingFirstRep.setSystem("http://loinc.org");
    codingFirstRep.setDisplay("Primary cause of death");

    if (bbmriID.isEmpty() && !miiID.isEmpty()) {
      this.bbmriID = miiID;
    }

    if (!miiPatientID.isEmpty() && bbmriPatientID.isEmpty()) {
      this.bbmriPatientID = miiPatientID;
    }

    if (bbmriCauseOfDeath.isEmpty() && !miiCauseOfDeath.isEmpty()) {
      this.bbmriCauseOfDeath = miiCauseOfDeath;
    }

    observation.setId(bbmriID);
    observation.setSubject(new Reference().setReference(bbmriPatientID));
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10");

    codeableConcept.getCodingFirstRep().setCode(bbmriCauseOfDeath);
    observation.setValue(codeableConcept);

    return observation;
  }

  public org.hl7.fhir.r4.model.Condition toMii() {
    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
    condition.setMeta(new Meta().addProfile("https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache"));

    CodeableConcept codingLoinc = new CodeableConcept();
    codingLoinc.getCodingFirstRep().setSystem("http://loinc.org");
    codingLoinc.getCodingFirstRep().setCode("79378-6");

    if (!bbmriID.isEmpty() && miiID.isEmpty()) {
      this.miiID = this.bbmriID;
    } else if (miiPatientID.isEmpty() && !bbmriPatientID.isEmpty()) {
      this.bbmriPatientID = miiPatientID;
    }

    if(Objects.equals(bbmriCauseOfDeath,null) && !Objects.equals(miiCauseOfDeath, null)) {
      this.bbmriCauseOfDeath = miiCauseOfDeath;
    }

    CodeableConcept codingSnomedCt = new CodeableConcept();
    codingSnomedCt.getCodingFirstRep().setSystem("http://snomed.info/sct");
    codingSnomedCt.getCodingFirstRep().setCode("16100001");

    condition.setCategory(List.of(codingLoinc, codingSnomedCt));

    CodeableConcept codeableConceptCause = new CodeableConcept();
    codeableConceptCause.getCodingFirstRep().setSystem("http://fhir.de/CodeSystem/bfarm/icd-10-gm");
    codeableConceptCause.getCodingFirstRep().setCode(miiCauseOfDeath);
    condition.setCode(codeableConceptCause);

    return condition;
  }
}
