package de.samply.transfair.resources;

import java.util.Objects;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;

/** Static methods for filtering specific profiles. */
public class CheckResources {
  public static boolean checkBbmriCauseOfDeath(Observation observation) {
    return observation.getCode().getCodingFirstRep().getCode().equals("68343-3");
  }

  public static boolean checkMiiCauseOfDeath(Condition condition) {
    return (Objects.equals(
                condition.getCategoryFirstRep().getCodingFirstRep().getCode(), "16100001")
            && Objects.equals(
                condition.getCategoryFirstRep().getCodingFirstRep().getSystem(),
                "http://snomed.info/sct"))
        || (Objects.equals(
                condition.getCategoryFirstRep().getCodingFirstRep().getSystem(), "http://loinc.org")
            && Objects.equals(
                condition.getCategoryFirstRep().getCodingFirstRep().getCode(), "79378-6"));
  }
}
