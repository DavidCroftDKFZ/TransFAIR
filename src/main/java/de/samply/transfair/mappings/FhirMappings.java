package de.samply.transfair.mappings;

public abstract class FhirMappings {

  private Boolean setup() {
    return true;
  }

  public abstract void transfer() throws Exception;

  public String overrideSourceFhirServer;
  public String overrideTargetFhirServer;
}
