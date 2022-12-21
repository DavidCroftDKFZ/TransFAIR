package de.samply.transfair.resources;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;

/** Cause of Death mappings for converting between bbmri.de and MII KDS. */
@Slf4j
public class CauseOfDeathMapping extends ConvertClass<Observation, Condition> {

  static String ICD_SYSTEM = "http://hl7.org/fhir/sid/icd-10";

  static String BBMRI_Profile = "https://fhir.bbmri.de/StructureDefinition/CauseOfDeath";

  static String MII_Profile =
      "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache";

  String causeOfDeath = "";
  String miiId = "";
  String miiPatientId = "";

  // ICD-10
  String bbmriId = "";
  String bbmriPatientId = "";

  /** This imports a Cause of Death to bbmri.de. */
  @Override
  public void fromBbmri(Observation resource) {
    if (resource.getMeta().getProfile().stream()
        .anyMatch(canonicalType -> canonicalType.equals(BBMRI_Profile))) {
      this.bbmriId = resource.getId();
      if (resource.getValueCodeableConcept().getCodingFirstRep().getSystem().equals(ICD_SYSTEM)) {
        this.causeOfDeath = resource.getValueCodeableConcept().getCodingFirstRep().getCode();
      }
      this.bbmriPatientId = resource.getSubject().getReference();
    }
  }

  /** This imports a Cause of Death to MII KDS. */
  @Override
  public void fromMii(Condition resource) {
    if (resource.getMeta().getProfile().stream()
        .anyMatch(canonicalType -> canonicalType.equals(MII_Profile))) {
      this.miiId = resource.getId();
      if (resource.getCode().getCodingFirstRep().getSystem().equals(ICD_SYSTEM)) {
        this.causeOfDeath = resource.getCode().getCodingFirstRep().getCode();
      }
      this.miiPatientId = resource.getSubject().getReference();
    }
  }

  /** This exports a Cause of Death to bbmri.de. */
  public org.hl7.fhir.r4.model.Observation toBbmri() {
    org.hl7.fhir.r4.model.Observation observation = new org.hl7.fhir.r4.model.Observation();
    observation.setMeta(
        new Meta().addProfile("https://fhir.bbmri.de/StructureDefinition/CauseOfDeath"));

    Coding codingFirstRep = observation.getCode().getCodingFirstRep();
    codingFirstRep.setCode("68343-3");
    codingFirstRep.setSystem("http://loinc.org");

    if (bbmriId.isEmpty() && !miiId.isEmpty()) {
      this.bbmriId = miiId;
    }

    if (!miiPatientId.isEmpty() && bbmriPatientId.isEmpty()) {
      this.bbmriPatientId = miiPatientId;
    }

    if (bbmriId.isBlank() && bbmriPatientId.isBlank() && causeOfDeath.isBlank()) {
      return new Observation();
    }

    observation.setId(bbmriId);
    observation.setSubject(new Reference().setReference(bbmriPatientId));
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.getCodingFirstRep().setSystem("http://hl7.org/fhir/sid/icd-10");

    codeableConcept.getCodingFirstRep().setCode(causeOfDeath);
    observation.setValue(codeableConcept);

    return observation;
  }

  /** This exports a Cause of Death to MII KDS. */

  public org.hl7.fhir.r4.model.Condition toMii() {
    if (bbmriId.isBlank() && bbmriPatientId.isBlank() && causeOfDeath.isBlank()) {
      return new Condition();
    }

    org.hl7.fhir.r4.model.Condition condition = new org.hl7.fhir.r4.model.Condition();
    condition.setMeta(
        new Meta()
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache"));

    CodeableConcept codingLoinc = new CodeableConcept();
    codingLoinc.getCodingFirstRep().setSystem("http://loinc.org");
    codingLoinc.getCodingFirstRep().setCode("79378-6");

    if (!bbmriId.isEmpty() && miiId.isEmpty()) {
      this.miiId = this.bbmriId;
    }

    condition.setId(miiId);

    if (miiPatientId.isEmpty() && !bbmriPatientId.isEmpty()) {
      this.miiPatientId = bbmriPatientId;
    }

    condition.setSubject(new Reference(miiPatientId));

    CodeableConcept codingSnomedCt = new CodeableConcept();
    codingSnomedCt.getCodingFirstRep().setSystem("http://snomed.info/sct");
    codingSnomedCt.getCodingFirstRep().setCode("16100001");

    condition.setCategory(List.of(codingLoinc, codingSnomedCt));

    CodeableConcept codeableConceptCause = new CodeableConcept();
    codeableConceptCause.getCodingFirstRep().setSystem(ICD_SYSTEM);
    codeableConceptCause.getCodingFirstRep().setCode(causeOfDeath);
    condition.setCode(codeableConceptCause);

    return condition;
  }
}
