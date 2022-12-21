package de.samply.transfair;

import de.samply.transfair.mappings.Bbmri2Bbmri;
import de.samply.transfair.mappings.Bbmri2Mii;
import de.samply.transfair.mappings.Mii2Bbmri;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Contains the default mappings. */
@Slf4j
@Service
public class MappingService {

  @Autowired
  Bbmri2Bbmri bbmri2Bbmri;

  @Autowired
  Bbmri2Mii bbmri2Mii;

  @Autowired
  Mii2Bbmri mii2Bbmri;

  @Autowired
  Configuration configuration;

  /** Starts the transformation. */
  public void run() throws Exception {
    switch (configuration.getProfile()) {
      case "BBMRI2BBMRI" -> bbmri2Bbmri.transfer();
      case "BBMRI2MII" -> bbmri2Mii.transfer();
      case "MII2BBMRI" -> mii2Bbmri.transfer();
      default -> log.info("Provided mode is not supported!");
    }
  }
}
