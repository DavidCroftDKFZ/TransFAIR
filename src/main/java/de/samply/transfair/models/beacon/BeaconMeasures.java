package de.samply.transfair.models.beacon;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Models a set of measures as understood by Beacon 2.x.
 */
@Slf4j
public class BeaconMeasures {
  private List<BeaconMeasure> measures = new ArrayList<BeaconMeasure>();

  /**
   * Add the supplied measure to the list of measures.
   *
   * @param beaconMeasure measure to be added.
   */
  public void add(BeaconMeasure beaconMeasure) {
    measures.add(beaconMeasure);
  }
}
