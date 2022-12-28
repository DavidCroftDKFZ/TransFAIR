package de.samply.transfair.converters;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@TestInstance(Lifecycle.PER_CLASS)
public class TemperatureConverterTest {
  
  public static final String SYSTEM = "https://fhir.bbmri.de/CodeSystem/StorageTemperature";
  public static final String MII_TEMPERATURE_EXTENSION_URL = 
      "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen";
  public static final String BBMRI_TEMPERATURE_EXTENSION_URL =
      "https://fhir.bbmri.de/StructureDefinition/StorageTemperature";
  
  String bbmriTemp1, bbmriTemp2, bbmriTemp3, bbmriTemp4, bbmriTemp5, bbmriTemp6, bbmriTemp7;
  
  Extension miiTemp1Extension, 
  miiTemp2Extension, 
      miiTemp3Extension, 
      miiTemp4Extension, 
      miiTemp5Extension, 
      miiTemp6Extension, 
      miiTemp7Extension;
   
  
  @BeforeAll
  void setup() {
    
    bbmriTemp1 = "temperature2to10";
    bbmriTemp2 = "temperature-18to-35";
    bbmriTemp3 = "temperature-60to-85";
    bbmriTemp4 = "temperatureGN";
    bbmriTemp5 = "temperatureLN";
    bbmriTemp6 = "temperatureRoom";
    bbmriTemp7 = "temperatureOther";  
    
    miiTemp1Extension = new Extension();
    miiTemp1Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp1Extension.setValue(new Range().setHigh(new Quantity(10)).setLow(new Quantity(2)));
    
    miiTemp2Extension = new Extension();
    miiTemp2Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp2Extension.setValue(new Range().setHigh(new Quantity(-18)).setLow(new Quantity(-35)));
    
    miiTemp3Extension = new Extension();
    miiTemp3Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp3Extension.setValue(new Range().setHigh(new Quantity(-60)).setLow(new Quantity(-85)));
    
    miiTemp4Extension = new Extension();
    miiTemp4Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp4Extension.setValue(new Range().setHigh(new Quantity(-160)).setLow(new Quantity(-195)));
    
    miiTemp5Extension = new Extension();
    miiTemp5Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp5Extension.setValue(new Range().setHigh(new Quantity(-196)).setLow(new Quantity(-209)));
    
    miiTemp6Extension = new Extension();
    miiTemp6Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp6Extension.setValue(new Range().setHigh(new Quantity(30)).setLow(new Quantity(11)));   
    
    miiTemp7Extension = new Extension();
    miiTemp7Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp7Extension.setValue(new Range());      
    
    miiTemp1Extension = new Extension();
    miiTemp1Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp1Extension.setValue(new Range().setHigh(new Quantity(10)).setLow(new Quantity(2)));
    
    miiTemp2Extension = new Extension();
    miiTemp2Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp2Extension.setValue(new Range().setHigh(new Quantity(-18)).setLow(new Quantity(-35)));
    
    miiTemp3Extension = new Extension();
    miiTemp3Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp3Extension.setValue(new Range().setHigh(new Quantity(-60)).setLow(new Quantity(-85)));
    
    miiTemp4Extension = new Extension();
    miiTemp4Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp4Extension.setValue(new Range().setHigh(new Quantity(-160)).setLow(new Quantity(-195)));
    
    miiTemp5Extension = new Extension();
    miiTemp5Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp5Extension.setValue(new Range().setHigh(new Quantity(-196)).setLow(new Quantity(-209)));
    
    miiTemp6Extension = new Extension();
    miiTemp6Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp6Extension.setValue(new Range().setHigh(new Quantity(30)).setLow(new Quantity(11)));   
    
    miiTemp7Extension = new Extension();
    miiTemp7Extension.setUrl(MII_TEMPERATURE_EXTENSION_URL);
    miiTemp7Extension.setValue(new Range());  
    

  }
  
  @Test
  void fromBbmri2To10ToMiiExpectOK() {
    Extension miiConverted = TemperatureConverter.fromBbrmiToMii(bbmriTemp1);
    
    assertEquals(miiTemp1Extension.getUrl(), miiConverted.getUrl());
    assertEquals(((Range)miiTemp1Extension.getValue()).getHigh().getValue(), ((Range)miiConverted.getValue()).getHigh().getValue());    
    assertEquals(((Range)miiTemp1Extension.getValue()).getLow().getValue(), ((Range)miiConverted.getValue()).getLow().getValue());  
    
  }
  
  @Test
  void fromToMiiToBbmri2To10ExpectOK() {
    Extension bbmriConverted = TemperatureConverter.fromMiiToBbmri(Long.valueOf(10), Long.valueOf(2));
    
    assertEquals(BBMRI_TEMPERATURE_EXTENSION_URL, bbmriConverted.getUrl());
    assertEquals(SYSTEM, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getSystem());    
    assertEquals(bbmriTemp1, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getCode());  
    
  }
  
  @Test
  void fromBbmriMinus18toMinus35ToMiiExpectOK() {
    Extension miiConverted = TemperatureConverter.fromBbrmiToMii(bbmriTemp2);
    
    assertEquals(miiTemp2Extension.getUrl(), miiConverted.getUrl());
    assertEquals(((Range)miiTemp2Extension.getValue()).getHigh().getValue(), ((Range)miiConverted.getValue()).getHigh().getValue());    
    assertEquals(((Range)miiTemp2Extension.getValue()).getLow().getValue(), ((Range)miiConverted.getValue()).getLow().getValue());  
    
  }
  
