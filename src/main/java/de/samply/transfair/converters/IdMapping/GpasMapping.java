package de.samply.transfair.converters.IdMapping;

import org.jetbrains.annotations.NotNull;

public class GpasMapping extends IdMapping {

  // TODO: Javadoc
  // TODO: Add reading host and port from configuration
  @Override
  public String fetchMapping(
      @NotNull String id, @NotNull String srcDomain, @NotNull String tarDomain) throws Exception {
    String gpas_host = "localhost";
    String gpas_port = "8085";
    String tar_id = ""; // TODO: just initialize and take from gPAs SOP response

    // TODO: SOAP call

    return tar_id;
  }
}
