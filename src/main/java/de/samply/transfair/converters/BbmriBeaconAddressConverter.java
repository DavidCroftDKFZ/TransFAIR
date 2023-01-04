package de.samply.transfair.converters;

import de.samply.transfair.models.beacon.BeaconGeographicOrigin;
import lombok.extern.slf4j.Slf4j;

/** Static methods for converting the bbmri.de address to Beacon geographic location
 * and back. See full list here:
 * https://github.com/EnvironmentOntology/gaz/blob/master/src/ontology/gaz_countries.csv
 * */
@Slf4j
public class BbmriBeaconAddressConverter {

  /** From bbmri.de to Beacon address. */
  public static BeaconGeographicOrigin fromBbmriToBeacon(String bbmriCountry) {
    if (bbmriCountry == null) {
      return null;
    }
    if (bbmriCountry.equalsIgnoreCase("Italy")) {
      return new BeaconGeographicOrigin("GAZ:00002650", "Italy");
    }
    if (bbmriCountry.equalsIgnoreCase("Malta")) {
      return new BeaconGeographicOrigin("GAZ:00004017", "Malta");
    }
    if (bbmriCountry.equalsIgnoreCase("Spain")) {
      return new BeaconGeographicOrigin("GAZ:00000591", "Spain");
    }
    if (bbmriCountry.equalsIgnoreCase("UK")) {
      return new BeaconGeographicOrigin("GAZ:00002637", "United Kingdom");
    }
    if (bbmriCountry.equalsIgnoreCase("USA")) {
      return new BeaconGeographicOrigin("GAZ:00002459", "United States of America");
    }
    log.warn("No Beacon geographic origin found for: " + bbmriCountry);
    return new BeaconGeographicOrigin("GAZ:00000448", "geographic location");
  }
}
