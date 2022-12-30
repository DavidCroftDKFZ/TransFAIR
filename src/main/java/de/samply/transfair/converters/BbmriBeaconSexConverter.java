package de.samply.transfair.converters;

import de.samply.transfair.models.beacon.BeaconSex;

/** Static methods for converting the bbmri.de sex to Beacon sex and back. */
public class BbmriBeaconSexConverter {

  /** From bbmri.de to Beacon sex/gender. */
  public static BeaconSex fromBbmriToBeacon(String bbmriGender) {
    if (bbmriGender.toLowerCase().equals("male")) {
      return new BeaconSex("NCIT:C20197", "male");
    }
    if (bbmriGender.toLowerCase().equals("female")) {
      return new BeaconSex("NCIT:C16576", "female");
    }
    return new BeaconSex("NCIT:C1799", "unknown");
  }
}