  @Test
  void fromToMiiToBbmriMinus18toMinus35ExpectOK() {
    Extension bbmriConverted = TemperatureConverter.fromMiiToBbmri(Long.valueOf(-18), Long.valueOf(-35));
    
    assertEquals(BBMRI_TEMPERATURE_EXTENSION_URL, bbmriConverted.getUrl());
    assertEquals(SYSTEM, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getSystem());    
    assertEquals(bbmriTemp2, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getCode());  
    
  }
  
  @Test
  void fromBbmriMinus60toMinus85ToMiiExpectOK() {
    Extension miiConverted = TemperatureConverter.fromBbrmiToMii(bbmriTemp3);
    
    assertEquals(miiTemp3Extension.getUrl(), miiConverted.getUrl());
    assertEquals(((Range)miiTemp3Extension.getValue()).getHigh().getValue(), ((Range)miiConverted.getValue()).getHigh().getValue());    
    assertEquals(((Range)miiTemp3Extension.getValue()).getLow().getValue(), ((Range)miiConverted.getValue()).getLow().getValue());  
    
  }
  
  @Test
  void fromToMiiToBbmriMinus60toMinus85ExpectOK() {
    Extension bbmriConverted = TemperatureConverter.fromMiiToBbmri(Long.valueOf(-60), Long.valueOf(-85));
    
    assertEquals(BBMRI_TEMPERATURE_EXTENSION_URL, bbmriConverted.getUrl());
    assertEquals(SYSTEM, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getSystem());    
    assertEquals(bbmriTemp3, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getCode());  
    
  }
  
  @Test
  void fromBbmriGNToMiiExpectOK() {
    Extension miiConverted = TemperatureConverter.fromBbrmiToMii(bbmriTemp4);
    
    assertEquals(miiTemp4Extension.getUrl(), miiConverted.getUrl());
    assertEquals(((Range)miiTemp4Extension.getValue()).getHigh().getValue(), ((Range)miiConverted.getValue()).getHigh().getValue());    
    assertEquals(((Range)miiTemp4Extension.getValue()).getLow().getValue(), ((Range)miiConverted.getValue()).getLow().getValue());  
    
  }
  
  @Test
  void fromToMiiToBbmriGNExpectOK() {
    Extension bbmriConverted = TemperatureConverter.fromMiiToBbmri(Long.valueOf(-160), Long.valueOf(-195));
    
    assertEquals(BBMRI_TEMPERATURE_EXTENSION_URL, bbmriConverted.getUrl());
    assertEquals(SYSTEM, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getSystem());    
    assertEquals(bbmriTemp4, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getCode());  
    
  }
  
  
  @Test
  void fromBbmriLNToMiiExpectOK() {
    Extension miiConverted = TemperatureConverter.fromBbrmiToMii(bbmriTemp5);
    
    assertEquals(miiTemp5Extension.getUrl(), miiConverted.getUrl());
    assertEquals(((Range)miiTemp5Extension.getValue()).getHigh().getValue(), ((Range)miiConverted.getValue()).getHigh().getValue());    
    assertEquals(((Range)miiTemp5Extension.getValue()).getLow().getValue(), ((Range)miiConverted.getValue()).getLow().getValue());      
    
  }
  
  @Test
  void fromToMiiToBbmriLNExpectOK() {
    Extension bbmriConverted = TemperatureConverter.fromMiiToBbmri(Long.valueOf(-196), Long.valueOf(-209));
    
    assertEquals(BBMRI_TEMPERATURE_EXTENSION_URL, bbmriConverted.getUrl());
    assertEquals(SYSTEM, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getSystem());    
    assertEquals(bbmriTemp5, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getCode());  
    
  }
  
  @Test
  void fromBbmriRoomToMiiExpectOK() {
    Extension miiConverted = TemperatureConverter.fromBbrmiToMii(bbmriTemp6);
    
    assertEquals(miiTemp6Extension.getUrl(), miiConverted.getUrl());
    assertEquals(((Range)miiTemp6Extension.getValue()).getHigh().getValue(), ((Range)miiConverted.getValue()).getHigh().getValue());    
    assertEquals(((Range)miiTemp6Extension.getValue()).getLow().getValue(), ((Range)miiConverted.getValue()).getLow().getValue());  
    
  }
  
  @Test
  void fromToMiiToBbmriRoomExpectOK() {
    Extension bbmriConverted = TemperatureConverter.fromMiiToBbmri(Long.valueOf(30), Long.valueOf(11));
    
    assertEquals(BBMRI_TEMPERATURE_EXTENSION_URL, bbmriConverted.getUrl());
    assertEquals(SYSTEM, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getSystem());    
    assertEquals(bbmriTemp6, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getCode());  
    
  }
  
  @Test
  void fromBbmriOtherToMiiExpectOK() {
    Extension miiConverted = TemperatureConverter.fromBbrmiToMii(bbmriTemp7);
    
    assertEquals(miiTemp7Extension.getUrl(), miiConverted.getUrl());
    assertNull(((Range)miiConverted.getValue()).getHigh().getValue());    
    assertNull(((Range)miiConverted.getValue()).getLow().getValue());  
    
  }
  
  @Test
  void fromToMiiToBbmriOtherExpectOK() {
    Extension bbmriConverted = TemperatureConverter.fromMiiToBbmri(Long.valueOf(0), Long.valueOf(0));
    
    assertEquals(BBMRI_TEMPERATURE_EXTENSION_URL, bbmriConverted.getUrl());
    assertEquals(SYSTEM, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getSystem());    
    assertEquals(bbmriTemp7, ((CodeableConcept)bbmriConverted.getValue()).getCodingFirstRep().getCode());  
    
  }
  

}
