package de.samply.transfair.models.beacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Models sample characteristics as understood by Beacon 2.x.
 */
public class BeaconSampleCharacteristics {
  public List<BeaconOrganism> organism;

  public static BeaconSampleCharacteristics createHumanSampleCharacteristics() {
    BeaconSampleCharacteristics characteristics = new BeaconSampleCharacteristics();
    List<BeaconOrganism> organisms = new ArrayList<BeaconOrganism>();
    organisms.add(BeaconOrganism.createHumanBeaconOrganism());
    characteristics.organism = organisms;

    return characteristics;
  }
}
