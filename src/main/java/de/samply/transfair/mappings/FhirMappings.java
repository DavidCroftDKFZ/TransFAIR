package de.samply.transfair.mappings;

/** Super class. */
public abstract class FhirMappings {

  private Boolean setup() {
    return true;
  }

  /** Super transfering. */
  public abstract void transfer() throws Exception;

  /** Override for the source fhir server. */
  public String overrideSourceFhirServer;

  /** Override for the target fhir server. */
  public String overrideTargetFhirServer;
}
