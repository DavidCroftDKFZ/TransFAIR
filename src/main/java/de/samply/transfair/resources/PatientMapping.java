package de.samply.transfair.resources;

import java.util.Date;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGenderEnumFactory;
import org.hl7.fhir.r4.model.Meta;

/** Patientmappings for converting between bbmri.de and MII KDS. */
public class PatientMapping
    extends ConvertClass<org.hl7.fhir.r4.model.Patient, org.hl7.fhir.r4.model.Patient> {

  // MII data
  String miiId = "";

  // BBMRI data
  String bbmriId = "";
  Date brithDate;
  Boolean patientDeceased;
  Date patientDeceasedDateTime;
  String gender;

  public String getMiiId() {
    return miiId;
  }

  public String getBbmriId() {
    return bbmriId;
  }

  public void setMiiId(String id) {
    this.miiId = id;
  }

  public void setBbmriId(String id) {
    this.bbmriId = id;
  }

  @Override
  public void fromBbmri(org.hl7.fhir.r4.model.Patient resource) {
    this.bbmriId = resource.getId();
    this.brithDate = resource.getBirthDate();
    this.gender = resource.getGender().toCode();
    if (resource.hasDeceased()) {
      this.patientDeceased = true;
      this.patientDeceasedDateTime = resource.getDeceasedDateTimeType().getValue();
    } else {
      this.patientDeceased = false;
    }
  }

  @Override
  public void fromMii(org.hl7.fhir.r4.model.Patient resource) {
    this.miiId = resource.getId();
    this.brithDate = resource.getBirthDate();
    this.gender = resource.getGender().toCode();
    if (resource.getDeceasedBooleanType().equals(new BooleanType(true))) {
      this.patientDeceased = true;
      this.patientDeceasedDateTime = resource.getDeceasedDateTimeType().getValue();
    } else {
      this.patientDeceased = false;
    }
  }

  @Override
  public org.hl7.fhir.r4.model.Patient toBbmri() {
    org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
    patient.setMeta(
        new Meta().addProfile("https://fhir.simplifier.net/bbmri.de/StructureDefinition/Patient"));

    patient.setGender(new AdministrativeGenderEnumFactory().fromCode(this.gender));
    patient.setBirthDate(brithDate);

    if (bbmriId.isEmpty() && !miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.bbmriId = miiId;
    }

    patient.setId(bbmriId);

    if (this.patientDeceased) {
      patient.getDeceasedDateTimeType().setValue(this.patientDeceasedDateTime);
    }

    return patient;
  }

  @Override
  public org.hl7.fhir.r4.model.Patient toMii() {
    org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
    patient.setMeta(
        new Meta()
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient"));

    if (!bbmriId.isEmpty() && miiId.isEmpty()) {
      // Todo: Add mapping from Patientfilter
      this.miiId = bbmriId;
    }

    patient.setId(miiId);

    patient.setGender(new AdministrativeGenderEnumFactory().fromCode(this.gender));
    patient.setBirthDate(this.brithDate);
    if (this.patientDeceased) {
      patient.getDeceasedDateTimeType().setValue(this.patientDeceasedDateTime);
    }

    return patient;
  }
}
