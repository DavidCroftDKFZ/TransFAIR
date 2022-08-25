package de.samply.fhirtransfair.converters;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;

public class TemperatureConverter {

  public static Extension fromBbrmiToMii(String BbmriTemp) {
    Extension extension = new Extension();

    extension.setUrl(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen");

    switch (BbmriTemp) {
      case "temperature2to10" ->
          extension.setValue(new Range().setHigh(new Quantity(10)).setLow(new Quantity(2)));
      case "temperature-18to-35" ->
          extension.setValue(new Range().setHigh(new Quantity(-18)).setLow(new Quantity(-35)));
      case "temperature-60to-85" ->
          extension.setValue(new Range().setHigh(new Quantity(-60)).setLow(new Quantity(-85)));
      case "temperatureGN" ->
          extension.setValue(new Range().setHigh(new Quantity(-195)).setLow(new Quantity(-92)));
      case "temperatureLN" -> extension.setValue(
          new Range().setHigh(new Quantity(-196)).setLow(new Quantity(-209)));
      case "temperatureRoom" ->
          extension.setValue(new Range().setHigh(new Quantity(40)).setLow(new Quantity(3)));
      case "temperatureOther" -> extension.setValue(new Range());
    }

    return extension;
  }

  public static Extension fromMiiToBbmri(Long high, Long low) {
    Extension extension = new Extension();
    extension.setUrl("https://fhir.bbmri.de/StructureDefinition/StorageTemperature");

    if (high <= 10 && low >= 2) {
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode("temperature2to10"));
    } else if (high <= -18 && low >= -35) {
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode("temperature-18to-35"));
    } else if (high <= -60 && low >= -85) {
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode("temperature-60to-85"));
    } else if (high <= -209 && low >= -196) {
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode("temperatureLN"));
    } else if (high <= -92 && low >= -195) {
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode("temperatureGN"));
    } else if (high <= 40 && low >= 3) {
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode("temperatureRoom"));
    } else {
      extension.setValue(new CodeableConcept().getCodingFirstRep().setCode("temperatureOther"));
    }

    return extension;
  }
}
