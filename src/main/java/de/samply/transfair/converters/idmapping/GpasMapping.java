package de.samply.transfair.converters.idmapping;

import org.jetbrains.annotations.NotNull;

/** Mapping for GPAS. */
public class GpasMapping extends IdMapping {

  // TODO: Javadoc
  // TODO: Add reading host and port from configuration
  /** todo Mapping for GPAS. */
  @Override
  public String fetchMapping(
      @NotNull String id, @NotNull String srcDomain, @NotNull String tarDomain) throws Exception {
    String gpasHost = "localhost";
    String gpasPort = "8085";
    String tarId = ""; // TODO: just initialize and take from gPAs SOP response

    // TODO: SOAP call

    return tarId;
  }
}
