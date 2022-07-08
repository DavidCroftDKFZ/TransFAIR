package de.samply.fhirtransfair.resources;

import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;

public class Patient {

  // BBMRI data
  String bbmrId;
  Date brithDate;
  Boolean patientDeceased;
  Date patientDeceasedDateTime;
  String gender;

  // MII data
  String miiId;
  HumanName name;
  Address address;

  public void fromBBMRIPatient(org.hl7.fhir.r4.model.Patient patient) {
    this.bbmrId = patient.getId();
    this.brithDate = patient.getBirthDate();
    this.gender = patient.getGender().toCode();
    if (patient.hasDeceased()) {
      this.patientDeceased = true;
      this.patientDeceasedDateTime = patient.getDeceasedDateTimeType().getValue();
    } else {
      this.patientDeceased = false;
    }
  }

  public void fromMIIPatient(org.hl7.fhir.r4.model.Patient patient) {
    this.miiId = patient.getId();
    this.brithDate = patient.getBirthDate();
    this.gender = patient.getGender().toCode();
    if (patient.hasDeceased()) {
      this.patientDeceased = true;
      this.patientDeceasedDateTime = patient.getDeceasedDateTimeType().getValue();
    } else {
      this.patientDeceased = false;
    }
  }

  public org.hl7.fhir.r4.model.Patient toBbmriPatient() {
    org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
    patient.setId(bbmrId);
    patient.setGender(AdministrativeGender.valueOf(gender));
    patient.setBirthDate(brithDate);

    if (this.patientDeceased) {
      patient.setDeceased(new DateType(this.patientDeceasedDateTime));
    }

    return patient;
  }

  public org.hl7.fhir.r4.model.Patient toMiiPatient() {
    org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
    patient.setId(miiId);
    patient.setAddress((List<Address>) this.address);
    patient.setName((List<HumanName>) this.name);

    // TODO: Map this value
    patient.setGender(AdministrativeGender.valueOf(this.gender));
    patient.setBirthDate(this.brithDate);
    if (this.patientDeceased) {
      patient.setDeceased(new DateType(this.patientDeceasedDateTime));
    }

    return patient;
  }
}
