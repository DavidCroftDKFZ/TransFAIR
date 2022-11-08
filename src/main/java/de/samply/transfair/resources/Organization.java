package de.samply.transfair.resources;

import org.hl7.fhir.r4.model.Extension;

public class Organization
    extends ConvertClass<org.hl7.fhir.r4.model.Organization, org.hl7.fhir.r4.model.Organization> {

  String identifer;
  String bioBankDescription;

  void fromBbmriOrganization(org.hl7.fhir.r4.model.Organization Organization) {
    identifer = Organization.getIdentifierFirstRep().getValue();

    for (Extension extension : Organization.getExtension()) {
      if (extension
          .getUrl()
          .equals("https://fhir.bbmri.de/StructureDefinition/OrganizationDescription")) {
        this.bioBankDescription = extension.getValue().primitiveValue();
      }
    }
  }

  void fromMiiOrganization(org.hl7.fhir.r4.model.Organization Organization) {
    identifer = Organization.getIdentifierFirstRep().getValue();

    for (Extension extension : Organization.getExtension()) {
      if (extension
          .getUrl()
          .equals(
              "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/BeschreibungSammlung")) {
        this.bioBankDescription = extension.getValue().primitiveValue();
      }
    }
  }

  org.hl7.fhir.r4.model.Organization toBbmriOrganization() {
    return new org.hl7.fhir.r4.model.Organization();
  }

  org.hl7.fhir.r4.model.Organization toMiiOrganization() {
    return new org.hl7.fhir.r4.model.Organization();
  }

  @Override
  public void fromBbmri(org.hl7.fhir.r4.model.Organization resource) {}

  @Override
  public void fromMii(org.hl7.fhir.r4.model.Organization resource) {}

  @Override
  public org.hl7.fhir.r4.model.Organization toBbmri() {
    return null;
  }

  @Override
  public org.hl7.fhir.r4.model.Organization toMii() {
    return null;
  }
}
