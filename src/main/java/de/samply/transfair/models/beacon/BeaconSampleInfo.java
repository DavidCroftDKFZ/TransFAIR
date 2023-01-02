package de.samply.transfair.models.beacon;

/**
 * Models sample info as understood by Beacon 2.x.
 */
public class BeaconSampleInfo {
  public BeaconSampleCharacteristics characteristics;
  public String taxId;

  public static BeaconSampleInfo createHumanBeaconSampleInfo() {
    BeaconSampleInfo beaconSampleInfo = new BeaconSampleInfo();
    beaconSampleInfo.characteristics = BeaconSampleCharacteristics.createHumanSampleCharacteristics();
    beaconSampleInfo.taxId = "9606";

    return beaconSampleInfo;
  }
}
