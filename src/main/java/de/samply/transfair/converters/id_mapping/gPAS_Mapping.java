package de.samply.transfair.converters.id_mapping;

import org.jetbrains.annotations.NotNull;

public class gPAS_Mapping extends ID_Mapping {

  // TODO: Javadoc
  // TODO: Add reading host and port from configuration
  @Override
  public String fetch_mapping(
      @NotNull String id, @NotNull String src_domain, @NotNull String tar_domain) throws Exception {
    String gpas_host = "localhost";
    String gpas_port = "8085";
    String tar_id = ""; // TODO: just initialize and take from gPAs SOP response

    // TODO: SOAP call

    return tar_id;
  }
}
