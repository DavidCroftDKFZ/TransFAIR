package de.samply.transfair.resources;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
public class CauseOfDeath extends ConvertClass<Observation, Condition> {

  static String ICD_SYSTEM = "http://hl7.org/fhir/sid/icd-10";

  static String BBMRI_Profile = "https://fhir.bbmri.de/StructureDefinition/CauseOfDeath";

  static String MII_Profile =
      "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache";

  String CauseOfDeath = "";
  String miiID = "";
  String miiPatientID = "";

  // ICD-10
  String bbmriID = "";
  String bbmriPatientID = "";

  // https://simplifier.net/medizininformatikinitiative-modulperson/sdmiipersontodesursache

  @Override
  public void fromBbmri(Observation resource) {
    if (resource.getMeta().getProfile().stream()
        .anyMatch(canonicalType -> canonicalType.equals(BBMRI_Profile))) {
      this.bbmriID = resource.getId();
      if (resource.getValueCodeableConcept().getCodingFirstRep().getSystem().equals(ICD_SYSTEM)) {
        this.CauseOfDeath = resource.getValueCodeableConcept().getCodingFirstRep().getCode();
      }
      this.bbmriPatientID = resource.getSubject().getReference();
    }
  }

  @Override
  public void fromMii(Condition resource) {
    if (resource.getMeta().getProfile().stream()
        .anyMatch(canonicalType -> canonicalType.equals(MII_Profile))) {
      this.miiID = resource.getId();
      if (resource.getCode().getCodingFirstRep().getSystem().equals(ICD_SYSTEM)) {
        this.CauseOfDeath = resource.getCode().getCodingFirstRep().getCode();
      }
      this.miiPatientID = resource.getSubject().getReference();
    }
  }

  public org.hl7.fhir.r4.model.Observation toBbmri() {
    org.hl7.fhir.r4.model.Observation observation = new org.hl7.fhir.r4.model.Observation();
    observation.setMeta(
        new Meta().addProfile("https://fhir.bbmri.de/StructureDefinition/CauseOfDeath"));

    Coding codingFirstRep = observation.getCode().getCodingFirstRep();
    codingFirstRep.setCode("68343-3");
    codingFirstRep.setSystem("http://loinc.org");

    if (bbmriID.isEmpty() && !miiID.isEmpty()) {
      this.bbmriID = miiID;
    }

    if (!miiPatientID.isEmpty() && bbmriPatientID.isEmpty()) {
      this.bbmriPatientID = miiPatientID;
    }

    if (bbmriID.isBlank() && bbmriPatientID.isBlank() && CauseOfDeath.isBlank()) {
      return null;
    }

    observation.setId(bbmriID);
    observation.setSubject(new Reference().setReference(bbmriPatientID));
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10");

    codeableConcept.getCodingFirstRep().setCode(CauseOfDeath);
    observation.setValue(codeableConcept);

    return observation;
  }

  public org.hl7.fhir.r4.model.Condition toMii() {
    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
    condition.setMeta(
        new Meta()
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache"));

    CodeableConcept codingLoinc = new CodeableConcept();
    codingLoinc.getCodingFirstRep().setSystem("http://loinc.org");
    codingLoinc.getCodingFirstRep().setCode("79378-6");

    if (!bbmriID.isEmpty() && miiID.isEmpty()) {
      this.miiID = this.bbmriID;
    }

    condition.setId(miiID);

    if (miiPatientID.isEmpty() && !bbmriPatientID.isEmpty()) {
      this.miiPatientID = bbmriPatientID;
    }

    condition.setSubject(new Reference(miiPatientID));

    CodeableConcept codingSnomedCt = new CodeableConcept();
    codingSnomedCt.getCodingFirstRep().setSystem("http://snomed.info/sct");
    codingSnomedCt.getCodingFirstRep().setCode("16100001");

    condition.setCategory(List.of(codingLoinc, codingSnomedCt));

    CodeableConcept codeableConceptCause = new CodeableConcept();
    codeableConceptCause.getCodingFirstRep().setSystem(ICD_SYSTEM);
    codeableConceptCause.getCodingFirstRep().setCode(CauseOfDeath);
    condition.setCode(codeableConceptCause);

    return condition;
  }
}
