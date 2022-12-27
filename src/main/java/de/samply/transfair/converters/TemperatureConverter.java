package de.samply.transfair.converters;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;

/** Convert between bbmri.de and MII KDS temperature. */
public class TemperatureConverter {
  
  public static final String URL = "https://fhir.bbmri.de/CodeSystem/StorageTemperature";

  /** From bbmri.de to MII KDS temperature. */
  public static Extension fromBbrmiToMii(String bbmriTemp) {
    Extension extension = new Extension();
    extension.setUrl(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen");

    switch (bbmriTemp) {
      case "temperature2to10" -> extension.setValue(
          new Range().setHigh(new Quantity(10)).setLow(new Quantity(2)));
      case "temperature-18to-35" -> extension.setValue(
          new Range().setHigh(new Quantity(-18)).setLow(new Quantity(-35)));
      case "temperature-60to-85" -> extension.setValue(
          new Range().setHigh(new Quantity(-60)).setLow(new Quantity(-85)));
      case "temperatureGN" -> extension.setValue(
          new Range().setHigh(new Quantity(-160)).setLow(new Quantity(-195)));
      case "temperatureLN" -> extension.setValue(
          new Range().setHigh(new Quantity(-196)).setLow(new Quantity(-209)));
      case "temperatureRoom" -> extension.setValue(
          new Range().setHigh(new Quantity(30)).setLow(new Quantity(11)));
      case "temperatureOther" -> extension.setValue(new Range());
      default -> new Range();
    }
    return extension;
  }

  /** From MII KDS to bbmri.de temperature. */
  public static Extension fromMiiToBbmri(Long high, Long low) {
    Extension extension = new Extension();
    extension.setUrl("https://fhir.bbmri.de/StructureDefinition/StorageTemperature");

    CodeableConcept codeableConcept = new CodeableConcept();

    if (high <= 10 && low >= 2) {
      codeableConcept.getCodingFirstRep().setSystem(URL).setCode("temperature2to10");
    } else if (high <= -18 && low >= -35) {
      codeableConcept.getCodingFirstRep().setSystem(URL).setCode("temperature-18to-35");
    } else if (high <= -60 && low >= -85) {
      codeableConcept.getCodingFirstRep().setSystem(URL).setCode("temperature-60to-85");
    } else if (high <= -196 && low >= -209) {
      codeableConcept.getCodingFirstRep().setSystem(URL).setCode("temperatureLN");
    } else if (high <= -160 && low >= -195) {
      codeableConcept.getCodingFirstRep().setSystem(URL).setCode("temperatureGN");
    } else if (high <= 30 && low >= 11) {
      codeableConcept.getCodingFirstRep().setSystem(URL).setCode("temperatureRoom");
    } else {
      codeableConcept.getCodingFirstRep().setSystem(URL).setCode("temperatureOther");
    }

    extension.setValue(codeableConcept);

    return extension;
  }
}
